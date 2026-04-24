package com.edussafy.backend.priority.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.ClassmateNotificationResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionRequest;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionResponse;
import com.edussafy.backend.priority.dto.PriorityDtos.MaterialReactionSummary;
import com.edussafy.backend.priority.dto.PriorityDtos.RoleAccessResponse;
import com.edussafy.backend.priority.repository.PriorityApiRepository;
import com.edussafy.backend.priority.repository.PriorityP2Repository;
import com.edussafy.backend.priority.repository.PriorityP3Repository;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PriorityApiServiceTest {

    @Test
    void exposesLearnerRolePermissionsByDefault() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        given(repository.findDefaultUser()).willReturn(Optional.empty());
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                mock(PriorityP3Repository.class)
        );

        RoleAccessResponse response = service.currentRoleAccess();

        assertThat(response.role()).isEqualTo("learner");
        assertThat(response.permissions()).contains("dashboard:read", "profile:update", "quest:submit");
        assertThat(response.deniedRoutes()).contains("/admin");
    }

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

    @Test
    void togglesFavoriteReactionUsingHelpfulCode() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        given(repository.findDefaultUser()).willReturn(Optional.empty());
        given(p3Repository.toggleMaterialReaction(5L, 1L, "helpful")).willReturn(true);
        given(p3Repository.findMaterialReactionSummary(5L, 1L))
                .willReturn(Optional.of(new MaterialReactionSummary(5L, 2L, 0L, 1L, false, false, true)));
        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        MaterialReactionResponse response = service.toggleMaterialReaction(
                5L,
                new MaterialReactionRequest("favorite")
        );

        verify(p3Repository).toggleMaterialReaction(5L, 1L, "helpful");
        assertThat(response.item().materialId()).isEqualTo(5L);
        assertThat(response.item().type()).isEqualTo("favorite");
        assertThat(response.item().active()).isTrue();
        assertThat(response.item().likeCount()).isEqualTo(2L);
        assertThat(response.item().bookmarkCount()).isEqualTo(0L);
        assertThat(response.item().favoriteCount()).isEqualTo(1L);
        assertThat(response.item().favorited()).isTrue();
        assertThat(response.item().demo()).isFalse();
    }

    @Test
    void togglesLikeReactionConsistentlyReflectsStateTransitions() {
        PriorityApiRepository repository = mock(PriorityApiRepository.class);
        PriorityP3Repository p3Repository = mock(PriorityP3Repository.class);
        given(repository.findDefaultUser()).willReturn(Optional.empty());

        AtomicBoolean liked = new AtomicBoolean(false);
        when(p3Repository.toggleMaterialReaction(5L, 1L, "like"))
                .thenAnswer(invocation -> {
                    boolean next = !liked.get();
                    liked.set(next);
                    return next;
                });
        when(p3Repository.findMaterialReactionSummary(5L, 1L)).thenAnswer(invocation -> liked.get()
                ? Optional.of(new MaterialReactionSummary(5L, 1L, 0L, 0L, true, false, false))
                : Optional.of(new MaterialReactionSummary(5L, 0L, 0L, 0L, false, false, false)));

        PriorityApiService service = new PriorityApiService(
                repository,
                mock(PriorityP2Repository.class),
                p3Repository
        );

        MaterialReactionResponse likeOn = service.toggleMaterialReaction(5L, new MaterialReactionRequest("like"));
        MaterialReactionResponse likeOff = service.toggleMaterialReaction(5L, new MaterialReactionRequest("like"));

        assertThat(likeOn.item().active()).isTrue();
        assertThat(likeOn.item().likeCount()).isEqualTo(1L);
        assertThat(likeOn.item().liked()).isTrue();
        assertThat(likeOff.item().active()).isFalse();
        assertThat(likeOff.item().likeCount()).isEqualTo(0L);
        assertThat(likeOff.item().liked()).isFalse();
    }
}
