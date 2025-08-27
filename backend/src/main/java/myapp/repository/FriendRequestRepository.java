package myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import myapp.model.FriendRequest;

@Repository
public interface FriendRequestRepository extends JpaRepository<myapp.model.FriendRequest, Long> 
{

    List<FriendRequest> findByReceiverId(Long userId);
    
}
