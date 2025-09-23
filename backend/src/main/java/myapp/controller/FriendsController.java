package myapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import myapp.model.User;
import myapp.payload.FriendRequestDto;
import myapp.service.FriendsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/friends")
public class FriendsController {
    @Autowired
    private FriendsService friendsService;

    @PutMapping("/remove")
    public ResponseEntity<?> removeFriend(@RequestBody FriendRequestDto friend) 
    {
        friendsService.removeFriend(friend.getSenderId(), friend.getReceiverId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllFriends(@RequestParam("userId") Long userId) 
    {
        List<User> friends = friendsService.getFriendsByUserId(userId);
        return ResponseEntity.ok(friends);
    }
    @GetMapping("/blocked")
    public ResponseEntity<List<User>> getBlockedFriends(@RequestParam("userId") Long userId) 
    {
        List<User> blockedFriends = friendsService.getBlockedFriendsByUserId(userId);
        return ResponseEntity.ok(blockedFriends);
    }
    

}
