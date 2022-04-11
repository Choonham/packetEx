package com.toy.packet_capture.test;

import com.toy.packet_capture.service.PacketExService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {

    @Autowired
    PacketExService packetExService;

    @RequestMapping("/index.do")
    public String test() {

        return "index";
    }
}