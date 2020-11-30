package com.tjfintech.common.functionTest.KmsTest;

import com.alibaba.fastjson.JSON;
import com.gmsm.utils.GmUtils;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.TjParseEncryptionKey;
import com.tjfintech.common.utils.UtilsClassKMS;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.Base64;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClassKMS.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
@Slf4j
public class GoKmsTest {
    public final static int SHORTSLEEPTIME = 3 * 1000;
    TestBuilder testBuilder = TestBuilder.getInstance();
    Kms kms = testBuilder.getKms();

    /***
     * 生成对称算法密钥
     * 对称密钥加密
     * 对称密钥解密
     */
    @Test
    public void createKey_Test01() {
        //生成对称密钥算法
        String response = kms.createKey(keySpecSm4, password);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
       //获取对称密钥加密参数
        String keyIdsm4 = UtilsClassKMS.getKeyIdSm4(response);
        //对称密钥加密
        String response1 = kms.encrypt(keyIdsm4, password, plainText);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取对称密钥解密参数
        String cipherText = UtilsClassKMS.getCipherText(response1);
        String response2 = kms.decrypt(keyIdsm4, password, cipherText);
        //验证加解密参数
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        assertThat(response2, containsString(plainText));
    }

    /***
     * 获取导入密钥参数
     * 导入密钥
     * 生成非对称算法密钥ID
     * 获取非对称密钥加密ID公钥
     */
    @Test
    public void getKey_Test02() throws  Exception{
        //生成非对称密钥导入参数 sm2
        String response = kms.getKey(keySpecSm2, pubFormat);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        String importToken = UtilsClassKMS.getImportToken(response);
        String encryptPublicKey = UtilsClassKMS.getEncryptPublicKey(response);
        //将获取的密钥参数加密转码
        System.out.println(encryptPublicKey.substring(2));
        byte[] pub=Base64.getDecoder().decode(encryptPublicKey);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(importToken, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response1);
        //导入公钥
        String response2 = kms.getPublicKey(keyIdsm2, pubFormat);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
    }


    @Test
    public void genRandom_Test03() {
        JSONObject data = new JSONObject();

        data.put(16, size);

        String Data = data.toString();
        log.info(Data);
        String response = kms.genRandom(size);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        assertThat(response, containsString("random"));
    }



    @Test
    public void eccEncrypt_Test04() throws Exception{
        //获取非对称密钥导入参数
        String response = kms.getKey(keySpecSm2, pubFormat);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        //获取导入密钥参数

        String importToken = UtilsClassKMS.getImportToken(response);
        String encryptPublicKey = UtilsClassKMS.getEncryptPublicKey(response);
        //将获取的密钥参数加密转码
        System.out.println(encryptPublicKey.substring(2));
        byte[] pub=Base64.getDecoder().decode(encryptPublicKey);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(importToken, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response1);

        String response2 = kms.eccEncrypt(keyIdsm2, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        String response3 = kms.eccDecrypt(keyIdsm2, password, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }

    @Test
    public void eccSign_Test05() throws Exception{
        //获取非对称密钥导入参数
        String response = kms.getKey(keySpecSm2, pubFormat);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        //获取导入密钥参数
        String importToken = UtilsClassKMS.getImportToken(response);
        String encryptPublicKey = UtilsClassKMS.getEncryptPublicKey(response);
        //将获取的密钥参数加密转码
        System.out.println(encryptPublicKey.substring(2));
        byte[] pub=Base64.getDecoder().decode(encryptPublicKey);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(importToken, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response1);

        String response2 = kms.eccSign(keyIdsm2, password, Digest);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String value = UtilsClassKMS.getValue(response2);

        String response3 = kms.eccVerify(keyIdsm2, Digest, value);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString("true"));
        //验签异常测试
        String response4 = kms.eccVerify(keyIdsm2, Digesterror, value);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("false"));

    }

    @Test
    public void changePwd_Test06() {
        //创建sm4账号
        String response = kms.createKey(keySpecSm4, password);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        
        String keyIdsm4 = UtilsClassKMS.getKeyIdSm4(response);
        //修改密码
        String response1 = kms.changePwd(keyIdsm4, password, newPwd);
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));

        //新密码加密
        String response2 = kms.encrypt(keyIdsm4, newPwd,plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        //新密码解密
        String cipherText = UtilsClassKMS.getCipherText(response2);
        String response3 = kms.decrypt(keyIdsm4, newPwd, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

        //旧密码加密
        String response4 = kms.encrypt(keyIdsm4, password,plainText);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("pin码与密钥不匹配,或请稍后再试"));

        //旧密码解密
        String response5 = kms.decrypt(keyIdsm4, password,cipherText);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("pin码与密钥不匹配,或请稍后再试"));
    }

    @Test
    public void changePwd_Test07() {
        String response = kms.createKey(keySpecSm2, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        String keyIdsm2 = UtilsClassKMS.getKeySm2(response);

        String response1 = kms.changePwd(keyIdsm2, password, newPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));
        //新密码加密，验证修改后的密码可用
        String response2 = kms.eccEncrypt(keyIdsm2, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        //新密码解密
        String cipherText = UtilsClassKMS.getCipherText(response2);
        String response3 = kms.eccDecrypt(keyIdsm2, newPwd, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));
        //旧密码加密,验证失败
        String response4 = kms.eccEncrypt(keyIdsm2, plainText);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("data"));

        String response5 = kms.eccDecrypt(keyIdsm2, password,cipherText);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("pin码与密钥不匹配,或请稍后再试"));

    }

    @Test
    public void eccChangePWDandEncrypt_Test08() throws Exception {

        String response = kms.createKey(keySpecSm2, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        String keyId = UtilsClassKMS.getKeySm2(response);

        //修改密码
        String response1 = kms.changePwd(keyId, password, oldPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));
        //再改一次密码
        response1 = kms.changePwd(keyId, oldPwd, newPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));

        //加密
        String response2 = kms.eccEncrypt(keyId, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        //解密
        String response3 = kms.eccDecrypt(keyId, newPwd, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }

    @Test
    public void buildinfo() {
        String response = kms.buildinfo();

        assertThat(response, containsString("200"));
        assertThat(response, containsString("2020年11月25日 10:56:08"));
        assertThat(response, containsString("bf0ae76381235ac9307173cf7079e7f911dd9e3d"));
        assertThat(response, containsString("go version go1.15.1 windows/amd64"));
        assertThat(response, containsString("1.0.1"));
    }

    @Test
    public void apihealth() {
        String response = kms.apihealth();

        assertThat(response, containsString("200"));
        assertThat(response, containsString("OK"));
    }

}

