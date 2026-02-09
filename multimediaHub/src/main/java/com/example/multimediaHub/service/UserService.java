package com.example.multimediaHub.service;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.Login;
import com.example.multimediaHub.web.dto.Register;
import com.example.multimediaHub.web.dto.UserSettingsDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        // Връщаме ТВОЯ UserData, а не стандартния на Spring
        return new UserData(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );
    }

    @Transactional
    public boolean registerUser(Register register) {
        if (!register.getPassword().equals(register.getConfirmPassword())) {
            throw new RuntimeException("Wrong Confirm Password");
        }

        User user = new User();
        user.setUsername(register.getUsername());
        user.setEmail(register.getEmail());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setRole("user");
        user.setBalance(register.getWalletBalance());
        user.setCardCvv(register.getCvv());
        user.setCardExpiry(register.getCardExpiry());

        userRepository.save(user);
        return true;
    }

    public boolean exist(Register register) {
        return userRepository.findByUsername(register.getUsername()).isPresent()
                || userRepository.findByEmail(register.getEmail()).isPresent();
    }

    public User checkForUser(Login login) {
        Optional<User> optionalUser = userRepository.findByUsername(login.getUsername());
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) return null;

        return user;
    }

    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public void updateUserSettings(UUID userId, UserSettingsDto dto) {
        User user = userRepository.findById(userId).orElseThrow();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
                throw new RuntimeException("Passwords do not match");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void chargeWallet(String username, BigDecimal amount) {
        // 1. Намираме потребителя
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Потребителят не е намерен"));

        // 2. Осигуряваме начален баланс, ако е бил null (добра практика)
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        // 3. Изчисляваме новия баланс
        BigDecimal newBalance = user.getBalance().add(amount);
        user.setBalance(newBalance);

        // 4. Записваме промените

        userRepository.save(user);
    }
}