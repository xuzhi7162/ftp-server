package pro.zyyz.ftp.server.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import pro.zyyz.ftp.server.common.config.FtpConfig;

import java.io.*;
import java.util.Map;

@Slf4j(topic = "FtpUtils")
public class FtpUtils {

    private FtpConfig ftpConfig = new FtpConfig();

    //ftp服务器地址
    public String hostname = "";
    //ftp服务器端口号默认为21
    public Integer port = 21 ;
    //ftp登录账号
    public String username = "";
    //ftp登录密码
    public String password = "";

    public FTPClient ftpClient = null;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FtpUtils() {
        System.out.println(ftpConfig.toString());
        this.hostname = ftpConfig.getFtpHost();
        this.port = ftpConfig.getFtpPort();
        this.username = ftpConfig.getFtpUser();
        this.password = ftpConfig.getFtpPass();
    }

    /**
     * 初始化ftp服务器
     */
    public boolean initFtpClient() {
        boolean flag = true;
        ftpClient = new FTPClient();
        // 设置文件名编码格式
        ftpClient.setControlEncoding("utf-8");
        log.info("准备连接 FTP 服务器：" + this.hostname + ":" + this.port);

        try {
            //连接ftp服务器
            ftpClient.connect(hostname, port);
        } catch (IOException e) {
            log.error("连接 FTP 服务器失败：", e);
        }
        try {
            //登录ftp服务器
            ftpClient.login(username, password);
        } catch (IOException e) {
            log.error("登陆 FTP 服务器失败，请检查用户名或密码", e);
        }
        //是否成功登录服务器
        int replyCode = ftpClient.getReplyCode();
        if(!FTPReply.isPositiveCompletion(replyCode)){
            flag = false;
            log.error("FTP 服务器连接失败，请检查连接配置：" + this.hostname + ":" + this.port);
        }else{
            log.info("FTP 服务器连接成功：" + this.hostname + ":" + this.port);
        }
        return flag;
    }


    /**
     * 上传文件
     * @param pathname ftp服务保存地址
     * @param fileName 上传到ftp的文件名
     *  @param originfilename 待上传文件的名称（绝对地址） *
     * @return
     */
    public boolean uploadFile( String pathname, String fileName,String originfilename){
        boolean flag = false;
        InputStream inputStream = null;
        try{
            log.info("开始上传文件：" + fileName);
            // 切换到被动模式,解决上传文件大小为 0 的问题
            inputStream = new FileInputStream(new File(originfilename));
            initFtpClient();
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            CreateDirecroty(pathname);
            //ftpClient.makeDirectory(pathname);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.logout();
            flag = true;
            log.info("文件上传成功！");
        }catch (Exception e) {
            log.error("文件上传失败", e);
        }finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }
    /**
     * 上传文件
     * @param pathname ftp服务保存地址
     * @param fileName 上传到ftp的文件名
     * @param inputStream 输入文件流
     * @return
     */
    public boolean uploadFile( String pathname, String fileName,InputStream inputStream){
        boolean flag = false;
        try{
            log.info("开始上传文件");
            initFtpClient();
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            CreateDirecroty(pathname);
            //ftpClient.makeDirectory(pathname);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.logout();
            flag = true;
            log.info("上传文件成功");
        }catch (Exception e) {
            log.error("上传文件失败");
            e.printStackTrace();
        }finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }
    //改变目录路径
    public boolean changeWorkingDirectory(String directory) {
        boolean flag = true;
        try {
            flag = ftpClient.changeWorkingDirectory(directory);
            if (flag) {
                log.info("进入文件夹" + directory + "成功！");
            } else {
                log.warn("进入文件夹" + directory + "失败！开始创建文件夹");
            }
        } catch (IOException e) {
            log.error("切换工作路径异常", e);
        }
        return flag;
    }

    /**
     * 如果不存在该目录结构，则创建，否则不创建
     * @param remote
     * @return
     * @throws IOException
     */
    public boolean CreateDirecroty(String remote) throws IOException {
        boolean success = true;
        String directory = remote + "/";
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(new String(directory))) {
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            String path = "";
            String paths = "";
            while (true) {
                String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
                path = path + "/" + subDirectory;
                System.out.println("path====="+path);
                if (!existFile(path)) {
                    if (makeDirectory(subDirectory)) {
                        changeWorkingDirectory(subDirectory);
                    } else {
                        log.error("创建目录[" + subDirectory + "]失败");
                        changeWorkingDirectory(subDirectory);
                    }
                } else {
                    changeWorkingDirectory(subDirectory);
                }

                paths = paths + "/" + subDirectory;
                start = end + 1;
                end = directory.indexOf("/", start);
                // 检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return success;
    }

    //判断ftp服务器文件是否存在
    public boolean existFile(String path) throws IOException {
        boolean flag = false;
        ftpClient.enterLocalPassiveMode();
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }
    //创建目录
    public boolean makeDirectory(String dir) {
        boolean flag = true;
        try {
            flag = ftpClient.makeDirectory(dir);
            if (flag) {
                System.out.println("创建文件夹" + dir + " 成功！");

            } else {
                System.out.println("创建文件夹" + dir + " 失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /** * 下载文件 *
     * @param pathname FTP服务器文件目录 *
     * @param filename 文件名称 *
     * @return */
    public  byte[] downloadFile(String pathname, String filename){
        boolean flag = false;
        byte[] bt = null;
        OutputStream os=null;
        InputStream in = null;
        try {
            System.out.println("开始下载文件");
            initFtpClient();
            //切换FTP目录
            Boolean b =ftpClient.changeWorkingDirectory(pathname);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for(FTPFile file : ftpFiles){
                if(filename.equalsIgnoreCase(file.getName())){
                    //直接用文件名取
                    String remoteAbsoluteFile = toFtpFilename(filename);
                    ftpClient.setBufferSize(1024);
                    ftpClient.setControlEncoding("UTF-8");
                    ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
                    in = ftpClient.retrieveFileStream(remoteAbsoluteFile);
                    bt = input2byte(in);
                    in.close();
                    /*File localFile = new File(localpath + "/" + file.getName());
                    os = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(file.getName(), os);
                    os.close(); */
                }
            }
            ftpClient.logout();
            flag = true;
            System.out.println("下载文件成功");
        } catch (Exception e) {
            System.out.println("下载文件失败");
            e.printStackTrace();
        } finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(null != os){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bt;
    }

    private static String toFtpFilename(String fileName) throws Exception {

        return new String(fileName.getBytes("GBK"),"ISO8859-1");

    }

    public static byte[] input2byte(InputStream inStream) throws IOException {

        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();

        byte[] buff = new byte[1024];

        int rc = 0;

        while ((rc = inStream.read(buff, 0, 100)) > 0) {

            swapStream.write(buff, 0, rc);

        }

        byte[] in2b = swapStream.toByteArray();

        swapStream.close();

        return in2b;

    }
    /** * 删除文件 *
     * @param pathname FTP服务器保存目录 *
     * @param filename 要删除的文件名称 *
     * @return */
    public boolean deleteFile(String pathname, String filename){
        boolean flag = false;
        try {
            System.out.println("开始删除文件");
            initFtpClient();
            //切换FTP目录
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.dele(filename);
            ftpClient.logout();
            flag = true;
            System.out.println("删除文件成功");
        } catch (Exception e) {
            System.out.println("删除文件失败");
            e.printStackTrace();
        } finally {
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }


    /** * 下载文件 *
     * @param pathname FTP服务器文件目录 *
     * @param filename 文件名称 *
     * @param localpath 下载后的文件路径 *
     * @return */
    public  boolean transferFile(Map map){
        String pathname = (String) map.get("pathname");

        boolean flag = false;
        byte[] bt = null;
        OutputStream os=null;
        InputStream in = null;
        String filePath = pathname.substring(0, pathname.lastIndexOf("/"));
        String filename = pathname.substring(pathname.lastIndexOf("/")+1);
        try {
            System.out.println("开始传输文件");
            //ftpUtil(hostnameCome,portCome,usernameCome,passwordCome);
            //切换FTP目录
            flag = initFtpClient();
            ftpClient.changeWorkingDirectory(filePath);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for(FTPFile file : ftpFiles){
                if(filename.equalsIgnoreCase(file.getName())){
                    //直接用文件名取
                    String remoteAbsoluteFile = toFtpFilename(filename);
                    ftpClient.setBufferSize(1024);
                    ftpClient.setControlEncoding("UTF-8");
                    ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
                    in = ftpClient.retrieveFileStream(remoteAbsoluteFile);
                    System.out.println("开始上传文件");
                    initFtpClient();
                    ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
                    CreateDirecroty("bordertemp/"+filePath);
                    //ftpClient.makeDirectory(pathname);
                    ftpClient.changeWorkingDirectory("bordertemp/"+filePath);
                    ftpClient.storeFile(filename, in);
                    //in.close();
                    /*File localFile = new File(localpath + "/" + file.getName());
                    os = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(file.getName(), os);
                    os.close(); */
                }
            }
            ftpClient.logout();
            flag = true;
            System.out.println("下载文件成功");
        } catch (Exception e) {
            System.out.println("下载文件失败");
            e.printStackTrace();
        } finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(null != in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != os){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

}
