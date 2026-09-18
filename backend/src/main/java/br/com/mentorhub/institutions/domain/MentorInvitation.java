package br.com.mentorhub.institutions.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class MentorInvitation {

    private static final Duration TTL = Duration.ofHours(72);

    private final UUID id;
    private final UUID institutionId;
    private final UUID invitedByUserId;
    private final String name;
    private final String email;
    private final String specialty;
    private final String program;
    private String token;
    private MentorInvitationStatus status;
    private Instant expiresAt;
    private UUID acceptedUserId;
    private Instant acceptedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private MentorInvitation(
            UUID id,
            UUID institutionId,
            UUID invitedByUserId,
            String name,
            String email,
            String specialty,
            String program,
            String token,
            MentorInvitationStatus status,
            Instant expiresAt,
            UUID acceptedUserId,
            Instant acceptedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.institutionId = Objects.requireNonNull(institutionId);
        this.invitedByUserId = Objects.requireNonNull(invitedByUserId);
        this.name = requireName(name);
        this.email = requireEmail(email);
        this.specialty = blankToNull(specialty, 160);
        this.program = blankToNull(program, 160);
        this.token = Objects.requireNonNull(token);
        this.status = Objects.requireNonNull(status);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.acceptedUserId = acceptedUserId;
        this.acceptedAt = acceptedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MentorInvitation create(
            UUID institutionId,
            UUID invitedByUserId,
            String name,
            String email,
            String specialty,
            String program
    ) {
        Instant now = Instant.now();
        return new MentorInvitation(
                UUID.randomUUID(),
                institutionId,
                invitedByUserId,
                name,
                email,
                specialty,
                program,
                newToken(),
                MentorInvitationStatus.PENDING,
                now.plus(TTL),
                null,
                null,
                now,
                now
        );
    }

    public static MentorInvitation restore(
            UUID id,
            UUID institutionId,
            UUID invitedByUserId,
            String name,
            String email,
            String specialty,
            String program,
            String token,
            MentorInvitationStatus status,
            Instant expiresAt,
            UUID acceptedUserId,
            Instant acceptedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new MentorInvitation(
                id, institutionId, invitedByUserId, name, email, specialty, program, token,
                status, expiresAt, acceptedUserId, acceptedAt, createdAt, updatedAt
        );
    }

    public void assertAcceptable(String candidateEmail) {
        if (status != MentorInvitationStatus.PENDING) {
            throw new BusinessException("INVITATION_NOT_PENDING", "Este convite já foi usado ou cancelado");
        }
        if (!expiresAt.isAfter(Instant.now())) {
            throw new BusinessException("INVITATION_EXPIRED", "Este convite expirou. Peça um novo à instituição");
        }
        if (!email.equals(requireEmail(candidateEmail))) {
            throw new BusinessException("INVITATION_EMAIL_MISMATCH", "Use o mesmo e-mail do convite para se cadastrar");
        }
    }

    public void accept(UUID mentorUserId) {
        assertAcceptable(email);
        Instant now = Instant.now();
        this.status = MentorInvitationStatus.ACCEPTED;
        this.acceptedUserId = Objects.requireNonNull(mentorUserId);
        this.acceptedAt = now;
        this.updatedAt = now;
    }

    public void cancel() {
        assertRemovable();
        this.status = MentorInvitationStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void assertRemovable() {
        if (status == MentorInvitationStatus.ACCEPTED) {
            throw new BusinessException("INVITATION_ACCEPTED", "Convite aceito não pode ser removido");
        }
    }

    public void refreshForResend() {
        if (status == MentorInvitationStatus.ACCEPTED) {
            throw new BusinessException("INVITATION_NOT_PENDING", "Convite aceito não pode ser reenviado");
        }
        this.status = MentorInvitationStatus.PENDING;
        this.token = newToken();
        this.expiresAt = Instant.now().plus(TTL);
        this.updatedAt = Instant.now();
    }

    public MentorInvitationStatus displayedStatus() {
        if (status == MentorInvitationStatus.PENDING && !expiresAt.isAfter(Instant.now())) {
            return MentorInvitationStatus.EXPIRED;
        }
        return status;
    }

    private static String newToken() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        StringBuilder token = new StringBuilder(32);
        for (byte value : bytes) {
            token.append(String.format("%02x", value));
        }
        return token.toString();
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public UUID getInvitedByUserId() {
        return invitedByUserId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getProgram() {
        return program;
    }

    public String getToken() {
        return token;
    }

    public MentorInvitationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UUID getAcceptedUserId() {
        return acceptedUserId;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("INVALID_INVITATION", "Nome do mentor é obrigatório");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 120) {
            throw new BusinessException("INVALID_INVITATION", "Nome do mentor deve ter no máximo 120 caracteres");
        }
        return trimmed;
    }

    private static String requireEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("INVALID_INVITATION", "E-mail do mentor é obrigatório");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new BusinessException("INVALID_INVITATION", "Campo deve ter no máximo " + max + " caracteres");
        }
        return trimmed;
    }
}
