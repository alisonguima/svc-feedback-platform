package com.techchallenge.feedback.adapter.output.dynamodb.persistence;

import com.techchallenge.feedback.adapter.output.dynamodb.mapper.FeedbackItemMapper;
import com.techchallenge.feedback.adapter.output.dynamodb.model.FeedbackItem;
import com.techchallenge.feedback.application.domain.Feedback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.util.List;

import static com.techchallenge.feedback.support.FeedbackFixture.critical;
import static com.techchallenge.feedback.support.FeedbackFixture.feedback;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackDynamoDbTest {

  @Mock private DynamoDbTable<FeedbackItem> table;
  @Mock private DynamoDbIndex<FeedbackItem> dayIndex;
  @InjectMocks private FeedbackDynamoDb persistence;

  @Test
  void savesItemAndReturnsDomain() {
    Feedback feedback = critical();

    assertThat(persistence.save(feedback)).isSameAs(feedback);

    ArgumentCaptor<FeedbackItem> captor = ArgumentCaptor.forClass(FeedbackItem.class);
    verify(table).putItem(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo("abc-123");
  }

  @Test
  void queriesDayIndexAcrossAllPages() {
    FeedbackItem first = FeedbackItemMapper.toItem(feedback("a", 2, "2026-11-09"));
    FeedbackItem second = FeedbackItemMapper.toItem(feedback("b", 9, "2026-11-09"));
    SdkIterable<Page<FeedbackItem>> pages = () -> List.of(Page.create(List.of(first)), Page.create(List.of(second)))
        .iterator();
    when(table.index(FeedbackItem.DAY_INDEX)).thenReturn(dayIndex);
    when(dayIndex.query(any(QueryConditional.class))).thenReturn(pages);

    List<Feedback> result = persistence.findBySentDay(LocalDate.of(2026, 11, 9));

    assertThat(result).extracting(Feedback::getId).containsExactly("a", "b");
  }
}
