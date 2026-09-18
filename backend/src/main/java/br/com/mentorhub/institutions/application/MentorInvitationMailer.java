package br.com.mentorhub.institutions.application;

public interface MentorInvitationMailer {

    boolean isEnabled();

    boolean sendInvite(String to, String mentorName, String institutionName, String acceptUrl);
}
