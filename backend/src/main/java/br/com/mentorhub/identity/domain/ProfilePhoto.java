package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;

public final class ProfilePhoto {

    public static final int MAX_BYTES = 2 * 1024 * 1024;

    private ProfilePhoto() {
    }

    public static String extensionOf(byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessException("INVALID_PHOTO", "Selecione uma foto para enviar");
        }
        if (content.length > MAX_BYTES) {
            throw new BusinessException("INVALID_PHOTO", "A foto deve ter no máximo 2 MB");
        }
        if (isJpeg(content)) {
            return "jpg";
        }
        if (isPng(content)) {
            return "png";
        }
        if (isWebp(content)) {
            return "webp";
        }
        throw new BusinessException("INVALID_PHOTO", "Envie uma imagem JPG, PNG ou WEBP");
    }

    private static boolean isJpeg(byte[] content) {
        return content.length >= 3
                && content[0] == (byte) 0xFF
                && content[1] == (byte) 0xD8
                && content[2] == (byte) 0xFF;
    }

    private static boolean isPng(byte[] content) {
        return content.length >= 8
                && content[0] == (byte) 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47
                && content[4] == 0x0D
                && content[5] == 0x0A
                && content[6] == 0x1A
                && content[7] == 0x0A;
    }

    private static boolean isWebp(byte[] content) {
        if (content.length < 12) {
            return false;
        }
        return content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P';
    }
}
