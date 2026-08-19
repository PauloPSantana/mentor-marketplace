package br.com.mentorhub.mentorships.domain;

import java.math.BigDecimal;
import java.util.UUID;

public interface MentorshipPaymentGate {

    boolean isSettled(UUID mentorshipId, BigDecimal amountDue);
}
