package com.techchallenge.feedback.config;

import com.techchallenge.feedback.adapter.output.dynamodb.model.FeedbackItem;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sns.SnsClient;

// UrlConnectionHttpClient em vez do Apache/Netty - pacote menor, cold start melhor
public final class AwsClientConfig {

  private AwsClientConfig() {}

  public static DynamoDbTable<FeedbackItem> feedbackTable() {
    DynamoDbClient dynamoDb = DynamoDbClient.builder()
        .httpClient(UrlConnectionHttpClient.create())
        .build();
    return DynamoDbEnhancedClient.builder()
        .dynamoDbClient(dynamoDb)
        .build()
        .table(EnvironmentConfig.require(EnvironmentConfig.TABLE_NAME), TableSchema.fromBean(FeedbackItem.class));
  }

  public static SnsClient snsClient() {
    return SnsClient.builder()
        .httpClient(UrlConnectionHttpClient.create())
        .build();
  }
}
