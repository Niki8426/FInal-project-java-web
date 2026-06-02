package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// Тази анотация казва на Spring, че това е репозитори компонент, който управлява таблицата "users" в MySQL.
// Наследяваме JpaRepository, за да имаме наготово всички основни софтуерни операции като записване, обновяване и триене на потребители.
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // С този метод търсим потребител по неговото потребителско име.
    // Връщаме го в Optional обвивка, за да можем лесно да проверим дали такъв човек изобщо съществува в базата, преди да го логнем.
    Optional<User> findByUsername(String username);

    // Този метод търси потребител по неговия имейл адрес.
    // Използваме го главно при регистрация, за да засечем дали някой друг вече не е заел същия имейл в системата.
    Optional<User> findByEmail(String email);




}