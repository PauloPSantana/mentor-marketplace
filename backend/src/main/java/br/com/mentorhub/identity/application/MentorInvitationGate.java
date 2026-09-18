package br.com.mentorhub.identity.application;

public interface MentorInvitationGate {

    void assertAcceptable(String token, String email);

    void accept(String token, java.util.UUID mentorUserId);
}
