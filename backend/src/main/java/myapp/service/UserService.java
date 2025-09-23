package myapp.service;

import myapp.model.Role;
import myapp.model.User;
import myapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private BlockService blockService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        // Check if user with email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.REGULAR); // Assign default REGULAR role
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException 
    {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        return user; // Return custom UserDetails object
    }

    public List<User> getAllNotBlockedUsers(Long userId)
    {
        List<User> userIsBlockedBy = blockService.userIsBlockedBy(userId);
        List<User> userIsBlocking = blockService.userIsBlocking(userId);
        List<User> allUsers = userRepository.findAll();
        allUsers.removeAll(userIsBlockedBy);
        allUsers.removeAll(userIsBlocking);
        return allUsers;
    }
}
