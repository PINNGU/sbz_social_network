package myapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import myapp.model.PlaceRating;
import myapp.repository.PlaceRatingRepository;

@Service
public class PlaceRatingService {
    @Autowired
    private PlaceRatingRepository placeRatingRepository;

    public void createPlaceRating(Long userId, Long placeId, int rating, String comment,String hashtag) 
    {
        if (!placeRatingRepository.existsByUserIdAndPlaceId(userId, placeId)) 
        {
            PlaceRating placeRating = new PlaceRating();
            placeRating.setUserId(userId);
            placeRating.setPlaceId(placeId);
            placeRating.setRating(rating);
            placeRating.setComment(comment);
            placeRating.setHashtag(hashtag);
            placeRating.setCreatedAt(java.time.LocalDateTime.now());
            placeRatingRepository.save(placeRating);
        }
    }

    public void updateRating(Long userId, Long placeId, int rating, String comment, String hashtag) 
    {
        PlaceRating placeRating = placeRatingRepository.findByUserIdAndPlaceId(userId, placeId);
        if(placeRating == null)
            return;
        placeRating.setRating(rating);
        placeRating.setComment(comment);
        placeRating.setHashtag(hashtag);
        placeRating.setCreatedAt(java.time.LocalDateTime.now());
        placeRatingRepository.save(placeRating);
    }

    public void deletePlaceRating(Long userId, Long placeId) 
    {
        PlaceRating placeRating = placeRatingRepository.findByUserIdAndPlaceId(userId, placeId);
        if(placeRating == null)
            return;
        placeRatingRepository.delete(placeRating);
    }
    public List<PlaceRating> getUserRatings(Long userId) 
    {
        return placeRatingRepository.findByUserId(userId);
    }
    public List<PlaceRating> getPlaceRatings(Long placeId) 
    {
        return placeRatingRepository.findByPlaceId(placeId);
    }

}
