package myapp.service;

import org.springframework.stereotype.Service;

import myapp.model.Place;
import myapp.repository.PlaceRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    public void createPlace(Place place) 
    {
        placeRepository.save(place);
    }
    public List<Place> getAllPlaces() 
    {
        return placeRepository.findAll();
    }
}
