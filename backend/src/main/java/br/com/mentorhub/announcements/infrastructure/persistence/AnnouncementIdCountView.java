package br.com.mentorhub.announcements.infrastructure.persistence;

import java.util.UUID;

public interface AnnouncementIdCountView {

    UUID getAnnouncementId();

    Long getCnt();
}
