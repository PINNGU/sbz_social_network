package myapp.repository;

import myapp.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Logger logger = LoggerFactory.getLogger(PostRepository.class);
    
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    List<Post> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM Post p JOIN p.reports r WHERE p.id = :postId")
    Long countReportsByPostId(@Param("postId") Long postId);
    
    // Additional query methods if needed
}
