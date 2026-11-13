package com.techchallenge.feedback.adapter.output.dynamodb.persistence;

import com.techchallenge.feedback.adapter.output.dynamodb.mapper.FeedbackItemMapper;
import com.techchallenge.feedback.adapter.output.dynamodb.model.FeedbackItem;
import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.port.output.FeedbackPersistencePort;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class FeedbackDynamoDb implements FeedbackPersistencePort {

  private final DynamoDbTable<FeedbackItem> table;

  @Override
  public Feedback save(Feedback feedback) {
    table.putItem(FeedbackItemMapper.toItem(feedback));
    return feedback;
  }

  /** Query no índice dia-index; a paginação é tratada pelo SDK ao percorrer as páginas. */
  @Override
  public List<Feedback> findBySentDay(LocalDate day) {
    return table.index(FeedbackItem.DAY_INDEX)
        .query(QueryConditional.keyEqualTo(key -> key.partitionValue(day.toString())))
        .stream()
        .flatMap(page -> page.items().stream())
        .map(FeedbackItemMapper::toDomain)
        .toList();
  }
}
