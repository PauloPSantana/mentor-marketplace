package br.com.mentorhub.mentorships.application;

import java.math.BigDecimal;

public record MentorshipProgress(
        BigDecimal amountDue,
        String currency,
        boolean paymentRequired,
        boolean paymentSettled,
        int requiredSessions,
        int completedSessions,
        int scheduledSessions,
        boolean canComplete
) {
}
