package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfilePhotoTest {

    @Test
    void shouldDetectJpeg() {
        byte[] jpeg = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        assertEquals("jpg", ProfilePhoto.extensionOf(jpeg));
    }

    @Test
    void shouldDetectPng() {
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertEquals("png", ProfilePhoto.extensionOf(png));
    }

    @Test
    void shouldRejectUnknownFormat() {
        BusinessException error = assertThrows(BusinessException.class, () -> ProfilePhoto.extensionOf(new byte[] {1, 2, 3}));
        assertEquals("INVALID_PHOTO", error.getCode());
    }

    @Test
    void shouldRejectEmptyFile() {
        BusinessException error = assertThrows(BusinessException.class, () -> ProfilePhoto.extensionOf(new byte[0]));
        assertEquals("INVALID_PHOTO", error.getCode());
    }
}
