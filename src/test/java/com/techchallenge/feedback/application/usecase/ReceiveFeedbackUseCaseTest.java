package com.techchallenge.feedback.application.usecase;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import com.techchallenge.feedback.application.port.output.DateTimeProviderPort;
import com.techchallenge.feedback.application.port.output.FeedbackPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiveFeedbackUseCaseTest {

  @Mock private FeedbackPersistencePort feedbackPersistencePort;
  @Mock private DateTimeProviderPort dateTimeProviderPort;

  private ReceiveFeedbackUseCase useCase;
  private final Instant now = Instant.parse("2026-11-09T15:30:00Z");

  @BeforeEach
  void setUp() {
    useCase = new ReceiveFeedbackUseCase(feedbackPersistencePort, dateTimeProviderPort);
    when(dateTimeProviderPort.now()).thenReturn(now);
  }

  @Test
  void savesClassifiedFeedback() {
    when(feedbackPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Feedback result = useCase.receive("Aula confusa", 2);

    assertThat(result.getUrgency()).isEqualTo(UrgencyEnum.CRITICAL);
    assertThat(result.getSentAt()).isEqualTo(now);
  }

  @Test
  void doesNotSaveInvalidFeedback() {
    assertThatThrownBy(() -> useCase.receive("ok", 11)).isInstanceOf(InvalidFeedbackException.class);
    verifyNoInteractions(feedbackPersistencePort);
  }
}
