package com.JavaWebToken.jwtAuthentication.entity;




import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter // Generates all getters
@Setter // Generates all setters
@NoArgsConstructor // Generates no-args constructor
@AllArgsConstructor // Generates all-args constructor
@Builder // Provides a builder pattern for object creation
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    // Account status fields with default values
    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    // UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    //for this instead of true return fields
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }// expires an account after a certain date

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }// locks after too many attempts

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }// if expired forced to reset password

    @Override
    public boolean isEnabled() {
        return true;
    }// banned or deactivated user
}

