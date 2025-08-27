package myapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import myapp.payload.BlockDto;
import myapp.service.BlockService;

@RestController
@RequestMapping("/api/block")
public class BlockController {
    @Autowired
    private BlockService blockService;

    @PostMapping("/block")
    public ResponseEntity<Void> blockUser(@RequestBody BlockDto blockRequest) 
    {
        blockService.blockUser(blockRequest.getUserId(), blockRequest.getBlockedUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unblock")
    public ResponseEntity<Void> unblockUser(@RequestBody BlockDto blockRequest) 
    {
        blockService.unblockUser(blockRequest.getUserId(), blockRequest.getBlockedUserId());
        return ResponseEntity.ok().build();
    }
}
