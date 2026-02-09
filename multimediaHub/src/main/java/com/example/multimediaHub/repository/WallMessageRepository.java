package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.WallMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WallMessageRepository extends JpaRepository<WallMessage, UUID> {

    // Използваме Ascending (възходящ ред), за да може новите да са в края на списъка
    List<WallMessage> findAllByOrderByCreatedAtAsc();

    List<WallMessage> findAllByOrderByCreatedAtDesc();
}
