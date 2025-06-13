package com.example.cyberpajzs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set; // Hozzáadva a Set importjához
import java.util.stream.Collectors; // Hozzáadva a Collectors importjához

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") // Javasolt a "user" helyett "users" táblanév
public class User implements UserDetails { // Implementálja az UserDetails interfészt a Spring Security miatt
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Felhasználónév (gyakran e-mail cím)

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false) // Hozzáadva: Vezetéknév
    private String firstName;

    @Column(nullable = false) // Hozzáadva: Keresztnév
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles; // Felhasználói szerepkörök (pl. "ROLE_USER", "ROLE_ADMIN")

    // --- UserDetails interfész implementációi (Spring Security miatt szükséges) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // A szerepköröket GrantedAuthority objektumokká alakítja át
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Mindig igaz, hacsak nem akarsz fiók lejáratot kezelni
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Mindig igaz, hacsak nem akarsz fiók zárolást kezelni
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Mindig igaz, hacsak nem akarsz jelszólejáratot kezelni
    }

    @Override
    public boolean isEnabled() {
        return true; // Mindig igaz, hacsak nem akarsz fiók aktiválást/deaktiválást kezelni
    }
}