package myapp.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import myapp.model.User;
import myapp.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController 
{
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam("userId") Long userId) {
        List<User> users = userService.getAllNotBlockedUsers(userId);
        return ResponseEntity.ok(users);
    }

}
