package myapp.repository;

import org.springframework.stereotype.Repository;

import myapp.model.Friends;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface FriendsRepository extends JpaRepository<myapp.model.Friends, Long> {

    Friends findByUserIdAndFriendId(Long userId, Long friendId);
    
}
