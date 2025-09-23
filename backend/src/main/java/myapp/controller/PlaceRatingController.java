package myapp.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import myapp.model.PlaceRating;
import myapp.service.PlaceRatingService;

@RestController
@RequestMapping("/api/placeRating")
public class PlaceRatingController 
{
    @Autowired
    private PlaceRatingService placeRatingService;

    @GetMapping("/all")
    public ResponseEntity<List<PlaceRating>> getPlaceRatings(@RequestParam("placeId") Long placeId) 
    {
        List<PlaceRating> placeRatings = placeRatingService.getPlaceRatings(placeId);
        return ResponseEntity.ok(placeRatings);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createPlaceRating(@RequestBody PlaceRating placeRating) 
    {
        placeRatingService.createPlaceRating(placeRating.getUserId(),placeRating.getPlaceId(), placeRating.getRating(), placeRating.getComment(), placeRating.getHashtag());
        return ResponseEntity.status(Response.SC_CREATED).build();
    }
    @PutMapping("/update")
    public ResponseEntity<Void> updatePlaceRating(@RequestBody PlaceRating placeRating) 
    {
        placeRatingService.updateRating(placeRating.getUserId(), placeRating.getPlaceId(), placeRating.getRating(), placeRating.getComment(), placeRating.getHashtag());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deletePlaceRating(@RequestParam("userId") Long userId, @RequestParam("placeId") Long placeId) 
    {
        placeRatingService.deletePlaceRating(userId, placeId);
        return ResponseEntity.ok().build();
    }

}
