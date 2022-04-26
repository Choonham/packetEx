package com.toy.packet_capture.service;

import com.toy.packet_capture.AsyncConfig;
import com.toy.packet_capture.packet.PacketList;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.PcapIf;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.Payload;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


@Service
public class PacketExServiceImpl implements PacketExService {
    private final Logger logger = LoggerFactory.getLogger(PacketExService.class);
    private static final int START_LENGTH = 6;

    @Override
    @Async
    public void listeningNet(String expression, int netmask) {
        ArrayList<PcapIf> allDevs = new ArrayList<PcapIf>();
        StringBuilder errBuf = new StringBuilder();

        int r = Pcap.findAllDevs(allDevs, errBuf);

        if(r == Pcap.NOT_OK || allDevs.isEmpty()) {
            logger.error("네트워크 장치 찾기 실패" + errBuf.toString());
            return;
        }

        logger.info("네트워크 Device");

        int i = 0;

        for (PcapIf device : allDevs) {
            String description = (device.getDescription() != null) ? device.getDescription() : "장비에 대한 설명이 없습니다.";

            logger.info(++i +"번: " + device.getName() + "[" + device.getDescription() + "]");
        }

        PcapIf device = allDevs.get(0);
        logger.info("선택된 장치: %s\n", (device.getDescription() != null) ?
                device.getDescription() : device.getName());

        int snaplen = 64 * 1024; //65536Byte만큼 패킷을 캡쳐
        int flags = Pcap.MODE_NON_PROMISCUOUS; // 무차별모드
        int timeout = 10 *1000; // timeout 10second
        Pcap pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errBuf);

        if (pcap == null) {
           logger.info("Network Device Access Failed. Error: " + errBuf.toString());
            return;
        }

        Ethernet eth = new Ethernet();
        Ip4 ip = new Ip4();
        Tcp tcp = new Tcp();
        Udp udp = new Udp();

        Payload payload = new Payload();
        PcapHeader header = new PcapHeader(JMemory.POINTER);
        JBuffer buf = new JBuffer(JMemory.POINTER);

        int id = JRegistry.mapDLTToId(pcap.datalink());	// pcap의 datalink 유형을 jNetPcap의 프로토콜 ID에 맵핑

        PcapBpfProgram filter = new PcapBpfProgram();
        /*String temp = "(tcp dst port 443 and dst host 121.53.105.193)";*/

        logger.info("expression: " + expression);

        if(expression != null) {

            if (pcap.compile(filter, expression, 0, netmask) != Pcap.OK) {
                logger.error(pcap.getErr());
                return;
            }
        }
        pcap.setFilter(filter);
        while(pcap.nextEx(header, buf) == Pcap.NEXT_EX_OK) // 헤더와 버퍼를 피어링
        {
            if(AsyncConfig.isSTOPFLAG()) break;
            PcapPacket packet = new PcapPacket(header, buf);

            packet.scan(id); //새로운 패킷을 스캔하여 포함 된 헤더를 찾습니다
            logger.info(Long.toString(packet.getFrameNumber()));
            logger.info("##################################### Packet #####################################");
            if (packet.hasHeader(eth)) {
                logger.info("##################################### ethernet start #####################################");
                PacketList.headers.add(eth);
                logger.info("headers length : " + eth.getHeaderLength());
                logger.info("ethernet packet: \n" + FormatUtils.hexdump(eth.getHeader()));
                logger.info("출발지 MAC 주소 = "+ FormatUtils.mac(eth.source()) +"||도착지 MAC 주소 = " + FormatUtils.mac(eth.destination()));
                logger.info("Type: " + eth.type());
                /**
                 * DstAddress(FF FF FF FF FF FF) SrcAddress(FF FF FF FF FF FF)
                 * Type(FF FF) Data() FCS(FF FF FF FF)
                 * */
                logger.info("##################################### ethernet end #####################################");
            }
            if (packet.hasHeader(ip)) {
                logger.info("##################################### ip start #####################################");
                PacketList.headers.add(ip);
                logger.info("headers length : " + PacketList.headers.size());
                logger.info("ip packet: \n" + FormatUtils.hexdump(ip.getHeader()));
                logger.info("ip version[4bit]: " + ip.version());
                logger.info("ip header length[4bit]: " + ip.getHeaderLength());
                logger.info("ip type of service[8bit]: " + ip.tos());
                logger.info("ip total length[16bit]: " + ip.getLength());
                logger.info("ip identification[16bit]: " + ip.id());
                logger.info("ip flag[3bit]: " + ip.flags());
                logger.info("ip fragment offset[13bit]: " + ip.offset());
                logger.info("ip ttl[8bit]: " + ip.ttl());
                logger.info("ip protocol type[8bit]: " + ip.type());
                logger.info("ip header checksum[16bit]: " + ip.checksum());
                logger.info("출발지 IP 주소[32bit] = "+FormatUtils.ip(ip.source())+" || 도착지 IP 주소[32bit] = "+ FormatUtils.ip(ip.destination()));

                logger.info("출발지 IP 주소[32bit] = "+ getDomain(FormatUtils.ip(ip.source())) +" || 도착지 IP 주소[32bit] = "+ getDomain(FormatUtils.ip(ip.destination())));
                /**
                 * Version(F) HeaderLength(F) typeOfService(FF) Total Length(FF FF)
                 * Identification(FF FF) Flag(3bit) FragmentOffset(13bit)
                 * TTL(FF) ProtocolType(FF) Header Checksum(FF FF)
                 * Source IP Address(FF FF FF FF)
                 * Destination IP Address(FF FF FF FF)
                 *
                 * */
                logger.info("##################################### ip end #####################################");
            }
            if (packet.hasHeader(tcp)) {
                logger.info("##################################### tcp start #####################################");
                PacketList.headers.add(tcp);
                logger.info("headers length : " + PacketList.headers.size());
                logger.info("tcp packet: \n" + FormatUtils.hexdump(tcp.getHeader()));
                logger.info("출발지 TCP 주소[16bit] = "+tcp.source()+"|| 도착지 TCP 주소[16bit] = "+tcp.destination());
                logger.info("tcp seq[32bit]: " + tcp.seq());
                logger.info("tcp ack[32bit]: " + tcp.ack());
                logger.info("tcp header length[4bit]: " + tcp.getHeaderLength());
                logger.info("tcp Reservation[6bit]: " + tcp.reserved());
                logger.info("tcp flag[1bit]: " + tcp.flags());
                logger.info("tcp flag[16bit]: " + tcp.window());
                logger.info("tcp check sum[8bit]: " + tcp.checksum());
                logger.info("tcp urgent point[8bit]: " + tcp.urgent());

                /**
                 * SourcePort(FF FF) DestinationPort(FF FF) seq(FF FF FF FF)
                 * ACK(FF FF FF FF) headerLength(F) Reservation(6bit) FLAG(1bit x 6)
                 * window size(FF FF) Checksum(FF) Urgent Point(FF)
                 * */
                logger.info("##################################### tcp end #####################################");
            }
            if (packet.hasHeader(udp)) {
                logger.info("##################################### udp start #####################################");
                PacketList.headers.add(udp);
                logger.info("headers length : " + PacketList.headers.size());
                logger.info("udp packet: \n" + FormatUtils.hexdump(udp.getHeader()));
                logger.info("출발지 UDP 주소 = "+udp.source()+"|| 도착지 UDP 주소 = "+udp.destination());
                logger.info("udp check sum[16bit]: " + udp.checksum());
                /**
                 * SourcePort(FF FF) DestinationPort(FF FF)
                 * UDP Datagram Length(FF FF) Checksum(FF FF)
                 * */
                logger.info("##################################### udp end #####################################");
            }
            if (packet.hasHeader(payload)) {
                logger.info("##################################### payload start #####################################");
                logger.info("페이로드의 길이 = " + payload.getLength());
                logger.info("\n" + payload.toHexdump());	// 와이어샤크에서 보이는 hexdump를 출력
                logger.info("##################################### payload end #####################################");
            }
        }
        pcap.close();
    }

    @Override
    public String dnsLookup(String hostName) {
        String rtnVal;
        try{
            InetAddress ipAddress = InetAddress.getByName(hostName);
            String ip = ipAddress.getHostAddress();

            rtnVal = ip;

        } catch(Exception e) {
            logger.error(e.getMessage());
            rtnVal = "Exception";
        }
        return rtnVal;
    }


    @Override
    public String getDomain(String hostIP) {
        String rtnVal;
        try{
            InetAddress ipAddress = InetAddress.getByName(hostIP);

            rtnVal = ipAddress.getHostName();

        } catch(Exception e) {
            logger.error(e.getMessage());
            rtnVal = "Exception";
        }
        return rtnVal;
    }
}