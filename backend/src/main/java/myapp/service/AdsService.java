package myapp.service;

import myapp.model.Place;
import myapp.model.PlaceRating;
import myapp.model.User;
import myapp.payload.AdRecommendation;
import myapp.repository.PlaceRepository;
import myapp.repository.PlaceRatingRepository;
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
    private UserRepository userRepository;

    @Autowired
    private KieContainer kieContainer;

    public List<AdRecommendation> recommendForUser(Long userId) {
        List<Place> places = placeRepository.findAll();
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // Simple pre-scoring and pass to Drools via globals
        Map<Long, Double> scoreMap = new HashMap<>();
        Map<Long, String> reasonMap = new HashMap<>();
        for (Place p : places) {
            scoreMap.put(p.getId(), 0.0);
            reasonMap.put(p.getId(), "");
        }

        KieSession ksession = kieContainer.newKieSession("ksession-rules");
        try {
            ksession.setGlobal("placeScores", scoreMap);
            ksession.setGlobal("placeReasons", reasonMap);
            ksession.setGlobal("currentUser", user);
            // insert places and ratings
            for (Place p : places) ksession.insert(p);
            List<PlaceRating> ratings = placeRatingRepository.findAll();
            for (PlaceRating r : ratings) ksession.insert(r);

            if (user != null) ksession.insert(user);

            ksession.fireAllRules();
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
