package myapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import myapp.model.FriendRequest;
import myapp.payload.FriendRequestDto;
import myapp.service.FriendRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/friendRequest")
public class FriendRequestController 
{
    @Autowired
    private FriendRequestService friendRequestService;

    @PostMapping("/send")
    public ResponseEntity<?> sendFriendRequest(@RequestBody FriendRequestDto friendRequestDto) 
    {
        friendRequestService.sendFriendRequest(friendRequestDto.getSenderId(), friendRequestDto.getReceiverId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestBody Long requestId) 
    {
        friendRequestService.acceptFriendRequest(requestId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestBody Long requestId) 
    {
        friendRequestService.rejectFriendRequest(requestId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/all")
    public List<FriendRequest> getAllFriendRequests(@RequestParam Long userId) 
    {
        return friendRequestService.getRequestsForUser(userId);
    }
    
}
