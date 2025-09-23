package myapp.service;

import myapp.model.Role;
import myapp.model.User;
import myapp.payload.BadUserDetection;
import myapp.payload.Suspension;
import myapp.payload.UserActivity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BadUserDetectionTest {

    private KieContainer kieContainer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        KieServices ks = KieServices.Factory.get();
        kieContainer = ks.getKieClasspathContainer();
    }

    //pravilo 1
    @Test
    public void testFiveReportsInOneDayRule() 
    {
        User user = createTestUser(1L, "test@example.com");
        List<UserActivity> activities = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 6; i++) {
            activities.add(new UserActivity(
                (long) (100 + i),    // Drugi korisnici prijavljuju
                "POST_REPORTED",
                now.minusHours(i),
                "Objava prijavljena",
                user.getId()         // Target je korisnik koji se analizira
            ));
        }

        BadUserDetection detection = new BadUserDetection(user, activities);
        List<Suspension> suspensions = executeDroolsRules(detection);

        assertTrue(detection.isSuspicious(), "Korisnik treba da bude označen kao sumnjiv");
        assertEquals("Vise od 5 prijavljenih objava u jednom danu", detection.getSuspicionReason());
        assertFalse(suspensions.isEmpty(), "Treba da postoji suspenzija");
        
        Suspension suspension = suspensions.get(0);
        assertEquals("POST_BAN", suspension.getSuspensionType());
        assertEquals(1, suspension.getDaysBanned());
    }

    //pravilo 2
    @Test
    public void testEightReportsInTwoDaysRule() 
    {
        User user = createTestUser(2L, "test2@example.com");
        List<UserActivity> activities = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 9; i++) {
            activities.add(new UserActivity(
                (long) (200 + i),    // Drugi korisnici prijavljuju
                "POST_REPORTED",
                now.minusHours(i * 5), 
                "Objava prijavljena",
                user.getId()         // Target je korisnik koji se analizira
            ));
        }

        BadUserDetection detection = new BadUserDetection(user, activities);
        List<Suspension> suspensions = executeDroolsRules(detection);

        assertTrue(detection.isSuspicious());
        assertEquals("Vise od 8 prijavljenih objava u dva dana", detection.getSuspicionReason());

        Suspension suspension = suspensions.stream().filter(s -> "POST_BAN".equals(s.getSuspensionType()) && s.getDaysBanned() == 2).findFirst().orElse(null);
        assertEquals("POST_BAN", suspension.getSuspensionType());
        assertEquals(2, suspension.getDaysBanned());
        }

    //pravilo 3
    @Test
    public void testFourBlocksInOneDayRule() 
    {
        User user = createTestUser(3L, "test3@example.com");
        List<UserActivity> activities = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 5; i++) 
        {
            activities.add(new UserActivity(
                (long) (10 + i), // user that blocked this user
                "USER_BLOCKED",
                now.minusHours(i * 4),
                "Korisnik blokiran",
                user.getId() 
            ));
        }

        BadUserDetection detection = new BadUserDetection(user, activities);
        List<Suspension> suspensions = executeDroolsRules(detection);

        assertTrue(detection.isSuspicious());
        assertEquals("Vise od 4 puta blokiran u jednom danu", detection.getSuspicionReason());
        
        Suspension suspension = suspensions.get(0);
        assertEquals("POST_BAN", suspension.getSuspensionType());
        assertEquals(1, suspension.getDaysBanned());
    }

    //pravilo 4
    @Test
    public void testCombinedBlockAndReportRule() 
    {
        User user = createTestUser(4L, "test4@example.com");
        List<UserActivity> activities = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 3; i++) {
            activities.add(new UserActivity(
                (long) (20 + i),
                "USER_BLOCKED",
                now.minusHours(12 + i * 6),
                "Korisnik blokiran",
                user.getId()
            ));
        }
        
        for (int i = 0; i < 4; i++) {
            activities.add(new UserActivity(
                (long) (400 + i),    // Drugi korisnici prijavljuju
                "POST_REPORTED",
                now.minusHours(i * 2),
                "Objava prijavljena",
                user.getId()         // Target je korisnik koji se analizira
            ));
        }

        BadUserDetection detection = new BadUserDetection(user, activities);
        List<Suspension> suspensions = executeDroolsRules(detection);

        assertTrue(detection.isSuspicious());
        assertEquals("2+ blokiranja u 2 dana + 4+ prijave u 1 dan", detection.getSuspicionReason());
        
        Suspension suspension = suspensions.get(0);
        assertEquals("LOGIN_BAN", suspension.getSuspensionType());
        assertEquals(2, suspension.getDaysBanned());
    }

    //pravilo 5
    @Test
    public void testExcessivePostingRule() {
        User user = createTestUser(5L, "test5@example.com");
        List<UserActivity> activities = new ArrayList<>();
        
        // Dodaj 15 objava u poslednji sat
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 15; i++) 
        {
            activities.add(new UserActivity(
                user.getId(),
                "EXCESSIVE_POSTING",
                now.minusMinutes(i * 2),
                "Prekomerno objavljivanje",
                null
            ));
        }

        BadUserDetection detection = new BadUserDetection(user, activities);
        List<Suspension> suspensions = executeDroolsRules(detection);

        assertTrue(detection.isSuspicious());
        assertEquals("Prekomerno objavljivanje - vise od 20 objava u jednom satu", detection.getSuspicionReason());
        
        Suspension suspension = suspensions.get(0);
        assertEquals("POST_BAN", suspension.getSuspensionType());
    }

    //pravilo 6
    @Test
    public void testSpamActivityRule() 
    {
        User user = createTestUser(6L, "test6@example.com");
        List<UserActivity> activities = new ArrayList<>();
        
        // Dodaj 20 spam aktivnosti u poslednja 30 minuta
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 20; i++) {
            activities.add(new UserActivity(
                user.getId(),
                "SPAM_ACTIVITY",
                now.minusMinutes(i),
                "Spam aktivnost",
                null
            ));
        }

        BadUserDetection detection = new BadUserDetection(user, activities);
        List<Suspension> suspensions = executeDroolsRules(detection);

        assertTrue(detection.isSuspicious());
        assertEquals("Spam aktivnost - 20+ aktivnosti u 30 minuta", detection.getSuspicionReason());
        
        Suspension suspension = suspensions.get(0);
        assertEquals("FULL_SUSPENSION", suspension.getSuspensionType());
        assertEquals(1, suspension.getDaysBanned());
    }

    //kad nije sumnjiv
    @Test
    public void testNoSuspiciousActivity() 
    {
        User user = createTestUser(7L, "test7@example.com");
        List<UserActivity> activities = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        activities.add(new UserActivity(
            999L,               // Drugi korisnik prijavljuje
            "POST_REPORTED",
            now.minusDays(2),
            "Stara prijava",
            user.getId()        // Target je korisnik koji se analizira
        ));

        BadUserDetection detection = new BadUserDetection(user, activities);
        List<Suspension> suspensions = executeDroolsRules(detection);

        assertFalse(detection.isSuspicious(), "Korisnik ne treba da bude označen kao sumnjiv");
        assertTrue(suspensions.isEmpty(), "Ne treba da postoji suspenzija");
    }

    private User createTestUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("Test");
        user.setSurname("User");
        user.setRole(Role.REGULAR);
        user.setCanPost(true);
        user.setCanLogin(true);
        return user;
    }

    private List<Suspension> executeDroolsRules(BadUserDetection detection) 
    {
        List<Suspension> suspensions = new ArrayList<>();
        
        KieSession kieSession = kieContainer.newKieSession("ksession-bad-users");
        try {
            kieSession.setGlobal("suspensions", suspensions);
            kieSession.insert(detection);
            
            for (UserActivity activity : detection.getActivities()) {
                kieSession.insert(activity);
            }
            kieSession.getAgenda().getAgendaGroup("bad-users").setFocus();
            kieSession.fireAllRules();
            return suspensions;
        } finally {
            kieSession.dispose();
        }
    }
}