package myapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import myapp.model.Friends;
import myapp.repository.FriendsRepository;

@Service
public class FriendsService 
{
    @Autowired
    private FriendsRepository friendsRepository;

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
            friendsRepository.delete(friends);
        }
        else if(friends2 != null)
        {
            friendsRepository.delete(friends2);
        }
    }
}
