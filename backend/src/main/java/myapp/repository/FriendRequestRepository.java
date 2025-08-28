package myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import myapp.model.FriendRequest;

@Repository
public interface FriendRequestRepository extends JpaRepository<myapp.model.FriendRequest, Long> 
{

    List<FriendRequest> findByReceiverId(Long userId);

    FriendRequest findBySenderIdAndReceiverId(Long userId, Long friendId);

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<FriendRequest> findBySenderId(Long senderId);

    boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, String string);

    FriendRequest findBySenderIdAndReceiverIdAndStatus(Long userId, Long friendId, String string);

    void deleteBySenderIdAndReceiverIdAndStatus(Long userId, Long friendId, String string);
    
}
