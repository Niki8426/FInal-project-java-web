package com.example.multimediHub.service;

import com.example.multimediHub.config.UserData;
import com.example.multimediHub.model.User;
import com.example.multimediHub.repository.UserRepository;
import com.example.multimediHub.web.dto.Login;
import com.example.multimediHub.web.dto.Register;
import com.example.multimediHub.web.dto.UserSettingsDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user=userRepository.findByUsername(username).stream().findFirst().orElseThrow(()->new UsernameNotFoundException("Username not found"));

        return new UserData(user.getId(),user.getUsername(),user.getPassword(),user.getRole());
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean registerUser( Register register) {

        // Създаване на нов User
        User user = new User();
        user.setUsername(register.getUsername());
        user.setEmail(register.getEmail());
        if (register.getPassword().equals(register.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(register.getPassword()));
        } else {
            throw new RuntimeException("Wrong Confirm Password");
        }
        user.setRole("user");
        user.setBalance(register.getWalletBalance());
        user.setCardCvv(register.getCvv());
        user.setCardExpiry(register.getCardExpiry());
        userRepository.save(user);

        return false;
    }


    public boolean exist(Register register) {
        return userRepository.findByUsername(register.getUsername()).isPresent()
                || userRepository.findByEmail(register.getEmail()).isPresent();
    }

    public User checkForUser(Login login) {

        Optional<User> optionalUser =
                userRepository.findByUsername(login.getUsername());

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            return null;
        }

        return user;
    }

    public User findUserById(UUID userId) {
       User user= userRepository.findById(userId).orElseThrow(()->new UsernameNotFoundException("User not found"));
        return user;
    }

    @Transactional
    public void updateUserSettings(UUID userId, UserSettingsDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        boolean wantsPasswordChange =
                dto.getNewPassword() != null && !dto.getNewPassword().isBlank();

        if (wantsPasswordChange) {

            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }

            if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
                throw new RuntimeException("Passwords do not match");
            }

            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }
    }
}
