package com.example.cyberpajzs.service;

import com.example.cyberpajzs.entity.User;
import com.example.cyberpajzs.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors; // Added for stream API

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Felhasználó nem található: " + username));

        // IMPORTANT CHANGE HERE: Remove "ROLE_" prefix before passing to roles()
        String[] rolesArray = user.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role) // Remove "ROLE_"
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(rolesArray) // Pass the roles without "ROLE_" prefix
                .build();
    }

    @Transactional
    public User registerNewUser(String username, String password, String email, String firstName, String lastName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Ez a felhasználónév már foglalt.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ez az e-mail cím már regisztrálva van.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // Hash the password
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER"); // Default role
        newUser.setRoles(roles);

        return userRepository.save(newUser);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User updateUser(User user, String password, List<String> roles) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található, ID: " + user.getId()));

        existingUser.setUsername(user.getUsername()); // Username can be edited in admin interface
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());

        // Update password if new one is provided
        if (password != null && !password.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }

        // Update roles if new list is provided
        if (roles != null) {
            // Ensure "ROLE_" prefix is added when saving roles to the User entity
            Set<String> newRoles = roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toSet());
            existingUser.setRoles(newRoles);
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public User updateProfile(User userDetails, String newPassword) {
        User existingUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található, ID: " + userDetails.getId()));

        // Only update relevant profile fields
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail()); // Update email

        // Update password if new one is provided
        if (newPassword != null && !newPassword.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("A felhasználó nem található, ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
