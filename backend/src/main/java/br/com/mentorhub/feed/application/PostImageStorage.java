package br.com.mentorhub.feed.application;

import br.com.mentorhub.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class PostImageStorage {

    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private final Path imagesDir;

    public PostImageStorage(
            @Value("${mentorhub.uploads.dir:./data/uploads}") String uploadsDir
    ) {
        this.imagesDir = Path.of(uploadsDir, "posts").toAbsolutePath().normalize();
    }

    public String save(byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessException("INVALID_IMAGE", "Selecione uma imagem para enviar");
        }
        if (content.length > MAX_BYTES) {
            throw new BusinessException("INVALID_IMAGE", "A imagem deve ter no máximo 5 MB");
        }
        String extension = detectExtension(content);
        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(imagesDir);
            Files.write(imagesDir.resolve(filename), content);
            return "/uploads/posts/" + filename;
        } catch (IOException ex) {
            throw new BusinessException("IMAGE_STORAGE_ERROR", "Não foi possível salvar a imagem");
        }
    }

    private static String detectExtension(byte[] content) {
        if (content.length >= 3 && content[0] == (byte) 0xFF && content[1] == (byte) 0xD8 && content[2] == (byte) 0xFF) {
            return "jpg";
        }
        if (content.length >= 8 && content[0] == (byte) 0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47) {
            return "png";
        }
        if (content.length >= 12 && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F'
                && content[8] == 'W' && content[9] == 'E' && content[10] == 'B' && content[11] == 'P') {
            return "webp";
        }
        if (content.length >= 4 && content[0] == 'G' && content[1] == 'I' && content[2] == 'F' && content[3] == '8') {
            return "gif";
        }
        throw new BusinessException("INVALID_IMAGE", "Envie uma imagem JPG, PNG, WEBP ou GIF");
    }
}
