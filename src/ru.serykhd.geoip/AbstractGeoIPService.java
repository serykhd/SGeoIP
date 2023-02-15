package ru.serykhd.geoip;

import lombok.NonNull;
import ru.serykhd.logger.InternalLogger;
import ru.serykhd.logger.impl.InternalLoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;

public abstract class AbstractGeoIPService implements GeoIPService {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractGeoIPService.class);

    /**
     * Remote url
     */
    private final String url;

    /**
     * Local file path
     */
    protected final Path path;

    private final Duration relevance;

    public AbstractGeoIPService(@NonNull String url, @NonNull Path path, @NonNull Duration relevance) {
        this.url = url;
        this.path = path;
        this.relevance  = relevance;
    }

    public void downloadIfNeeded() throws IOException {
        if (Files.exists(path)) {
            FileTime fileTime = Files.getLastModifiedTime(path);

            if (System.currentTimeMillis() - fileTime.toMillis() < relevance.toMillis()) {
                logger.info("Using local file {}", path);
                return;
            }
        }

        download();
    }

    private void download() {
        logger.info("Downloading ...");

        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body)
                .thenAccept(this::processBytes).join();
    }

    protected abstract void processBytes(byte[] payload);
}
