package br.com.mentorhub.announcements.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "group_announcement_recipients")
public class AnnouncementRecipientJpaEntity {

    @Id
    private UUID id;

    @Column(name = "announcement_id", nullable = false)
    private UUID announcementId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected AnnouncementRecipientJpaEntity() {
    }

    public AnnouncementRecipientJpaEntity(UUID id, UUID announcementId, UUID userId) {
        this.id = id;
        this.announcementId = announcementId;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(UUID announcementId) {
        this.announcementId = announcementId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
