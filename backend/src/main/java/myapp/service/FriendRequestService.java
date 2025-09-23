package myapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import myapp.model.FriendRequest;
import myapp.repository.FriendRequestRepository;

@Service
public class FriendRequestService 
{
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendsService friendsService;

    public void sendFriendRequest(Long senderId, Long receiverId) 
    {
        if(!friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "PENDING"))
        {
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setSenderId(senderId);
            friendRequest.setReceiverId(receiverId);
            friendRequest.setStatus("PENDING");
            friendRequest.setTimestamp(System.currentTimeMillis());
            friendRequestRepository.save(friendRequest);
        }
    }

    @Transactional
    public void acceptFriendRequest(Long requestId) 
    {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId).orElse(null);
        if (friendRequest != null) 
        {
            friendRequest.setStatus("ACCEPTED");
            friendRequest.setTimestampAnswer(System.currentTimeMillis());
            friendRequestRepository.save(friendRequest);
            friendsService.addFriend(friendRequest.getSenderId(), friendRequest.getReceiverId());
        }
    }

    @Transactional
    public void rejectFriendRequest(Long requestId) 
    {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId).orElse(null);
        if (friendRequest != null) 
        {
            friendRequest.setStatus("REJECTED");
            friendRequest.setTimestampAnswer(System.currentTimeMillis());
            friendRequestRepository.save(friendRequest);
        }
    }
    @Transactional
    public void deleteFriendRequest(Long userId, Long friendId) 
    {
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(userId, friendId, "PENDING");
        if (friendRequest != null) 
        {
            friendRequestRepository.delete(friendRequest);
        }
    }
    
    public List<FriendRequest> getRequestsForUser(Long userId) 
    {
        return friendRequestRepository.findByReceiverId(userId);
    }

    public List<FriendRequest> getSentRequestsForUser(Long userId) 
    {
        return friendRequestRepository.findBySenderId(userId);
    }
}
