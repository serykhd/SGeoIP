package ru.serykhd.geoip.maxmind;


import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import ru.serykhd.geoip.AbstractGeoIPService;
import ru.serykhd.logger.InternalLogger;
import ru.serykhd.logger.impl.InternalLoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class MaxMindGeoIP extends AbstractGeoIPService {

    // TODO https://github.com/P3TERX/GeoLite.mmdb

    // 700K RPS
    public static final MaxMindGeoIP INSTANCE = new MaxMindGeoIP();

    // https://www.maxmind.com/en/accounts/current/license-key
    private static final String KEY = "P5g0fVdAQIq8yQau";

    private static final String URL = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key=%s&suffix=%s";

    @Getter
    private final InternalLogger logger = InternalLoggerFactory.getInstance(MaxMindGeoIP.class);
    @Getter
    private DatabaseReader reader;

    public MaxMindGeoIP() {
        super(String.format(URL, KEY, "tar.gz"), Path.of("MaxMind", "GeoLite2-Country.mmdb"), Duration.ofDays(3));
    }

    /*
    private final LoadingCache<InetAddress, Optional<CountryResponse>> cache = Caffeine.newBuilder().initialCapacity(1024 * 10)
            .build(key -> reader.tryCountry(key));*/

    @SneakyThrows
    @Override
    public void start() {
        try {
            downloadIfNeeded();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // 1 адрес около 15 рекордов
        // 100 адресов около 70
        // 1000 адресов около 150
        // 10_000 адресов около 270
        // 100_000 адресов около 390
        // 1_000_000 адресов около 460
        // делаем вывод что емкости в 1024 хватит
        reader = new DatabaseReader.Builder(path.toFile()).withCache(new CHMCache(1024)).build();
    }

    @SneakyThrows
    protected void processBytes(byte[] payload) {
        @Cleanup
        GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(payload));
        @Cleanup
        TarInputStream tarIn = new TarInputStream(gzipIn);
        TarEntry entry;

        while ((entry = tarIn.getNextEntry()) != null) {

           // GeoLite2-Country_20220907/
           // GeoLite2-Country_20220907/LICENSE.txt
           // GeoLite2-Country_20220907/COPYRIGHT.txt
           // GeoLite2-Country_20220907/GeoLite2-Country.mmdb

         //   System.out.println(entry.getName());

            if (entry.getName().endsWith("mmdb")) {
                byte[] bytes = tarIn.readAllBytes();

                Files.createDirectories(path.getParent());
                Files.write(path, bytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

                logger.info("Downloaded!");

                break;
            }
        }
    }

    @SneakyThrows
    @Override
    public void shutdown() {
        reader.close();
    }

    @Override
    @SneakyThrows
    public Optional<String> getCountryResponse(@NonNull InetAddress address) {
        Optional<CountryResponse> countryResponseOptional = reader.tryCountry(address);

        return countryResponseOptional.map(countryResponse -> countryResponse.getCountry().getIsoCode());

    }
}