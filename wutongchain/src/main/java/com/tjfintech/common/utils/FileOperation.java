package com.tjfintech.common.utils;


import java.io.*;

//import static com.java.tar.gz.FileUtil.log;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SDKPATH;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.*;

import com.jcraft.jsch.*;
import com.sun.org.apache.bcel.internal.classfile.ConstantString;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.StringContains;

public class FileOperation {

    //@Test
    public void replace(String fulPathFile,String srcStr,String destStr) throws Exception{
        String fileName = fulPathFile.substring(fulPathFile.lastIndexOf("/")+1,fulPathFile.lastIndexOf("."));
        File destFile = new File(fulPathFile.replace(fileName,fileName+"_temp"));
        String cont = read(fulPathFile);
        //System.out.println(cont);
        //对得到的内容进行处理
        cont = cont.replaceAll(srcStr, destStr);
        //System.out.println(cont);
        //更新源文件
        write(cont, destFile);
        Thread.sleep(2000);

    }

    public static String read(String path) {
        File file = new File(path);
        StringBuffer res = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                res.append(line + "\n");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    public static boolean write(String cont, File dist) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dist));
            writer.write(cont);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void appendToFile(String data,String filename)throws Exception{
        FileWriter fw = null;
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(filename);
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(data);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------------------//
    //以下为对配置文件修改 通过shell脚本方式
    //此部分读写不支持重复section的内容 例如 peer config.toml中会有多个 [[Members.Peers]]
    //此部分读写不支持重复section的内容 例如 sdk conf/config.toml中会有多个 [[Peers]]
    //修改节点配置项conf/base.toml信息
    public static void setPeerBaseByShell(String IP,String Section,String Key,String Value)throws Exception{
        String info = shExeAndReturn(IP,"sh " + destShellScriptDir + "SetConfig.sh " + PeerBaseConfigPath + " " + Section + " " + Key + " " + Value);
        System.out.print(info);
        
    }
    //读取节点配置项信息
    public static String getPeerBaseValueByShell(String IP,String Section,String Key)throws Exception{
        return shExeAndReturn(IP,"sh " + destShellScriptDir + "GetConfig.sh " + PeerBaseConfigPath + " " + Section + " " + Key);
    }

    //修改SDK配置项conf/config.toml信息
    public static void setSDKConfigByShell(String IP,String Section,String Key,String Value)throws Exception{
        shExeAndReturn(IP,"sh " + destShellScriptDir + "SetConfig.sh " + SDKConfigPath + " " + Section + " " + Key + " " + Value);
    }
    //读取SDK配置项conf/config.toml信息
    public static String getSDKConfigValueByShell(String IP,String Section,String Key){
        return shExeAndReturn(IP,"sh " + destShellScriptDir + "GetConfig.sh " + SDKConfigPath + " " + Section + " " + Key);
    }

    //读取TOKEN API配置项conf/config.toml信息
    public static String getTokenApiConfigValueByShell(String IP,String Section,String Key){
        return shExeAndReturn(IP,"sh " + destShellScriptDir + "GetConfig.sh " + TokenApiConfigPath + " " + Section + " " + Key);
    }

    public static void uploadFiletoDestDirByssh(String srcFile,String destIP,String destUser,String destPwd,String destDir,String destFileName){

        //首先确认目标目录必须存在 以下创建仅能创建一层目录即destDir的前一级目录必须存在
        String mkdirInfo = shExeAndReturn(destIP,"mkdir " + destDir);
        assertEquals(false,mkdirInfo.contains("No such file or directory"));

        try {
            Session session = null;
            JSch jsch = new JSch();
            session = jsch.getSession(destUser,destIP,22);

            session.setPassword(destPwd);
            //修改服务器/etc/ssh/sshd_config 中 GSSAPIAuthentication的值yes为no，解决用户不能远程登录
            session.setConfig("userauth.gssapi-with-mic", "no");
            //为session对象设置properties,第一次访问服务器时不用输入yes

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp channelSftp = (ChannelSftp)session.openChannel("sftp");
            channelSftp.connect();
            assertEquals(true,channelSftp.isConnected());

            File file = new File(srcFile);
            InputStream input = new BufferedInputStream(new FileInputStream(file));
            channelSftp.cd(destDir);
            if(destFileName.isEmpty()) {
                channelSftp.put(input, file.getName());
            }
            else{
                channelSftp.put(input, destFileName);
            }
            try {
                if (input != null) input.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print(file.getName() + "关闭文件时.....异常!" + e.getMessage());
            }

        }catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
        }
    }

}
