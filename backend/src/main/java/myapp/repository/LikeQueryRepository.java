package myapp.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface LikeQueryRepository extends JpaRepository<myapp.model.Post, Long> {
    @Query(value = "SELECT COUNT(*) FROM post_likes WHERE post_id = :postId", nativeQuery = true)
    int countLikesByPostId(@Param("postId") Long postId);

    @Query(value = "SELECT liker_user_id FROM post_likes WHERE post_id = :postId", nativeQuery = true)
    java.util.List<Long> findLikerUserIdsByPostId(@Param("postId") Long postId);
}
