package ru.serykhd.geoip;

import lombok.SneakyThrows;
import ru.serykhd.geoip.ipfire.IPFireGeoIP;
import ru.serykhd.geoip.maxmind.MaxMindGeoIP;

import java.net.InetAddress;
import java.util.Set;

public class GeoIPServiceBench {

    public static void main(String[] args) throws Exception {
        GeoIPService geoIPService = IPFireGeoIP.INSTANCE;

       // geoIPService = MaxMindGeoIP.INSTANCE;

        geoIPService.initAndStart();

        Set<InetAddress> addresses = AddressesGenerator.generate();

        for (int i = 0; i < 10; i++) {
            bench(geoIPService, addresses);
        }
    }

    @SneakyThrows
    private static void bench(GeoIPService geoIPService, Set<InetAddress> addresses) {
        long startTime = System.nanoTime();

        for (InetAddress address : addresses) {
            geoIPService.getCountryResponse(address);
        }

        long endTime = System.nanoTime();

        long duration = endTime - startTime;
        long qps = addresses.size() * 1000000000L / duration;

        System.out.println("Requests per second: " + qps);
    }
}
