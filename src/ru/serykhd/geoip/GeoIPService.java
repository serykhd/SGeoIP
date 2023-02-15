package ru.serykhd.geoip;

import lombok.NonNull;
import ru.serykhd.common.service.ShutdownableService;

import java.net.InetAddress;
import java.util.Optional;

public interface GeoIPService extends ShutdownableService {

    Optional<String> getCountryResponse(@NonNull InetAddress address);

    // TODO https://db-ip.com/db/download/ip-to-country-lite
}
