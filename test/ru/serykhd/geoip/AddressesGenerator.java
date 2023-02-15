package ru.serykhd.geoip;

import lombok.experimental.UtilityClass;
import ru.serykhd.common.net.IPLong;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class AddressesGenerator {

    public Set<InetAddress> generate() {
        Set<InetAddress> addresses = new HashSet<>();

        for (int i = 0; i < 1_100_000; i++) {
            addresses.add(IPLong.long2ip(ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE)));
        }

        addresses.removeIf(InetAddress::isLoopbackAddress);
        addresses.removeIf(InetAddress::isSiteLocalAddress);
        addresses.removeIf(InetAddress::isMulticastAddress);

        System.out.println(addresses.size() + " addresses [AddressesGenerator]");

        return addresses;
    }
}
