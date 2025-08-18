package myapp.repository;

import myapp.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Logger logger = LoggerFactory.getLogger(PostRepository.class);
    // Additional query methods if needed
}
