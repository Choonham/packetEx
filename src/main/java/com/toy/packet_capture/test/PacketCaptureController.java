package com.toy.packet_capture.test;

import com.toy.packet_capture.AsyncConfig;
import com.toy.packet_capture.service.PacketExService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/capture")
public class PacketCaptureController {
    @Autowired
    PacketExService packetExService;

    @Resource(name="asyncConfig")
    AsyncConfig asyncConfig;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "start.do", produces = "application/json; charset=utf8")
    @ResponseBody
    public String openServer(String expression) {
        int rtnVal = 0;

        try {
            if(asyncConfig.checkSampleTaskExecute()) {
                asyncConfig.getAsyncExecutor();
                AsyncConfig.setSTOPFLAG(false);
                packetExService.listeningNet(expression, 0xFFFFFF00);
                rtnVal = 1;
            }else {
                logger.error("Thread 개수 초과");
            }
        } catch (Exception e) {
            logger.error("Thread Err :: " + e.getMessage());
            rtnVal = 0;
        }

        return "{\"result\": \"" + Integer.toString(rtnVal) + "\"}";
    }

    @RequestMapping(value = "getIP.do", produces = "application/json; charset=utf8")
    @ResponseBody
    public String getIP(String hostName) {
        String rtnVal = "";
        try{
            rtnVal=  packetExService.dnsLookup(hostName);
        } catch(Exception e) {
            rtnVal = "0";
        }

        return "{\"result\": \"" + rtnVal + "\"}";
    }

    @RequestMapping(value = "stop.do", produces = "application/json; charset=utf8")
    @ResponseBody
    public String stop() {
        String rtnVal = "";
        try{
            AsyncConfig.setSTOPFLAG(true);
            rtnVal = "1";
        } catch(Exception e) {
            rtnVal = "0";
        }

        return "{\"result\": \"" + rtnVal + "\"}";
    }
}
