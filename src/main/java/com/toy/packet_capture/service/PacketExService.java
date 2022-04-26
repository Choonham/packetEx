package com.toy.packet_capture.service;

public interface PacketExService {
    public void listeningNet(String expression, int netmask);

    public String dnsLookup(String hostName);

    public String getDomain(String hostIP);
}
