
package myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.ElementCollection;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Address is required")
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Friends: store user IDs of friends
    @ElementCollection
    private Set<Long> friends = new HashSet<>();

    // Suspension fields for bad user detection
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "can_post", nullable = false, columnDefinition = "boolean default true")
    private Boolean canPost = true;

    @Column(name = "can_login", nullable = false, columnDefinition = "boolean default true")
    private Boolean canLogin = true;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Korisnik je omogućen ako može da se loguje i nije suspendovan
        return canLogin && (suspendedUntil == null || LocalDateTime.now().isAfter(suspendedUntil));
    }

    // Getter and setter for friends
    public Set<Long> getFriends() {
        return friends;
    }
    public void setFriends(Set<Long> friends) {
        this.friends = friends;
    }

    // Getters and setters for suspension fields
    public LocalDateTime getSuspendedUntil() {
        return suspendedUntil;
    }
    
    public void setSuspendedUntil(LocalDateTime suspendedUntil) {
        this.suspendedUntil = suspendedUntil;
    }
    
    public Boolean getCanPost() {
        return canPost;
    }
    
    public void setCanPost(Boolean canPost) {
        this.canPost = canPost;
    }
    
    public Boolean getCanLogin() {
        return canLogin;
    }
    
    public void setCanLogin(Boolean canLogin) {
        this.canLogin = canLogin;
    }
    
    public String getSuspensionReason() {
        return suspensionReason;
    }
    
    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    // Pomocne metode za proveru stanja suspenzije
    public boolean isSuspended() {
        return suspendedUntil != null && LocalDateTime.now().isBefore(suspendedUntil);
    }
    
    public boolean canPostContent() {
        return canPost && !isSuspended();
    }
    
    public boolean canLoginToSystem() {
        return canLogin && !isSuspended();
    }
}
