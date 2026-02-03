package com.example.gift_svc.repository;

import com.example.gift_svc.model.Gift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GiftRepository extends JpaRepository<Gift, UUID> {


    List<Gift> findByReceiverUsername(String receiverUsername);

}
