package ru.serykhd.geoip.ipfire;

import lombok.NonNull;
import lombok.SneakyThrows;
import ru.serykhd.common.net.IPLong;
import ru.serykhd.geoip.AbstractGeoIPService;
import ru.serykhd.logger.InternalLogger;
import ru.serykhd.logger.impl.InternalLoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class IPFireGeoIP extends AbstractGeoIPService {

    // https://location.ipfire.org/
    // https://github.com/torproject/tor/tree/main/src/config
    // 15MB in memory

    // 7M RPS
    public static final IPFireGeoIP INSTANCE = new IPFireGeoIP();

    private static final String GEO4_URL = "https://raw.githubusercontent.com/torproject/tor/main/src/config/geoip";

    private final InternalLogger logger = InternalLoggerFactory.getInstance(IPFireGeoIP.class);

    private final TreeMap<Long, Optional<String>> iso = new TreeMap<>();

    public IPFireGeoIP() {
        super(GEO4_URL, Path.of("IPFire", "geoip"), Duration.ofDays(7));
    }

    @SneakyThrows
    @Override
    public void start() {
        try {
            downloadIfNeeded();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Files.readAllLines(path).forEach(line -> {
            if (line.startsWith("#")) {
                return;
            }

            String[] split = line.split(",");

            String code = split[2];

            // не нужно нам хранить это
            if (code.equals("??")) {
                return;
            }

            long start = Long.parseLong(split[0]);
            long end = Long.parseLong(split[1]);

            // это чтобы мы могли юзнуть lower вместо floor т.к. он производительнее
            start--;

            iso.put(start, Optional.of(code));
            iso.put(end, Optional.empty());

            //   System.out.printf("%s > %s | %s \n", start, end, code);

        });

        logger.info("Loaded {} ranges!", iso.size());
    }

    @Override
    public void shutdown() {
        iso.clear();
    }

    @SneakyThrows
    protected void processBytes(byte[] payload) {
        Files.createDirectories(path.getParent());

        Files.write(path, payload, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        logger.info("Saved to file!");
    }

    @SneakyThrows
    public Optional<String> getCountryResponse(@NonNull InetAddress address) {
        if (address instanceof Inet6Address) {
            return Optional.empty();
        }

        long addressLong = IPLong.ip2long(address);

        Map.Entry<Long, Optional<String>> entry = iso.lowerEntry(addressLong);

        if (entry == null) {
            return Optional.empty();
        }

        return entry.getValue();
    }
}
