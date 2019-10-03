package pro.zyyz.ftp.server.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.zyyz.ftp.server.common.util.FtpUtils;

@RestController
public class FtpController {

    @GetMapping("/ftp")
    public String test(){
        FtpUtils ftpUtils = new FtpUtils();
        boolean b1 = ftpUtils.uploadFile("/home/test", "CentOS-7-x86_64-Minimal-1810.iso", "H:/Linux/CentOS-7-x86_64-Minimal-1810.iso");

        return String.valueOf("连接状态：" + b1);

    }

    @GetMapping("/down")
    public byte[] down(){
        FtpUtils ftpUtils = new FtpUtils();

        byte[] bytes = ftpUtils.downloadFile("/home/test", "CentOS-7-x86_64-Minimal-1810.iso");

        return bytes;

    }
}
