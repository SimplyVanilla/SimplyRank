package net.simplyvanilla.simplyrank.proxy.provider;

public enum ProxyType {
    RESIDENTIAL("Residential"),
    WIRELESS("Wireless"),
    BUSINESS("Business"),
    HOSTING("Hosting"),
    TOR("TOR"),
    SOCKS("SOCKS"),
    SOCKS4("SOCKS4"),
    SOCKS4A("SOCKS4A"),
    SOCKS5("SOCKS5"),
    SOCKS5H("SOCKS5H"),
    SHADOWSOCKS("Shadowsocks"),
    HTTP("HTTP("),
    HTTPS("HTTPS"),
    CROMPROMISED_SERVER("Compromised Server"),
    INFERENCE_ENGINE("Inference Engine"),
    OPENVPN("OpenVPN"),
    VPN("VPN"),
    WHITELISTED("whitelisted by"),
    BLACKLISTED("blacklisted by"),
    UNKNOWN("Unknown");

    private final String value;

    ProxyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static ProxyType parse(String value) {
        for (ProxyType proxyType : ProxyType.values()) {
            if (proxyType.getValue().equalsIgnoreCase(value)) {
                return proxyType;
            }
        }

        if (value.startsWith("whitelisted by")) {
            return ProxyType.WHITELISTED;
        } else if (value.startsWith("blacklisted by")) {
            return ProxyType.BLACKLISTED;
        }

        return UNKNOWN;
    }
}
