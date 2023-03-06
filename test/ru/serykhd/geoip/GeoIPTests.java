package ru.serykhd.geoip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.Setup;
import ru.serykhd.geoip.dbip.DBIP;
import ru.serykhd.geoip.ipfire.IPFireGeoIP;
import ru.serykhd.geoip.maxmind.MaxMindGeoIP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GeoIPTests {

    private final Collection<GeoIPService> services = new ArrayList<>();
    private final Map<InetAddress, String> addresses = new HashMap<>();

    @Setup
    @Before
    public void init() throws UnknownHostException {
        services.add(IPFireGeoIP.INSTANCE);
        services.add(MaxMindGeoIP.INSTANCE);
        services.add(DBIP.INSTANCE);

        services.forEach(GeoIPService::initAndStart);

        addresses.put(InetAddress.getByName("77.88.8.8"), "RU");
   //     addresses.put(InetAddress.getByName("157.90.10.212"), "DE");
        addresses.put(InetAddress.getByName("185.120.77.165"), "KZ");
        addresses.put(InetAddress.getByName("93.123.16.89"), "BG");
        addresses.put(InetAddress.getByName("5.44.42.40"), "AE");
    }

    @Test
    public void testResolve() {
        services.forEach(service -> {
            addresses.forEach((address, country) -> {
                Assert.assertEquals(service.getCountryResponse(address).get(), country);
            });
        });
    }
}
