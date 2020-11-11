package com.tjfintech.common.functionTest.KmsTest;

import com.alibaba.fastjson.JSON;
import com.gmsm.utils.GmUtils;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.TjParseEncryptionKey;
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
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);

        String s = mapType1.get("keyId").toString();
        System.out.println("s = " + s);
        //对称密钥加密
        String response1 = kms.encrypt(s, password, plainText);
        System.out.println("response1 = " + response1);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取对称密钥解密参数
        Map mapkey2 = JSON.parseObject(response1, Map.class);
        Map mapType3 = JSON.parseObject(mapkey2.get("data").toString(), Map.class);

        String x = mapType3.get("cipherText").toString();
        System.out.println("x = " + x);
        //对称密钥解密
        String response2 = kms.decrypt(s, password, x);
        System.out.println("response2 = " + response2);

        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
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

        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String s = mapType1.get("importToken").toString();
        System.out.println("s = " + s);
        String x = mapType1.get("encryptPublicKey").toString();
        System.out.println("x = " + x);
        //将获取的密钥参数加密转码
        System.out.println(x.substring(2));
        byte[] pub=Base64.getDecoder().decode(x);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(s, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        Map mapkey1 = JSON.parseObject(response1, Map.class);
        Map mapType2 = JSON.parseObject(mapkey1.get("data").toString(), Map.class);
        String z = mapType2.get("keyId").toString();
        System.out.println("z = " + z);
        //导入公钥
        String response2 = kms.getPublicKey(z, pubFormat);

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
        //获取导入密钥参数 s x
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String s = mapType1.get("importToken").toString();
        System.out.println("s =" + s);
        String x = mapType1.get("encryptPublicKey").toString();
        System.out.println("x = " + x);
        //将获取的密钥参数加密转码
        System.out.println(x.substring(2));
        byte[] pub=Base64.getDecoder().decode(x);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(s, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        Map mapkey1 = JSON.parseObject(response1, Map.class);
        Map mapType2 = JSON.parseObject(mapkey1.get("data").toString(), Map.class);
        String z = mapType2.get("keyId").toString();
        System.out.println("z = " + z);

        String response2 = kms.eccEncrypt(z, plainText);
        System.out.println("response = " + response2); //打印返回值
        Map mapType = JSON.parseObject(response2, Map.class);//把我的返回值转换成map
        System.out.println("mapType = " + mapType);//打印转换成map格式的返回值
        System.out.println(mapType.get("data"));

        Map mapType3 = JSON.parseObject(mapType.get("data").toString(), Map.class);
        System.out.println("====>" + mapType1.get("cipherText"));

        String v = mapType3.get("cipherText").toString();//从map里获取到cip的值 赋值给s
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        String response3 = kms.eccDecrypt(z, password, v);
        System.out.println("response1 = " + response3);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));

    }

    @Test
    public void eccSign_Test05() throws Exception{
        //获取非对称密钥导入参数
        String response = kms.getKey(keySpecSm2, pubFormat);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        //获取导入密钥参数 s x
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String s = mapType1.get("importToken").toString();
        System.out.println("s =" + s);
        String x = mapType1.get("encryptPublicKey").toString();
        System.out.println("x = " + x);
        //将获取的密钥参数加密转码
        System.out.println(x.substring(2));
        byte[] pub=Base64.getDecoder().decode(x);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(s, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        Map mapkey1 = JSON.parseObject(response1, Map.class);
        Map mapType2 = JSON.parseObject(mapkey1.get("data").toString(), Map.class);
        String z = mapType2.get("keyId").toString();
        System.out.println("z = " + z);

        String response2 = kms.eccSign(z, password, Digest);

        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        Map mapkey2 = JSON.parseObject(response2, Map.class);
        Map mapType3 = JSON.parseObject(mapkey2.get("data").toString(), Map.class);
        String h = mapType3.get("value").toString();
        System.out.println("h = " + h);

        String response3 = kms.eccVerify(z, Digest, h);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
    }

    @Test
    public void changePwd_Test06() {
        String response = kms.createKey(keySpecSm4, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);

        String s = mapType1.get("keyId").toString();
        System.out.println("s = " + s);

        String response1 = kms.changePwd(s, oldPwd, newPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
    }
}