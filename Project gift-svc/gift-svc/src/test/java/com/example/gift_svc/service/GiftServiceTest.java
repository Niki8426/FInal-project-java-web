package com.example.gift_svc.service;

import com.example.gift_svc.model.Gift;
import com.example.gift_svc.repository.GiftRepository;
import com.example.gift_svc.web.dto.GiftCreateRequest;
import com.example.gift_svc.web.dto.GiftResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftServiceTest {

    @Mock
    private GiftRepository giftRepository;

    @InjectMocks
    private GiftService giftService;

    private Gift gift;
    private UUID giftId;

    @BeforeEach
    void setUp() {
        giftId = UUID.randomUUID();
        gift = new Gift("senderUser", "receiverUser", UUID.randomUUID());
        gift.setId(giftId);
    }

    @Test
    void createGift_Success() {
        GiftCreateRequest request = new GiftCreateRequest();
        request.setSenderUsername("senderUser");
        request.setReceiverUsername("receiverUser");
        request.setMediaId(UUID.randomUUID());

        when(giftRepository.save(any(Gift.class))).thenReturn(gift);

        GiftResponse response = giftService.createGift(request);

        assertNotNull(response);
        assertEquals("senderUser", response.getSenderUsername());
        verify(giftRepository, times(1)).save(any(Gift.class));
    }

    @Test
    void getReceivedGifts_ShouldReturnList() {
        when(giftRepository.findByReceiverUsername("receiverUser")).thenReturn(List.of(gift));

        List<GiftResponse> results = giftService.getReceivedGifts("receiverUser");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(giftRepository, times(1)).findByReceiverUsername("receiverUser");
    }

    @Test
    void deleteById_ThrowsException_WhenNotFound() {
        when(giftRepository.existsById(giftId)).thenReturn(false);

        // Тук тестваме изискването за Error Handling
        assertThrows(RuntimeException.class, () -> giftService.deleteById(giftId));
        verify(giftRepository, never()).deleteById(any());
    }
    @Test
    void deleteById_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        // Симулираме, че записът съществува, за да не хвърли Exception
        when(giftRepository.existsById(id)).thenReturn(true);

        // Act
        giftService.deleteById(id);

        // Assert
        // Проверяваме дали deleteById на репозиторито е извикано точно веднъж
        verify(giftRepository, times(1)).deleteById(id);
    }
    @Test
    void deleteOldGifts_ShouldInvokeRepository() {
        // Act
        giftService.deleteOldGifts();

        // Assert
        // Проверяваме дали методът за изтриване по дата е извикан
        verify(giftRepository, times(1)).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }

}