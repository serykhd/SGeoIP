package ru.serykhd.geoip.dbip;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import ru.serykhd.common.net.IPLong;
import ru.serykhd.geoip.AbstractGeoIPService;
import ru.serykhd.logger.InternalLogger;
import ru.serykhd.logger.impl.InternalLoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

public class DBIP extends AbstractGeoIPService {

    public static final DBIP INSTANCE = new DBIP();

    private static final String GEO_URL = "https://download.db-ip.com/free/dbip-country-lite-2023-%s.csv.gz";

    private final InternalLogger logger = InternalLoggerFactory.getInstance(DBIP.class);

    private final TreeMap<Long, Optional<String>> iso = new TreeMap<>();

    public DBIP() {
        this(String.format(GEO_URL, parseMonth()));
    }

    private DBIP(String url) {
        super(url, Path.of("DBIP", "geoip"), Duration.ofDays(7 * 3));
    }

    private static String parseMonth() {
        LocalDate localDate = LocalDate.now();
        return localDate.getMonthValue() < 10 ? "0" + localDate.getMonthValue() : Integer.toString(localDate.getMonthValue());
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
            String[] split = line.split(",");

            InetAddress startAddress = parseAddress(split[0]);

            // тут нам нужно откинуть это
            if (startAddress instanceof Inet6Address) {
                return;
            }

            InetAddress endAddress = parseAddress(split[1]);

            long start = IPLong.ip2long(startAddress);
            long end = IPLong.ip2long(endAddress);

            String code = split[2];

            // это чтобы мы могли юзнуть lower вместо floor т.к. он производительнее
            start--;

            iso.put(start, Optional.of(code));
            iso.put(end, Optional.empty());

        //    System.out.printf("%s > %s | %s \n", start, end, code);
        });

        logger.info("Loaded {} ranges!", iso.size());
    }

    @SneakyThrows
    private InetAddress parseAddress(@NonNull String address) {
        return InetAddress.getByName(address);
    }

    @Override
    public void shutdown() {
        iso.clear();
    }

    @SneakyThrows
    protected void processBytes(byte[] payload) {
        @Cleanup
        GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(payload));

        Files.createDirectories(path.getParent());

        Files.write(path, gzipIn.readAllBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

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