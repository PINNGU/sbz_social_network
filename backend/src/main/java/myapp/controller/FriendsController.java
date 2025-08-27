package myapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import myapp.payload.FriendRequestDto;
import myapp.service.FriendsService;
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
}
