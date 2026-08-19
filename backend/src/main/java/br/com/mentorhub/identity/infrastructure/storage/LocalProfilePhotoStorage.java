package br.com.mentorhub.identity.infrastructure.storage;

import br.com.mentorhub.identity.application.ProfilePhotoStorage;
import br.com.mentorhub.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalProfilePhotoStorage implements ProfilePhotoStorage {

    private final Path profilesDir;

    public LocalProfilePhotoStorage(
            @Value("${mentorhub.uploads.dir:./data/uploads}") String uploadsDir
    ) {
        this.profilesDir = Path.of(uploadsDir, "profiles").toAbsolutePath().normalize();
    }

    @Override
    public String save(UUID userId, byte[] content, String extension) {
        try {
            Files.createDirectories(profilesDir);
            deleteExisting(userId);
            Path target = profilesDir.resolve(userId + "." + extension);
            Files.write(target, content);
            return "/uploads/profiles/" + userId + "." + extension;
        } catch (IOException ex) {
            throw new BusinessException("PHOTO_STORAGE_ERROR", "Não foi possível salvar a foto");
        }
    }

    private void deleteExisting(UUID userId) throws IOException {
        if (!Files.isDirectory(profilesDir)) {
            return;
        }
        String prefix = userId + ".";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesDir, prefix + "*")) {
            for (Path existing : stream) {
                Files.deleteIfExists(existing);
            }
        }
    }
}
