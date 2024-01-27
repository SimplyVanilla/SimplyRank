package net.simplyvanilla.simplyrank.proxy.provider;

import org.jetbrains.annotations.Nullable;

public interface ProxyProvider {

    /**
     * Fetches the proxy type of the given address
     *
     * @param address the address to fetch the proxy type of
     * @return the proxy type of the given address, or null if the proxy type could not be fetched
     */
    @Nullable ProxyResult fetch(String address);

}
