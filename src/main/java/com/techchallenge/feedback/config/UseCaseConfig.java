package com.techchallenge.feedback.config;

import com.techchallenge.feedback.adapter.output.dynamodb.persistence.FeedbackDynamoDb;
import com.techchallenge.feedback.adapter.output.sns.SnsAdminNotifier;
import com.techchallenge.feedback.adapter.output.time.SystemDateTimeProvider;
import com.techchallenge.feedback.application.port.input.GenerateWeeklyReportPort;
import com.techchallenge.feedback.application.port.input.NotifyUrgencyPort;
import com.techchallenge.feedback.application.port.input.ReceiveFeedbackPort;
import com.techchallenge.feedback.application.usecase.GenerateWeeklyReportUseCase;
import com.techchallenge.feedback.application.usecase.NotifyUrgencyUseCase;
import com.techchallenge.feedback.application.usecase.ReceiveFeedbackUseCase;

// composition root manual (sem Spring - o container so ia aumentar cold start pra 3 handlers
// pequenos). cada funcao monta so os adapters que usa.
public final class UseCaseConfig {

  private UseCaseConfig() {}

  public static ReceiveFeedbackPort receiveFeedbackPort() {
    return new ReceiveFeedbackUseCase(
        new FeedbackDynamoDb(AwsClientConfig.feedbackTable()),
        new SystemDateTimeProvider());
  }

  public static NotifyUrgencyPort notifyUrgencyPort() {
    return new NotifyUrgencyUseCase(adminNotifier());
  }

  public static GenerateWeeklyReportPort generateWeeklyReportPort() {
    return new GenerateWeeklyReportUseCase(
        new FeedbackDynamoDb(AwsClientConfig.feedbackTable()),
        adminNotifier(),
        new SystemDateTimeProvider());
  }

  private static SnsAdminNotifier adminNotifier() {
    return new SnsAdminNotifier(
        AwsClientConfig.snsClient(),
        EnvironmentConfig.require(EnvironmentConfig.ADMIN_TOPIC_ARN));
  }
}
