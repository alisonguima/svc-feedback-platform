package com.techchallenge.feedback.application.port.output;

import com.techchallenge.feedback.application.domain.Feedback;

import java.time.LocalDate;
import java.util.List;

public interface FeedbackPersistencePort {

  Feedback save(Feedback feedback);

  /** Avaliações enviadas no dia informado (fuso de Brasília). */
  List<Feedback> findBySentDay(LocalDate day);
}
