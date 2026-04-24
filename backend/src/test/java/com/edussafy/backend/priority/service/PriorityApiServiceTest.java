package com.edussafy.backend.priority.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.edussafy.backend.priority.repository.PriorityP2Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository;
import org.junit.jupiter.api.Test;

class PriorityApiServiceTest {

    @Test
    void createsClassmateNotificationWithDefaults() {
        PriorityApiService service = new PriorityApiService(
                mock(PriorityApiRepository.class),
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        ClassmateNotificationResponse response = service.createClassmateNotification(
                7L,
                new ClassmateNotificationRequest(null, null)
        );

        assertThat(response.item().id()).isEqualTo(900_007L);
        assertThat(response.item().recipientUserId()).isEqualTo(7L);
        assertThat(response.item().type()).isEqualTo("contact_request");
        assertThat(response.item().message()).isEqualTo("Let's study together!");
        assertThat(response.item().status()).isEqualTo("sent");
        assertThat(response.item().notification()).isNotNull();
        assertThat(response.item().notification().id()).isEqualTo(900_007L);
        assertThat(response.item().notification().body()).isEqualTo("Let's study together!");
        assertThat(response.item().demo()).isTrue();
    }
}
