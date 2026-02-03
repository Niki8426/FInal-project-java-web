package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.LibraryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LibraryEntryRepository extends JpaRepository<LibraryEntry, UUID> {
    List<LibraryEntry> findAllByUserIdAndMediaItem_Type(
            UUID userId,
            String type
    );
}
