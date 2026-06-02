package com.example.multimediaHub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Тази анотация казва на Hibernate, че този клас е специален модел, който ще се мапира към базата данни.
// @Table пък задава как точно да се казва реалната таблица в MySQL (в случая "users").
@Entity
@Table(name = "users")
public class User {

    // @Id казва, че това поле е главният ключ на таблицата (Primary Key).
    // GenerationType.UUID кара софтуера автоматично да генерира уникални 128-битови кодове при нов потребител.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // @Column задава ограничения на ниво база данни: nullable=false означава, че полето е задължително (NOT NULL),
    // а unique=true гарантира, че не може двама души да се регистрират с едно и също потребителско име.
    @Column(nullable = false, unique = true)
    private String username;

    // Казва на базата данни, че паролата е задължително поле и не може да се остави празна.
    @Column(nullable = false)
    private String password;

    // Прави имейл адреса задължителен за попълване и блокира възможността за дублирани имейли в системата.
    @Column(nullable = false, unique = true)
    private String email;

    // Wallet simulation
    // Маркира баланса като задължително софтуерно поле, като по подразбиране го занулява при регистрация.
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    // Ролята също се записва като задължителен обикновен стринг (низ) в базата, за да се чете от Spring Security.
    @Column(nullable = false)
    private String role;

    // @ManyToMany указва релация "много към много" (един потребител има много медии, и една медия се притежава от много потребители).
    // @JoinTable създава трета, междинна свързваща таблица "users_media", която съдържа само колоните "user_id" и "media_id".
    @ManyToMany
    @JoinTable(
            name = "users_media",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "media_id")
    )
    private List<MediaItem> ownedMedia = new ArrayList<>();

    // Debit card simulation
    // Тези четири полета се записват като стандартни VARCHAR колони в MySQL без допълнителни софтуерни ограничения.
    private String cardNumber;
    private String cardHolderName;
    private String cardExpiry;
    private String cardCvv;

    // Този празен конструктор е тук само и единствено заради Hibernate, за да може
    // софтуерът да извлича записи от таблицата с потребители и да ги мапира като Java обекти.
    public User() {}

    // Този пълен конструктор ни позволява да сглобим накуп нов потребител с всичките му
    // детайли, карти, роли и закупена медия наведнъж, когато тестваме или правим софтуерни сийдове.
    public User(UUID id, String username, String password, String email, BigDecimal balance, String role, List<MediaItem> ownedMedia, String cardNumber, String cardHolderName, String cardExpiry, String cardCvv) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.balance = balance;
        this.role = role;
        this.ownedMedia = ownedMedia;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardExpiry = cardExpiry;
        this.cardCvv = cardCvv;
    }

    // Връща уникалния UUID ключ на конкретния потребител.
    public UUID getId() {
        return id;
    }

    // Ръчно задаване на уникалното ID на потребителския профил.
    public void setId(UUID id) {
        this.id = id;
    }

    // Връща потребителското име за логин или за показване по уеб страниците.
    public String getUsername() {
        return username;
    }

    // Задава или променя потребителското име в профила.
    public void setUsername(String username) {
        this.username = username;
    }

    // Извлича криптирания пасуърд на потребителя за нуждите на сигурността.
    public String getPassword() {
        return password;
    }

    // Налива паролата (която преди това трябва да е минала през BCrypt) в обекта.
    public void setPassword(String password) {
        this.password = password;
    }

    // Връща регистрирания имейл адрес на човека.
    public String getEmail() {
        return email;
    }

    // Задава нов или променя текущия имейл адрес.
    public void setEmail(String email) {
        this.email = email;
    }

    // Връща парите в портфейла на потребителя като прецизен BigDecimal.
    public BigDecimal getBalance() {
        return balance;
    }

    // Сменя или актуализира сумата, с която разполага потребителят в баланса си.
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    // Връща правата на потребителя (например ROLE_USER или ROLE_ADMIN).
    public String getRole() {
        return role;
    }

    // Задава софтуерната роля, за да знае Spring Security какви права има този профил.
    public void setRole(String role) {
        this.role = role;
    }

    // Връща списъка от всички филми и песни, които потребителят вече си е купил и притежава.
    public List<MediaItem> getOwnedMedia() {
        return ownedMedia;
    }

    // Пренаписва или залага цялата колекция от закупени продукти на потребителя.
    public void setOwnedMedia(List<MediaItem> ownedMedia) {
        this.ownedMedia = ownedMedia;
    }

    // Извлича симулирания номер на банковата карта за плащания.
    public String getCardNumber() {
        return cardNumber;
    }

    // Записва 16-цифрения номер на дебитната карта в профила.
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    // Връща името на картодържателя, изписано на пластмасата.
    public String getCardHolderName() {
        return cardHolderName;
    }

    // Записва имената на собственика на картата.
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    // Връща датата на валидност на симулираната банкова карта.
    public String getCardExpiry() {
        return cardExpiry;
    }

    // Записва докога е валидна картата на потребителя.
    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    // Извлича трицифрения Cvv сигурностен код на картата.
    public String getCardCvv() {
        return cardCvv;
    }

    // Записва тайния код на картата за потвърждение на плащанията.
    public void setCardCvv(String cardCvv) {
        this.cardCvv = cardCvv;
    }
}