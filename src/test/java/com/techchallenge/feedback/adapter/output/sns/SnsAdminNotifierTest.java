package com.techchallenge.feedback.adapter.output.sns;

import com.techchallenge.feedback.application.domain.WeeklyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import static com.techchallenge.feedback.support.FeedbackFixture.critical;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SnsAdminNotifierTest {

  private static final String TOPIC = "arn:aws:sns:us-east-1:123456789012:admin";

  @Mock private SnsClient snsClient;

  private SnsAdminNotifier notifier;

  @BeforeEach
  void setUp() {
    notifier = new SnsAdminNotifier(snsClient, TOPIC);
  }

  @SuppressWarnings("unchecked")
  private PublishRequest capturePublish() {
    ArgumentCaptor<Consumer<PublishRequest.Builder>> captor = ArgumentCaptor.forClass(Consumer.class);
    verify(snsClient).publish(captor.capture());
    PublishRequest.Builder builder = PublishRequest.builder();
    captor.getValue().accept(builder);
    return builder.build();
  }

  @Test
  void publishesUrgencyEmailToAdminTopic() {
    notifier.notifyUrgency(critical());

    PublishRequest request = capturePublish();
    assertThat(request.topicArn()).isEqualTo(TOPIC);
    assertThat(request.subject()).startsWith("[URGENTE]");
    assertThat(request.message()).contains("Aula confusa");
  }

  @Test
  void publishesWeeklyReportToAdminTopic() {
    notifier.sendWeeklyReport(WeeklyReport.of(List.of(LocalDate.of(2026, 11, 8)), List.of()));

    PublishRequest request = capturePublish();
    assertThat(request.topicArn()).isEqualTo(TOPIC);
    assertThat(request.subject()).startsWith("Relatorio semanal");
  }
}
