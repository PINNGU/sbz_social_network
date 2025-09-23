package myapp.service;

import myapp.model.Block;
import myapp.model.Post;
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
            kieSession.fireAllRules();

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

        // Aktivnosti prijavljivanja objava
        List<Post> userPosts = postRepository.findByUserId(user.getId());
        for (Post post : userPosts) 
        {
            Long reportCount = postRepository.countReportsByPostId(post.getId());
            int count = reportCount != null ? reportCount.intValue() : 0;
            for (int i = 0; i < count; i++) 
            {
                UserActivity activity = new UserActivity(
                    user.getId(),
                    "POST_REPORTED",
                    post.getDateOfCreation(), // Koristimo datum kreiranja posta kao aproksimaciju
                    "Objava prijavljena",
                    post.getId()
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

        return activities;
    }

    private void applySuspensions(List<Suspension> suspensions) 
    {
        for (Suspension suspension : suspensions) 
        {
            User user = userRepository.findById(suspension.getUserId()).orElse(null);
            if (user != null) 
            {
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
                //user.setSuspendedUntil(suspension.getSuspensionEnd());
                user.setSuspensionReason(suspension.getReason());
                
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
                
                System.out.println("Suspenzija primenjena na korisnika: " + user.getEmail() + 
                                 " - Tip: " + suspension.getSuspensionType() + 
                                 " - Do: " + suspension.getSuspensionEnd() +
                                 " - Razlog: " + suspension.getReason());
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
                
                System.out.println("Očišćena suspenzija za korisnika: " + user.getEmail());
            }
        }
        
        return clearedCount;
    }
}