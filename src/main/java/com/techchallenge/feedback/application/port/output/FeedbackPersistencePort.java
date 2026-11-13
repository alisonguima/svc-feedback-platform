package com.techchallenge.feedback.application.port.output;

import com.techchallenge.feedback.application.domain.Feedback;

import java.time.LocalDate;
import java.util.List;

public interface FeedbackPersistencePort {

  Feedback save(Feedback feedback);

  // dia calculado no fuso de Brasilia
  List<Feedback> findBySentDay(LocalDate day);
}
