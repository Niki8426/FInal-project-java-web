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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// @Service: Регистрира класа като бизнес компонент (сървис) в Spring контекста.
// "implements UserDetailsService": Казва на Spring Security, че този сървис ще управлява автентикацията
// и ще предоставя нужните данни за потребителите при логин.
@Service
public class UserService implements UserDetailsService {

    // Финални полета за репозитори слоя и енкодъра за пароли.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // @Autowired: Инжектира автоматично UserRepository и PasswordEncoder през конструктора
    // за сигурна и чиста работа с базата данни и криптирането.
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // @Override loadUserByUsername: Основен метод от интерфейса UserDetailsService на Spring Security.
    // Извиква се автоматично по време на логин процес, за да намери потребителя по неговия username.
    // Вместо стандартния User обект, методът връща къстъм структурата UserData, за да запази UUID-то в сесията.
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

    // @Transactional: Отваря транзакция към базата. Ако потвърждението на паролата не съвпадне или записът се срине,
    // базата се връща в начално състояние (rollback) и няма да се запише дефектен потребител.
    // Методът приема данните от формата за регистрация (Register DTO), хешира паролата и записва новия акаунт.
    @Transactional
    public boolean registerUser(Register register) {
        if (!register.getPassword().equals(register.getConfirmPassword())) {
            throw new RuntimeException("Wrong Confirm Password");
        }

        User user = new User();
        user.setUsername(register.getUsername());
        user.setEmail(register.getEmail());
        // Хешираме сигурно паролата с BCrypt преди запис в базата данни
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setRole("user");
        user.setBalance(register.getWalletBalance());
        user.setCardCvv(register.getCvv());
        user.setCardExpiry(register.getCardExpiry());

        userRepository.save(user);
        return true;
    }

    // Метод exist: Проверява бързо дали в базата данни вече съществува потребител със същия username
    // ИЛИ със същия имейл адрес. Използва се като софтуерна защита преди нова регистрация.
    public boolean exist(Register register) {
        return userRepository.findByUsername(register.getUsername()).isPresent()
                || userRepository.findByEmail(register.getEmail()).isPresent();
    }

    // Метод checkForUser: Ръчна проверка на потребителски данни за вход (Login DTO).
    // Намира акаунта по username и сравнява въведената чиста парола с криптирания хеш от базата чрез passwordEncoder.matches().
    // Ако всичко е точно, връща обекта на потребителя, иначе връща null.
    public User checkForUser(Login login) {
        Optional<User> optionalUser = userRepository.findByUsername(login.getUsername());
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) return null;

        return user;
    }

    // Метод findUserById: Извлича пълния потребителски обект по неговото уникално UUID.
    // Ако ID-то не съществува в базата, хвърля UsernameNotFoundException грешка.
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // @Transactional: Подсигурява сигурната редакция на потребителския профил.
    // Методът updateUserSettings обновява името и имейла, а ако потребителят е попълнил полето за нова парола,
    // първо валидира старата му парола, проверява съвпадението на новата и я записва криптирана в MySQL.
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

    // Метод findByUsername: Търси потребител по неговия username и връща резултата,
    // опакован в Optional, за да се избегнат NullPointerException грешки в извикващия софтуер.
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // @Transactional: Гарантира, че операцията по зареждане на дигиталния портфейл (Wallet) е транзакционно защитена.
    // Намира потребителя, подсигурява баланса срещу null и добавя подадената сума (amount) към текущите пари.
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

    // Метод getAllUsers: Изтегля абсолютно всички регистрирани потребители от базата данни.
    // Използва се предимно за администраторски панели.
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // @Transactional: Отговаря за сигурното начисляване на бонус суми от администратор към конкретен потребител по неговото ID.
    // Методът извършва математическо добавяне над BigDecimal баланса и записва промяната в базата.
    @Transactional
    public void addBonusBalance(UUID userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Потребителят не е намерен!"));

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
    }

    // @Transactional: Използва се за пълно изтриване на потребителски профил от системата по неговото UUID.
    // Тъй като е транзакционен, методът гарантира, че изтриването на записа от таблицата "users" (и свързаните релации) ще мине успешно.
    @Transactional
    public void deleteById(UUID userId) {
        userRepository.deleteById(userId);
    }
}