package com.example.multimediHub.repository;

import com.example.multimediHub.model.MediaItem;
import com.example.multimediHub.model.MediaType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaItemRepository extends JpaRepository<MediaItem, UUID> {

    Optional<MediaItem> findFirstByCurrentTrue();

    List<MediaItem> findAllByTypeOrderByYearDesc(MediaType type);

    @Query("""
        SELECT m
        FROM MediaItem m
        WHERE m.type = :type
          AND m.id NOT IN :ownedIds
        ORDER BY m.year DESC
    """)
    List<MediaItem> findMarketItems(
            @Param("type") MediaType type,
            @Param("ownedIds") List<UUID> ownedIds
    );
}
