package myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import myapp.model.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    Block findByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    List<Block> findByBlockedUserId(Long userId);

    List<Block> findByUserId(Long userId);
    
}
