package com.example.multimediHub.repository;

import com.example.multimediHub.model.LibraryEntry;
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
