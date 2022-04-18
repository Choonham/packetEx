package com.toy.packet_capture.packet;

import org.jnetpcap.packet.JHeader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class PacketList {
    public static ArrayList<JHeader> headers;

    @Bean
    public ArrayList<JHeader> headerList() {
        headers = new ArrayList<JHeader>();
        return headers;
    }
}
