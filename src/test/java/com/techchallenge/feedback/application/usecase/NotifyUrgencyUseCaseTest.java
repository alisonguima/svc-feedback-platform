package com.techchallenge.feedback.application.usecase;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.port.output.AdminNotifierPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.techchallenge.feedback.support.FeedbackFixture.critical;
import static com.techchallenge.feedback.support.FeedbackFixture.feedback;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotifyUrgencyUseCaseTest {

  @Mock private AdminNotifierPort adminNotifierPort;
  @InjectMocks private NotifyUrgencyUseCase useCase;

  @Test
  void notifiesCriticalFeedback() {
    Feedback feedback = critical();

    assertThat(useCase.notifyIfCritical(feedback)).isTrue();
    verify(adminNotifierPort).notifyUrgency(feedback);
  }

  @Test
  void ignoresNonCriticalFeedback() {
    assertThat(useCase.notifyIfCritical(feedback("x", 9, "2026-11-09"))).isFalse();
    verifyNoInteractions(adminNotifierPort);
  }
}
