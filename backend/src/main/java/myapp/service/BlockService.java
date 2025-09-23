package myapp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import myapp.model.Block;
import myapp.model.User;
import myapp.repository.BlockRepository;
import myapp.repository.UserRepository;

@Service
public class BlockService {
    @Autowired
    private BlockRepository blockRepository;
    
    @Autowired
    private UserRepository userRepository;

    public void blockUser(Long userId, Long blockedUserId) 
    {
        if(!blockRepository.existsByUserIdAndBlockedUserId(userId, blockedUserId))
        {
            Block block = new Block();
            block.setUserId(userId);
            block.setBlockedUserId(blockedUserId);
            blockRepository.save(block);
        }
    }
    public void unblockUser(Long userId, Long blockedUserId)
    {
        Block block = blockRepository.findByUserIdAndBlockedUserId(userId, blockedUserId);
        if (block != null) 
        {
            blockRepository.delete(block);
        }
    }
    public List<User> userIsBlockedBy(Long UserId)
    {
        List<Block> blocks = blockRepository.findByBlockedUserId(UserId);
        return blocks.stream()
            .map(block -> userRepository.findById(block.getUserId()).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toList());
    }
    public List<User> userIsBlocking(Long UserId)
    {
        List<Block> blocks = blockRepository.findByUserId(UserId);
        return blocks.stream()
            .map(block -> userRepository.findById(block.getBlockedUserId()).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toList());
    }
}
