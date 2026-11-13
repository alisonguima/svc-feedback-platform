package com.techchallenge.feedback.adapter.output.sns;

import com.techchallenge.feedback.adapter.output.sns.formatter.AdminMessage;
import com.techchallenge.feedback.adapter.output.sns.formatter.AdminMessageFormatter;
import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.WeeklyReport;
import com.techchallenge.feedback.application.port.output.AdminNotifierPort;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sns.SnsClient;

@RequiredArgsConstructor
public class SnsAdminNotifier implements AdminNotifierPort {

  private final SnsClient snsClient;
  private final String topicArn;

  @Override
  public void notifyUrgency(Feedback feedback) {
    publish(AdminMessageFormatter.urgency(feedback));
  }

  @Override
  public void sendWeeklyReport(WeeklyReport report) {
    publish(AdminMessageFormatter.weeklyReport(report));
  }

  private void publish(AdminMessage message) {
    snsClient.publish(request -> request
        .topicArn(topicArn)
        .subject(message.subject())
        .message(message.body()));
  }
}
