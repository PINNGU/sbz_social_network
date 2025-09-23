package myapp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import myapp.model.Block;
import myapp.model.FriendRequest;
import myapp.model.Friends;
import myapp.model.User;
import myapp.repository.BlockRepository;
import myapp.repository.FriendRequestRepository;
import myapp.repository.FriendsRepository;
import myapp.repository.UserRepository;

@Service
public class FriendsService 
{
    @Autowired
    private FriendsRepository friendsRepository;
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BlockRepository blockRepository;

    @Transactional
    public void addFriend(Long userId, Long friendId) 
    {
        if(!friendExists(userId,friendId))
        {
            Friends friends = new Friends();
            friends.setUserId(userId);
            friends.setFriendId(friendId);
            friends.setTimestamp(System.currentTimeMillis());
            friendsRepository.save(friends);
        }
    }
    
    private boolean friendExists(Long userId, Long friendId)
    {
        Friends friend1 = friendsRepository.findByUserIdAndFriendId(userId,friendId);
        Friends friend2 = friendsRepository.findByUserIdAndFriendId(friendId,userId);
        if(friend1 == null && friend2 == null)
            return false;
        else
            return true;
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId)
    {
        Friends friends = friendsRepository.findByUserIdAndFriendId(userId, friendId);
        Friends friends2 = friendsRepository.findByUserIdAndFriendId(friendId, userId);
        if (friends != null) 
        {
            FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(userId, friendId, "ACCEPTED");
            if(friendRequest != null)
            {
                friendRequest.setStatus("REJECTED");
                friendRequestRepository.save(friendRequest);
            }
            friendsRepository.delete(friends);
        }
        else if(friends2 != null)
        {
            FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(friendId, userId, "ACCEPTED");
            if(friendRequest != null)
            {
                friendRequest.setStatus("REJECTED");
                friendRequestRepository.save(friendRequest);
                
            }
            friendsRepository.delete(friends2);
        }
    }

    public List<User> getFriendsByUserId(Long userId) 
    {
    List<Friends> friendsList1 = friendsRepository.findByUserId(userId);
    List<Friends> friendsList2 = friendsRepository.findByFriendId(userId);

    List<Block> blocksCurrentUser = blockRepository.findByBlockedUserId(userId); 
    for (Block block : blocksCurrentUser) 
    {
        friendsList1.removeIf(f -> f.getFriendId().equals(block.getUserId()));
        friendsList2.removeIf(f -> f.getUserId().equals(block.getUserId()));
    }
    List<User> friendsListFinal = new ArrayList<>();
    for (Friends friends : friendsList1) 
    {
        User friend = userRepository.findById(friends.getFriendId()).orElse(null);
        if (friend != null) 
        {
            friendsListFinal.add(friend);
        }
    }
    for (Friends friends : friendsList2) 
    {
        User friend = userRepository.findById(friends.getUserId()).orElse(null);
        if (friend != null) 
        {
            friendsListFinal.add(friend);
        }
    }
    return friendsListFinal;
}

    public List<User> getBlockedFriendsByUserId(Long userId) 
    {
        List<Block> blockedIds = blockRepository.findByUserId(userId);
        List<User> blockedFriends = new ArrayList<>();
        for (Block block : blockedIds) 
        {
            User user = userRepository.findById(block.getBlockedUserId()).orElse(null);
            if (user != null) {
                blockedFriends.add(user);
            }
        }
        return blockedFriends;
    }
}
