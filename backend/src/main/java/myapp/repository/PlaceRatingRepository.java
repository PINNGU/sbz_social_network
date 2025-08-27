package myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import myapp.model.PlaceRating;

@Repository
public interface PlaceRatingRepository extends JpaRepository<PlaceRating, Long> 
{

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    PlaceRating findByUserIdAndPlaceId(Long userId, Long placeId);

    List<PlaceRating> findByUserId(Long userId);

    List<PlaceRating> findByPlaceId(Long placeId);

}
