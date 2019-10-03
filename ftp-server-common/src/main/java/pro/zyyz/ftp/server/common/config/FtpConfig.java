package pro.zyyz.ftp.server.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


/**
 * @author xuzhi
 * @Desc ftp 服务器相关配置，数据来源 application.yml 配置文件
 */
@Data
@Component
public class FtpConfig {

    /**
     * ftp 服务器地址
     */
    @Value("${ftp.ftpHost}")
    private String ftpHost = "192.168.11.135";

    /**
     * ftp 服务器用户名
     */
    @Value("${ftp.username}")
    private String ftpUser = "myuser";

    /**
     * ftp 服务器密码
     */
    @Value("${ftp.password}")
    private String ftpPass = "mypass";

    /**
     * ftp 服务器端口号
     */
    @Value("${ftp.port}")
    private Integer ftpPort = 21;

    /**
     * ftp 服务器端保存路径
     */
    @Value("${ftp.cloudDir}")
    private String ftpCloudDir;


}
