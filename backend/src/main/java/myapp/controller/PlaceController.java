package myapp.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import myapp.model.Place;
import myapp.service.PlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/places")
public class PlaceController {
    @Autowired
    private PlaceService placeService;

    @GetMapping("/all")
    public ResponseEntity<List<Place>> getAllPlaces() 
    {
        List<Place> places = placeService.getAllPlaces();
        return ResponseEntity.ok(places);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createPlace(@RequestBody Place place) 
    {
        placeService.createPlace(place);
        return ResponseEntity.status(Response.SC_CREATED).build();
    }
}
