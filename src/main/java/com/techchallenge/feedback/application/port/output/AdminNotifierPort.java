package com.techchallenge.feedback.application.port.output;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.WeeklyReport;

/** Canal de comunicação com os administradores (e-mail, no adapter atual). */
public interface AdminNotifierPort {

  void notifyUrgency(Feedback feedback);

  void sendWeeklyReport(WeeklyReport report);
}
