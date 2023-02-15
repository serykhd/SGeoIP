package ru.serykhd.geoip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import ru.serykhd.common.net.IPLong;
import ru.serykhd.geoip.ipfire.IPFireGeoIP;
import ru.serykhd.geoip.maxmind.MaxMindGeoIP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
public class IPFireGeoIPTest {

    @Setup
    @Before
    public void init() {
        IPFireGeoIP.INSTANCE.initAndStart();
        MaxMindGeoIP.INSTANCE.initAndStart();
    }

    @Test
    public void testCheckRu() throws UnknownHostException {
        Assert.assertEquals(IPFireGeoIP.INSTANCE.getCountryResponse(InetAddress.getByName("77.88.8.8")).get(), "RU");
        Assert.assertEquals(IPFireGeoIP.INSTANCE.getCountryResponse(InetAddress.getByName("157.90.10.212")).get(), "DE");
        Assert.assertEquals(IPFireGeoIP.INSTANCE.getCountryResponse(InetAddress.getByName("185.120.77.165")).get(), "KZ");

        Assert.assertEquals(IPFireGeoIP.INSTANCE.getCountryResponse(InetAddress.getByName("93.123.16.89")).get(), "BG");
        Assert.assertEquals(IPFireGeoIP.INSTANCE.getCountryResponse(InetAddress.getByName("5.44.42.40")).get(), "AE");
    }

    private InetAddress addr = IPLong.long2ip(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));

    @Benchmark
    public void BE() {
     //   IPFireGeoIP.INSTANCE.getCountryResponse(IPLong.long2ip(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)));
        MaxMindGeoIP.INSTANCE.getCountryResponse(IPLong.long2ip(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)));
    }

    @Benchmark
    public void B2E() {
        IPLong.long2ip(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
    }
}
