package com.example.gift_svc.repository;

import com.example.gift_svc.model.Gift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GiftRepository extends JpaRepository<Gift, UUID> {


    List<Gift> findByReceiverUsername(String receiverUsername);

    // Този метод ще генерира: DELETE FROM gifts WHERE created_at < ?
    void deleteByCreatedAtBefore(LocalDateTime expiryDate);

}
