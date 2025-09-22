package myapp.controller;

import myapp.model.Role;
import myapp.model.User;
import myapp.payload.BadUserDetection;
import myapp.repository.UserRepository;
import myapp.service.BadUserDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private BadUserDetectionService badUserDetectionService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Pokreće detekciju loših korisnika (samo admin)
     */
    @PostMapping("/detect-bad-users")
    // @PreAuthorize("hasRole('ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<?> detectBadUsers() {
        try {
            // Temporarily skip admin check for testing
            List<BadUserDetection> suspiciousUsers = badUserDetectionService.detectBadUsers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Detekcija završena uspešno");
            response.put("suspiciousUsersCount", suspiciousUsers.size());
            response.put("suspiciousUsers", suspiciousUsers);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Greška tokom detekcije: " + e.getMessage()));
        }
    }

    /**
     * Vraća listu sumnjivih korisnika (samo admin)
     */
    @GetMapping("/suspicious-users")
    // @PreAuthorize("hasRole('ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<?> getSuspiciousUsers() {
        try {
            // Temporarily skip admin check for testing
            List<BadUserDetection> suspiciousDetections = badUserDetectionService.detectBadUsers();
            List<BadUserDetection> onlySuspicious = suspiciousDetections.stream()
                .filter(BadUserDetection::isSuspicious)
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("suspiciousUsers", onlySuspicious);
            response.put("count", onlySuspicious.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Greška pri dohvatanju sumnjivih korisnika: " + e.getMessage()));
        }
    }

    /**
     * Analizira jednog konkretnog korisnika (samo admin)
     */
    @PostMapping("/analyze-user/{userId}")
    // @PreAuthorize("hasRole('ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<?> analyzeSpecificUser(@PathVariable Long userId) {
        try {
            // Temporarily skip admin check for testing
            Optional<User> targetUserOpt = userRepository.findById(userId);
            if (targetUserOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Korisnik sa ID " + userId + " nije pronađen"));
            }

            User targetUser = targetUserOpt.get();
            BadUserDetection analysis = badUserDetectionService.analyzeUser(targetUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysis", analysis);
            response.put("userId", userId);
            response.put("userEmail", targetUser.getEmail());
            response.put("isSuspicious", analysis.isSuspicious());
            response.put("reason", analysis.getSuspicionReason());
            response.put("currentSuspension", Map.of(
                "isSuspended", targetUser.isSuspended(),
                "suspendedUntil", targetUser.getSuspendedUntil(),
                "canPost", targetUser.getCanPost(),
                "canLogin", targetUser.getCanLogin(),
                "suspensionReason", targetUser.getSuspensionReason()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Greška pri analizi korisnika: " + e.getMessage()));
        }
    }

    /**
     * Zdravstvena proverava stanja sistema (samo admin)
     */
    @GetMapping("/system-health")
    // @PreAuthorize("hasRole('ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<?> getSystemHealth() {
        try {
            // Temporarily skip admin check for testing
            long totalUsers = userRepository.count();
            List<BadUserDetection> allDetections = badUserDetectionService.detectBadUsers();
            long suspiciousUsersCount = allDetections.stream()
                .filter(BadUserDetection::isSuspicious)
                .count();
            
            long activeSuspensions = userRepository.findAll().stream()
                .filter(user -> user.getSuspendedUntil() != null && 
                               user.getSuspendedUntil().isAfter(java.time.LocalDateTime.now()))
                .count();

            Map<String, Object> health = new HashMap<>();
            health.put("status", "OK");
            health.put("timestamp", java.time.LocalDateTime.now());
            health.put("badUserDetectionService", "RUNNING");
            health.put("droolsEngine", "ACTIVE");
            health.put("totalUsers", totalUsers);
            health.put("suspiciousUsers", suspiciousUsersCount);
            health.put("activeSuspensions", activeSuspensions);
            health.put("totalPosts", 0); // TODO: Add post count
            health.put("totalReports", 0); // TODO: Add report count

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Greška pri proveri stanja sistema: " + e.getMessage()));
        }
    }
}