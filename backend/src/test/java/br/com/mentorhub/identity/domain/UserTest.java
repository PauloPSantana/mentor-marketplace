package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void shouldRegisterMentorAsActive() {
        User user = User.register("Ana Mentor", "ana@email.com", "hash", UserRole.MENTOR);

        assertEquals(UserRole.MENTOR, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("ana@email.com", user.getEmail());
        assertTrue(user.isActive());
    }

    @Test
    void shouldRegisterInstitution() {
        User user = User.register("Instituto", "gestor@email.com", "hash", UserRole.INSTITUTION);
        assertEquals(UserRole.INSTITUTION, user.getRole());
    }

    @Test
    void shouldRejectPublicAdminRegistration() {
        assertThrows(
                BusinessException.class,
                () -> User.register("Admin", "admin@email.com", "hash", UserRole.ADMIN)
        );
    }

    @Test
    void shouldNormalizeEmail() {
        User user = User.register("João", "  Joao@Email.COM ", "hash", UserRole.MENTEE);
        assertEquals("joao@email.com", user.getEmail());
    }

    @Test
    void shouldRenameUser() {
        User user = User.register("Paulo Teste", "paulo@email.com", "hash", UserRole.MENTOR);

        user.rename("  Paulo Santana  ");

        assertEquals("Paulo Santana", user.getName());
    }

    @Test
    void shouldUpdatePhoto() {
        User user = User.register("Paulo Teste", "paulo@email.com", "hash", UserRole.MENTEE);

        user.updatePhoto(" /uploads/profiles/paulo.jpg ");

        assertEquals("/uploads/profiles/paulo.jpg", user.getPhotoUrl());
    }

    @Test
    void shouldRejectBlankNameOnRename() {
        User user = User.register("Paulo Teste", "paulo@email.com", "hash", UserRole.MENTEE);

        BusinessException error = assertThrows(BusinessException.class, () -> user.rename("   "));

        assertEquals("INVALID_NAME", error.getCode());
        assertEquals("Paulo Teste", user.getName());
    }
}
