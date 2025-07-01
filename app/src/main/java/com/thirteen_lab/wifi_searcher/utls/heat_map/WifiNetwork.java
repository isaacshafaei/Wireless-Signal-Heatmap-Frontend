package com.thirteen_lab.wifi_searcher.utls.heat_map;

public class WifiNetwork {
    private String bssid;
    private String ssid;

    public WifiNetwork(String bssid, String ssid) {
        this.bssid = bssid;
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiNetwork other = (WifiNetwork) o;

        return bssid != null && bssid.equals(other.getBssid()) &&
                ssid != null && ssid.equals(other.getSsid());
    }

    @Override
    public int hashCode() {
        int result = bssid != null ? bssid.hashCode() : 0;
        result = 31 * result + (ssid != null ? ssid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getSsid() + " (" + getBssid() + ")";
    }
}
