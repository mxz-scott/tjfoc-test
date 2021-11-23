package com.tjfintech.common;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class CertTool {

    public String certExePath = "cd " + destShellScriptDir + "; ./tjfoc-cert ";
    public String sm4ToolExePath = "cd " + destShellScriptDir + "; ./sm4op ";
    public String wuxiUTXOSignExePath = "cd " + destShellScriptDir + "; ./prikeysign ";
    public String wtcliExePath = "cd " + destShellScriptDir + "; ./wtcli ";

    public String decryptPriData(String peerIP, String plainKey, String SecretData) {
        //使用解密后的key对隐私存证密文进行解密
        String decryptData = shExeAndReturn(peerIP, sm4ToolExePath +
                "decrypt -c \"" + SecretData + "\" -f hex -k \"" + plainKey + "\" -t hex");
        return decryptData.substring(decryptData.lastIndexOf("plaintext:") + 11);
    }

    public String decryptKey(String peerIP, String secretKey, String prikey, String pwd) throws Exception {

        String pwdParam = "";
        if (!pwd.isEmpty()) pwdParam = " -p " + pwd;

        //将key保存至文件key.pem 解密SecretKey
        shellExeCmd(peerIP, "echo " + prikey + " > " + destShellScriptDir + "key.pem");
        String key = shExeAndReturn(peerIP, certExePath +
                "decrypt -i " + destShellScriptDir + "key.pem" + pwdParam + " -f base64 -c \"" + secretKey + "\" -t hex -o hex");
        key = key.trim().substring(key.lastIndexOf("msg:") + 5).trim();

        return key;
    }

    //使用解密后的key对pubkey进行加密
    public String encryptKeyWithPub(String peerIP, String key, String addPubkey) throws Exception {
        //将key保存至文件key.pem 解密SecretKey
        shellExeCmd(peerIP, "echo " + addPubkey + " > " + destShellScriptDir + "pubkey.pem");
        String SecurityKey = shExeAndReturn(peerIP, certExePath +
                "encrypt -i pubkey.pem -f base64 -m " + key + " -t hex -o base64");
        SecurityKey = SecurityKey.trim().substring(key.lastIndexOf("cipher:") + 8).trim();

        //返回使公钥加密后的密码
        return SecurityKey;
    }

    public String sign(String peerIP, String prikey, String pwd, String data, String outFormat) throws Exception {

        String pwdParam = "";
        if (!pwd.isEmpty()) pwdParam = " -p " + pwd;

        //将key保存至文件key.pem 解密SecretKey
        shellExeCmd(peerIP, "echo " + prikey + " > " + destShellScriptDir + "key.pem");
        String SignStr = shExeAndReturn(peerIP, certExePath +
                "sign -i key.pem -f base64 -m \"" + data + "\"" + pwdParam + " -o " + outFormat);
        SignStr = SignStr.trim().substring(SignStr.lastIndexOf("sign:") + 6).trim();

        return SignStr;
    }

    //Smart token转让接口签名
    public String smartSign(String peerIP, String prikey, String pwd, String data, String outFormat) throws Exception {

        String pwdParam = "";
        if (!pwd.isEmpty()) pwdParam = " -p " + pwd;

        //将key保存至文件key.pem 解密SecretKey
        shellExeCmd(peerIP, "echo " + prikey + " > " + destShellScriptDir + "key.pem");
        String SignStr = shExeAndReturn(peerIP, certExePath +
                "sign -i key.pem -f base64 -s hex -m \"" + data + "\"" + pwdParam + " -o " + outFormat);
        SignStr = SignStr.trim().substring(SignStr.lastIndexOf("sign:") + 6).trim();

        return SignStr;
    }

    public String signUTXO(String strTBS, String prikey) {
        String sig = "";
        String resp = shExeAndReturn(PEER2IP, wuxiUTXOSignExePath + strTBS + " " + prikey);
        sig = resp.trim().replaceAll("\r", "").replaceAll("\n", "");
        sig = sig.substring(sig.lastIndexOf(":") + 1).trim();
//        log.info(sig);
        return sig;

    }

    public String getSm3Hash(String peerIP, String data) {
        String hash = "";
        String resp = shExeAndReturn(peerIP, certExePath + "hash -m " + data);
        hash = resp.trim().replaceAll("\r", "").replaceAll("\n", "");
        hash = hash.substring(hash.lastIndexOf(":") + 1).trim();
        return hash;
    }

    //投标防偷窥项目--项目标识签名
    public String tapSign(String peerIP, String key, String pwd, String data, String outFormat) throws Exception {

        String pwdParam = "";
        if (!pwd.isEmpty()) pwdParam = " -p " + pwd;

        //将key保存至文件key.pem 解密SecretKey
//        shellExeCmd(peerIP, "echo " + prikey + " > " + destShellScriptDir + "key.pem");
        String SignStr = shExeAndReturn(peerIP, wtcliExePath + "crypt sm2 -k key.pem  -s -f " + key + "=" + data);
        SignStr = SignStr.trim().replaceAll("\r", "").replaceAll("\n", "").
                replaceAll("Signed successfully", "").trim();

        return SignStr;
    }

    //投标防偷窥项目--公钥大数的16进制编码
    public String tapPubToHex(String peerIP, String prikey, String pwd, String data, String outFormat) throws Exception {

        String pwdParam = "";
        if (!pwd.isEmpty()) pwdParam = " -p " + pwd;

//        将key保存至文件key.pem 解密SecretKey
//        shellExeCmd(peerIP, "echo " + prikey + " > " + destShellScriptDir + "key.pem");
        String pubStr = shExeAndReturn(peerIP, wtcliExePath + " id sdkid -i pub.pem");
        pubStr = pubStr.trim().replaceAll("\r", "").replaceAll("\n", "").
                replaceAll("sdkid is", "").trim();

        return pubStr;
    }

    //投标防偷窥项目--filePath数据解密
    public String tapDecryptFilePath(String peerIP, String prikey, String pwd, String filePath, String keySecret) throws Exception {

        String pwdParam = "";
        if (!pwd.isEmpty()) pwdParam = " -p " + pwd;

//        //将key保存至文件key.pem 解密SecretKey
//        shellExeCmd(peerIP, "echo " + prikey + " > " + destShellScriptDir + "key.pem");
        String SignStr = shExeAndReturn(peerIP, wtcliExePath + "util tap dec -f " + filePath + " -s " + keySecret + " -k key.pem");
        SignStr = SignStr.trim().replaceAll("\r", "").replaceAll("\n", "").
                replaceAll("Signed successfully", "").trim();

        return SignStr;
    }

}
