package myapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import myapp.model.Block;
import myapp.repository.BlockRepository;

@Service
public class BlockService {
    @Autowired
    private BlockRepository blockRepository;

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
}
