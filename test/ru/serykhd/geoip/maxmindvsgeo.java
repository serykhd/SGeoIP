package ru.serykhd.geoip;

import ru.serykhd.geoip.ipfire.IPFireGeoIP;
import ru.serykhd.geoip.maxmind.MaxMindGeoIP;

import java.net.InetAddress;

@Deprecated
public class maxmindvsgeo {
/*
    public static void main(String[] args) {
        IPFireGeoIP.INSTANCE.initAndStart();
        MaxMindGeoIP.INSTANCE.initAndStart();

        Set<InetAddress> addresses = new HashSet<>();

        for (int i = 0; i < 1_100_000; i++) {
            addresses.add(InetAddresses.fromInteger(new Random().nextInt()));
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(IPLong.long2ip(ThreadLocalRandom.current().nextLong(0, Integer.MAX_VALUE)));
        }
        addresses.removeIf(InetAddress::isLoopbackAddress);
        addresses.removeIf(InetAddress::isSiteLocalAddress);
        addresses.removeIf(InetAddress::isMulticastAddress);

        System.out.println(addresses.size() + " addresses");

        for (InetAddress address : addresses) {
            Optional<CountryResponse> a =  GeoIPService.INSTANCE.getCountryResponse(address);

            String maxmind = null;

            if (a.isPresent()) {
                maxmind = a.get().getCountry().getIsoCode();
            }

            Optional<String> b =  geo3.INSTANCE.getCountryResponse(address);

            String geo2 = null;

            if (b.isPresent()) {
                geo2 = b.get();
            }

            if (maxmind == null && geo2 == null ) {
                continue;
            }

            if (maxmind == null || geo2 == null) {
              //  System.out.println("null " + maxmind + " " + geo2 + " " + address);
                continue;
            }

            if (maxmind.equals(geo2)) {
                continue;
            }

            System.out.println("neq " + maxmind + " " + geo2 + " " + address);
            continue;
        }
    }*/
}
