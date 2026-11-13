package com.techchallenge.feedback.adapter.output.dynamodb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

// nomes dos atributos em portugues - o filtro do stream no template.yaml depende deles
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class FeedbackItem {

  public static final String DAY_INDEX = "dia-index";

  @Getter(onMethod_ = @DynamoDbPartitionKey)
  private String id;

  @Getter
  private String descricao;

  @Getter
  private Integer nota;

  @Getter
  private String urgencia;

  @Getter(onMethod_ = @DynamoDbSecondarySortKey(indexNames = DAY_INDEX))
  private String dataEnvio;

  @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = DAY_INDEX))
  private String dia;
}
