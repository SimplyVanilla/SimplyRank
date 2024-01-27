package net.simplyvanilla.simplyrank.proxy;

import net.simplyvanilla.simplyrank.proxy.provider.ProxyProvider;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyResult;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DummyProxyProvider implements ProxyProvider {
    private final Map<String, ProxyResult> values;

    public DummyProxyProvider(Map<String, ProxyResult> values) {
        this.values = values;
    }

    @Override
    public @Nullable ProxyResult fetch(String address) {
        return this.values.get(address);
    }
}
