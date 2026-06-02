package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.WallMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


// Тази анотация регистрира интерфейса като репозитори слой в Spring.
// Свързва се директно с таблицата "wall_messages" в MySQL и ни осигурява базовите CRUD операции наготово.
@Repository
public interface WallMessageRepository extends JpaRepository<WallMessage, UUID> {

    // С този метод изтегляме абсолютно всички съобщения от стената, подредени по време на създаване от най-старите към най-новите.
    // Използва се, за да се четат публикациите в естествен хронологичен ред (както в чат).
    List<WallMessage> findAllByOrderByCreatedAtAsc();

    // Този метод прави обратното — дърпа всички съобщения от стената, но ги сортира от най-новите към най-старите.
    // Удобен е, ако искаме най-пресните публикации да излизат най-отгоре на екрана (като във фийд на социална мрежа).
    List<WallMessage> findAllByOrderByCreatedAtDesc();
}