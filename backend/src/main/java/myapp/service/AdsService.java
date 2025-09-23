package myapp.service;

import myapp.model.Place;
import myapp.model.PlaceRating;
import myapp.model.Post;
import myapp.model.User;
import myapp.payload.AdRecommendation;
import myapp.repository.PlaceRepository;
import myapp.repository.PlaceRatingRepository;
import myapp.repository.PostRepository;
import myapp.repository.UserRepository;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdsService {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceRatingRepository placeRatingRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KieContainer kieContainer;

    public List<AdRecommendation> recommendForUser(Long userId) {
        System.out.println("DEBUG AdsService: Starting recommendation for user: " + userId);
        
        List<Place> places = placeRepository.findAll();
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                System.out.println("DEBUG AdsService: User found - name: " + user.getName() + ", address: '" + user.getAddress() + "'");
            }
        }

        // Simple pre-scoring and pass to Drools via globals
        Map<Long, Double> scoreMap = new HashMap<>();
        Map<Long, String> reasonMap = new HashMap<>();
        for (Place p : places) 
        {
            scoreMap.put(p.getId(), 0.0);
            reasonMap.put(p.getId(), "");
        }

        KieSession ksession = kieContainer.newKieSession("ksession-ads");
        try 
        {
            // Set globals for ads rules only
            ksession.setGlobal("placeScores", scoreMap);
            ksession.setGlobal("placeReasons", reasonMap);
            ksession.setGlobal("currentUser", user);
            
            // insert places and ratings
            for (Place p : places) ksession.insert(p);
            List<PlaceRating> ratings = placeRatingRepository.findAll();
            for (PlaceRating r : ratings) ksession.insert(r);
            
            // insert all posts for hashtag matching from liked posts
            List<Post> posts = postRepository.findAll();
            for (Post post : posts) ksession.insert(post);

            if (user != null) ksession.insert(user);

            System.out.println("DEBUG AdsService: Before firing rules - scoreMap: " + scoreMap);
            System.out.println("DEBUG AdsService: Before firing rules - reasonMap: " + reasonMap);
            
            // Activate only "ads" agenda group instead of all rules
            ksession.getAgenda().getAgendaGroup("ads").setFocus();
            ksession.fireAllRules();
            
            System.out.println("DEBUG AdsService: After firing rules - scoreMap: " + scoreMap);
            System.out.println("DEBUG AdsService: After firing rules - reasonMap: " + reasonMap);
        } finally {
            ksession.dispose();
        }

        // Collect results and sort
        List<AdRecommendation> result = new ArrayList<>();
        for (Place p : places) {
            double s = scoreMap.getOrDefault(p.getId(), 0.0);
            String reason = reasonMap.getOrDefault(p.getId(), "");
            if (s > 0) result.add(new AdRecommendation(p, s, reason));
        }

        result.sort(Comparator.comparingDouble(AdRecommendation::getScore).reversed());
        // return top 10
        return result.size() > 10 ? result.subList(0, 10) : result;
    }
}
