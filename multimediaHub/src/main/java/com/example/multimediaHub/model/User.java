package com.example.multimediaHub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    // Wallet simulation
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    // Role as string
    @Column(nullable = false)
    private String role;

    @ManyToMany
    @JoinTable(
            name = "users_media",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "media_id")
    )
    private List<MediaItem> ownedMedia = new ArrayList<>();

    // Debit card simulation
    private String cardNumber;
    private String cardHolderName;
    private String cardExpiry;
    private String cardCvv;

    // -------- Празен конструктор --------
    public User() {}

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<MediaItem> getOwnedMedia() {
        return ownedMedia;
    }

    public void setOwnedMedia(List<MediaItem> ownedMedia) {
        this.ownedMedia = ownedMedia;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCardCvv() {
        return cardCvv;
    }

    public void setCardCvv(String cardCvv) {
        this.cardCvv = cardCvv;
    }
}