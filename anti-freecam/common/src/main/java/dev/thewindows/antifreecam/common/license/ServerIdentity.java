package dev.thewindows.antifreecam.common.license;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ServerIdentity {

    private final Path identityFile;
    private String cachedId;

    public ServerIdentity(Path dataFolder) {
        this.identityFile = dataFolder.resolve("server-uuid.dat");
    }

    public synchronized String getOrCreate() throws IOException {
        if (cachedId != null) return cachedId;

        if (Files.exists(identityFile)) {
            cachedId = Files.readString(identityFile, StandardCharsets.UTF_8).strip();
        } else {
            cachedId = UUID.randomUUID().toString();
            Files.createDirectories(identityFile.getParent());
            Files.writeString(identityFile, cachedId, StandardCharsets.UTF_8);
        }
        return cachedId;
    }
}
