package myapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import myapp.model.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> 
{

} 
