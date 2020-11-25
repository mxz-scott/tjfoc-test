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
        String a = UtilsClassKMS.a(response);
        //对称密钥加密
        String response1 = kms.encrypt(a, password, plainText);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取对称密钥解密参数
        String b = UtilsClassKMS.b(response1);
        String response2 = kms.decrypt(a, password, b);
        System.out.println("response2 = " + response2);
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
        String c = UtilsClassKMS.c(response);
        String d = UtilsClassKMS.d(response);
        //将获取的密钥参数加密转码
        System.out.println(d.substring(2));
        byte[] pub=Base64.getDecoder().decode(d);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(c, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String e = UtilsClassKMS.e(response1);
        System.out.println("e = " + e);
        //导入公钥
        String response2 = kms.getPublicKey(e, pubFormat);
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

        String c = UtilsClassKMS.c(response);
        String d = UtilsClassKMS.d(response);
        //将获取的密钥参数加密转码
        System.out.println(d.substring(2));
        byte[] pub=Base64.getDecoder().decode(d);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(c, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String e = UtilsClassKMS.e(response1);

        String response2 = kms.eccEncrypt(e, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String f = UtilsClassKMS.f(response2);

        String response3 = kms.eccDecrypt(e, password, f);
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
        String c = UtilsClassKMS.c(response);
        String d = UtilsClassKMS.d(response);
        //将获取的密钥参数加密转码
        System.out.println(d.substring(2));
        byte[] pub=Base64.getDecoder().decode(d);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(c, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String e = UtilsClassKMS.e(response1);

        String response2 = kms.eccSign(e, password, Digest);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String g = UtilsClassKMS.g(response2);

        String response3 = kms.eccVerify(e, Digest, g);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString("true"));
        //验签异常测试
        String response4 = kms.eccVerify(e, Digesterror, g);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("false"));

    }

    @Test
    public void changePwd_Test06() {
        String response = kms.createKey(keySpecSm4, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        
        String a = UtilsClassKMS.a(response);

        String response1 = kms.changePwd(a, password, newPwd);
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));

        //用新的密码去做一下加密解密，验证修改后的密码可用

        //用旧密码做下加解密，验证是否会失败
    }

    @Test
    public void changePwd_Test07() {
        String response = kms.createKey(keySpecSm2, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        String a = UtilsClassKMS.a(response);

        String response1 = kms.changePwd(a, password, newPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));

        //用新的密码去做一下加密解密，验证修改后的密码可用

        //用旧密码做下加解密，验证是否会失败

    }

    @Test
    public void eccChangePWDandEncrypt_Test08() throws Exception {

        String response = kms.createKey(keySpecSm2, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        String keyId = UtilsClassKMS.a(response);

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

        String f = UtilsClassKMS.f(response2);

        //解密
        String response3 = kms.eccDecrypt(keyId, newPwd, f);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }

}

