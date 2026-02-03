package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, UUID> {

    List<UserMessage> findByReceiverAndDeletedFalseOrderByCreatedAtDesc(User receiver);
}
