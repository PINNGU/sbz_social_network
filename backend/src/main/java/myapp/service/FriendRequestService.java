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
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(senderId);
        friendRequest.setReceiverId(receiverId);
        friendRequest.setStatus("PENDING");
        friendRequest.setTimestamp(System.currentTimeMillis());
        friendRequestRepository.save(friendRequest);
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

    public List<FriendRequest> getRequestsForUser(Long userId) 
    {
        return friendRequestRepository.findByReceiverId(userId);
    }
}
