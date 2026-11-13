package com.techchallenge.feedback.adapter.input.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.port.input.NotifyUrgencyPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.techchallenge.feedback.support.StreamImageFixture.criticalImage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyUrgencyHandlerTest {

  @Mock private NotifyUrgencyPort notifyUrgencyPort;

  private DynamodbEvent.DynamodbStreamRecord insert(StreamRecord streamRecord) {
    DynamodbEvent.DynamodbStreamRecord dynamoRecord = new DynamodbEvent.DynamodbStreamRecord();
    dynamoRecord.setEventName("INSERT");
    dynamoRecord.setDynamodb(streamRecord);
    return dynamoRecord;
  }

  @Test
  void notifiesEachCriticalRecordAndSkipsRecordsWithoutImage() {
    when(notifyUrgencyPort.notifyIfCritical(any())).thenReturn(true);
    DynamodbEvent event = new DynamodbEvent();
    event.setRecords(List.of(
        insert(new StreamRecord().withNewImage(criticalImage())),
        insert(new StreamRecord()),
        insert(null)));

    Map<String, Object> result = new NotifyUrgencyHandler(notifyUrgencyPort).handleRequest(event, null);

    assertThat(result).containsEntry("notificacoesEnviadas", 1L);
    ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
    verify(notifyUrgencyPort).notifyIfCritical(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo("abc-123");
  }

  @Test
  void defaultConstructorWiresDependenciesFromEnvironment() {
    HandlerEnvironment.configure();

    assertThatCode(NotifyUrgencyHandler::new).doesNotThrowAnyException();
  }
}
