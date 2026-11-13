package com.techchallenge.feedback.application.port.input;

import com.techchallenge.feedback.application.domain.Feedback;

public interface NotifyUrgencyPort {

  // false quando a avaliacao nao e critica (nada e enviado)
  boolean notifyIfCritical(Feedback feedback);
}
