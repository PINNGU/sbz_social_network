package myapp.service;

import myapp.model.Block;
import myapp.model.Post;
import myapp.model.PostReport;
import myapp.model.User;
import myapp.payload.BadUserDetection;
import myapp.payload.Suspension;
import myapp.payload.UserActivity;
import myapp.repository.BlockRepository;
import myapp.repository.PostRepository;
import myapp.repository.UserRepository;
import org.drools.core.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BadUserDetectionService {

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Transactional
    public List<BadUserDetection> detectBadUsers() 
    {
        List<User> allUsers = userRepository.findAll();
        List<BadUserDetection> suspiciousUsers = new ArrayList<>();

        for (User user : allUsers) 
        {
            BadUserDetection detection = analyzeUser(user);
            if (detection.isSuspicious()) 
            {
                suspiciousUsers.add(detection);
            }
        }

        return suspiciousUsers;
    }

    @Transactional
    public BadUserDetection analyzeUser(User user) 
    {
        List<UserActivity> activities = collectUserActivities(user);
        BadUserDetection detection = new BadUserDetection(user, activities);

        KieSession kieSession = kieContainer.newKieSession("ksession-bad-users");
        
        try {
            // Lista za čuvanje suspenzija koje će biti kreirane tokom izvršavanja pravila
            List<Suspension> suspensions = new ArrayList<>();
            kieSession.setGlobal("suspensions", suspensions);

            // Dodaj detection object u working memory
            kieSession.insert(detection);
            
            // Dodaj sve aktivnosti u working memory
            for (UserActivity activity : activities) {
                kieSession.insert(activity);
            }

            // Pokreni samo "bad-users" pravila
            kieSession.getAgenda().getAgendaGroup("bad-users").setFocus();
            int rulesFired = kieSession.fireAllRules();
            
            System.out.println("DEBUG: Pokrenuto je " + rulesFired + " pravila za korisnika " + user.getEmail());
            System.out.println("DEBUG: Kreiran je " + suspensions.size() + " broj suspenzija");

            // Primeni suspenzije
            applySuspensions(suspensions);

            return detection;
        } finally {
            kieSession.dispose();
        }
    }

    private List<UserActivity> collectUserActivities(User user) 
    {
        List<UserActivity> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        System.out.println("DEBUG: collectUserActivities za korisnika " + user.getEmail() + " (ID: " + user.getId() + ")");

        // Aktivnosti prijavljivanja objava
        List<Post> userPosts = postRepository.findByUserId(user.getId());
        System.out.println("DEBUG: Pronadjeno " + userPosts.size() + " postova za korisnika");
        
        for (Post post : userPosts) 
        {
            System.out.println("DEBUG: Post ID " + post.getId() + " ima " + post.getReports().size() + " prijava");
            // Za svaku prijavu posta, kreiraj aktivnost sa pravim timestamp-om
            for (PostReport report : post.getReports()) 
            {
                // Koristimo timestamp prijave ili datum kreiranja posta kao fallback
                LocalDateTime reportTime = report.getReportTimestamp() != null 
                    ? report.getReportTimestamp() 
                    : post.getDateOfCreation();
                    
                System.out.println("DEBUG: Kreiram UserActivity - reporterUserId: " + report.getReporterUserId() + 
                                 ", reportTime: " + reportTime + ", targetUserId: " + user.getId());
                    
                UserActivity activity = new UserActivity(
                    report.getReporterUserId(), // Ko je prijavio
                    "POST_REPORTED",
                    reportTime, // Koristi pravi timestamp prijave ili fallback
                    "Objava prijavljena",
                    user.getId() // Vlasnik posta je target
                );
                activities.add(activity);
            }
        }

        // Aktivnosti blokiranja (korisnik je blokiran od strane drugih)
        List<Block> blocksAgainstUser = blockRepository.findByBlockedUserId(user.getId());
        for (Block block : blocksAgainstUser) 
        {
            UserActivity activity = new UserActivity(
                block.getUserId(), // Ko je blokirao
                "USER_BLOCKED",
                now.minusDays(1), // Aproksimacija vremena blokiranja
                "Korisnik blokiran",
                user.getId()
            );
            activities.add(activity);
        }

        // Aktivnosti preteranog objavljivanja
        long recentPostsCount = userPosts.stream()
            .filter(post -> post.getDateOfCreation().isAfter(now.minusHours(1))) 
            .count();
        
        for (int i = 0; i < recentPostsCount; i++) 
        {
            UserActivity activity = new UserActivity(
                user.getId(),
                "EXCESSIVE_POSTING",
                now.minusMinutes(30),
                "Prekomerno objavljivanje",
                null
            );
            activities.add(activity);
        }
        long excessivePostsCount = userPosts.stream()   //checks for posts in the last 30 mins
            .filter(post -> post.getDateOfCreation().isAfter(now.minusMinutes(30))) 
            .count();

        if (excessivePostsCount > 20) 
        {
            for (int i = 0; i < 20; i++) 
            {
                UserActivity activity = new UserActivity(
                    user.getId(),
                    "SPAM_ACTIVITY",
                    now.minusMinutes(15),
                    "Spam aktivnost",
                    null
                );
                activities.add(activity);
            }
        }

        System.out.println("DEBUG: Ukupno kreirano " + activities.size() + " UserActivity objekata");
        for (UserActivity activity : activities) {
            System.out.println("DEBUG: Activity - userId: " + activity.getUserId() + 
                             ", type: " + activity.getActivityType() + 
                             ", time: " + activity.getTimestamp() + 
                             ", targetId: " + activity.getTargetId());
        }

        return activities;
    }

    @Transactional
    private void applySuspensions(List<Suspension> suspensions) 
    {
        //System.out.println("DEBUG: applySuspensions pozvan sa " + suspensions.size() + " suspenzija");
        for (Suspension suspension : suspensions) 
        {
            //System.out.println("DEBUG: Obradjujem suspenziju za userId: " + suspension.getUserId());
            User user = userRepository.findById(suspension.getUserId()).orElse(null);
            if (user != null) 
            {
                //System.out.println("DEBUG: Korisnik pronadjen: " + user.getEmail());
                if(user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now())) 
                {
                    if(suspension.getSuspensionEnd().isAfter(user.getSuspendedUntil())) 
                    {
                        user.setSuspendedUntil(suspension.getSuspensionEnd());
                    }
                } 
                else 
                {
                    user.setSuspendedUntil(suspension.getSuspensionEnd());
                }
                user.setSuspensionReason(suspension.getReason());
                
                //System.out.println("DEBUG: Postavljam suspendedUntil: " + suspension.getSuspensionEnd());
                //System.out.println("DEBUG: Postavljam suspensionReason: " + suspension.getReason());
                
                switch (suspension.getSuspensionType()) 
                {
                    case "POST_BAN":
                        user.setCanPost(false);
                        break;
                    case "LOGIN_BAN":
                        user.setCanLogin(false);
                        break;
                    case "FULL_SUSPENSION":
                        user.setCanPost(false);
                        user.setCanLogin(false);
                        break;
                }
                
                userRepository.save(user);
                userRepository.flush(); // Forsiraj flush u bazu
                //System.out.println("DEBUG: Korisnik snimljen u bazu");
                
                // Proveri da li su se promena perzistirale
                User verifyUser = userRepository.findById(suspension.getUserId()).orElse(null);
                if (verifyUser != null) {
                    //System.out.println("DEBUG: VERIFIKACIJA - can_post: " + verifyUser.getCanPost());
                    //System.out.println("DEBUG: VERIFIKACIJA - suspended_until: " + verifyUser.getSuspendedUntil());
                    //System.out.println("DEBUG: VERIFIKACIJA - suspension_reason: " + verifyUser.getSuspensionReason());
                } else {
                    //System.out.println("DEBUG: GREŠKA - ne mogu da učitam korisnika posle save!");
                }
                
                System.out.println("Suspenzija primenjena na korisnika: " + user.getEmail() + 
                                 " - Tip: " + suspension.getSuspensionType() + 
                                 " - Do: " + suspension.getSuspensionEnd() +
                                 " - Razlog: " + suspension.getReason());
            } else {
                System.out.println("DEBUG: Korisnik sa ID " + suspension.getUserId() + " nije pronadjen");
            }
        }
    }

    public List<User> getSuspiciousUsers() 
    {
        return detectBadUsers().stream()
            .filter(BadUserDetection::isSuspicious)
            .map(BadUserDetection::getUser)
            .collect(Collectors.toList());
    }

    public int clearExpiredSuspensions() 
    {
        LocalDateTime now = LocalDateTime.now();
        List<User> allUsers = userRepository.findAll();
        int clearedCount = 0;
        
        for (User user : allUsers) {
            if (user.getSuspendedUntil() != null && now.isAfter(user.getSuspendedUntil())) 
            {
                user.setSuspendedUntil(null);
                user.setCanPost(true);
                user.setCanLogin(true);
                user.setSuspensionReason(null);
                
                userRepository.save(user);
                clearedCount++;
                
                System.out.println("Ociscena suspenzija za korisnika: " + user.getEmail());
            }
        }
        
        return clearedCount;
    }
}