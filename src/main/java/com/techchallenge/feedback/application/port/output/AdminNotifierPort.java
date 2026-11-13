package com.techchallenge.feedback.application.port.output;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.WeeklyReport;

public interface AdminNotifierPort {

  void notifyUrgency(Feedback feedback);

  void sendWeeklyReport(WeeklyReport report);
}
