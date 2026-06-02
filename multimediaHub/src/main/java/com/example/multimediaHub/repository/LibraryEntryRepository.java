package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.LibraryEntry;
import com.example.multimediaHub.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// Тази анотация казва на Spring, че този интерфейс е репозитори слой — тоест отговаря за директната софтуерна връзка с MySQL таблицата "library_entries".
// JpaRepository ни дава готови вградени методи (като save, delete, findById), без да пишем нито един SQL ред.
@Repository
public interface LibraryEntryRepository extends JpaRepository<LibraryEntry, UUID> {

    // С този метод правим интелигентно филтриране в базата данни по два критерия наведнъж.
    // Отива до таблицата с лични библиотеки и изтегля само тези записи, които хем съвпадат с ID-то на логнатия потребител (userId),
    // хем типът на закупения продукт съвпада с подадения MediaType (например да изкара само песните или само филмите на човека).
    List<LibraryEntry> findAllByUserIdAndMediaItemType(UUID userId, MediaType type);
}