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

//   String keyId = "";

    /***
     * 生成对称算法密钥
     * 对称密钥加密
     * 对称密钥解密
     */
    @Test
    public void createKey_test01() {
        String response = kms.createKey(keySpec, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);

        String s = mapType1.get("keyId").toString();
        System.out.println("s = " + s);

        String response1 = kms.encrypt(s, password, plainText);
        System.out.println("response1 = " + response1);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));

        Map mapkey2 = JSON.parseObject(response1, Map.class);
        Map mapType3 = JSON.parseObject(mapkey2.get("data").toString(), Map.class);

        String x = mapType3.get("cipherText").toString();
        System.out.println("x = " + x);

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
        String response = kms.getKey(keySpec1, pubFormat);

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

        String response1 = kms.importKey(s, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));

        Map mapkey1 = JSON.parseObject(response1, Map.class);
        Map mapType2 = JSON.parseObject(mapkey1.get("data").toString(), Map.class);
        String z = mapType2.get("keyId").toString();
        System.out.println("z = " + z);

        String response2 = kms.getPublicKey(z, pubFormat);

        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
    }

    @Test
    public void getPublicKey_03Test() {

        String response = kms.getPublicKey(keyId, pubFormat);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
    }

    @Test
    public void genRandom_04Test() {
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
    public void eccEncrypt_05Test() {
        String response = kms.eccEncrypt(keyId, plainText);
        System.out.println("response = " + response); //打印返回值
        Map mapType = JSON.parseObject(response, Map.class);//把我的返回值转换成map
        System.out.println("mapType = " + mapType);//打印转换成map格式的返回值
        System.out.println(mapType.get("data"));

        Map mapType1 = JSON.parseObject(mapType.get("data").toString(), Map.class);
        System.out.println("====>" + mapType1.get("cipherText"));

        String s = mapType1.get("cipherText").toString();//从map里获取到cip的值 赋值给s
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        String response1 = kms.eccDecrypt(keyId, password, s);
        System.out.println("response1 = " + response1);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));


    }

    @Test
    public void eccDecrypt_06Test() {
        JSONObject data = new JSONObject();
        data.put("keyId", keyId);
        data.put("password", password);
        data.put("cipherText", cipherText);

        String Data = data.toString();
        log.info(Data);
        String response = kms.eccDecrypt(keyId, password, cipherText);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
    }

    @Test
    public void eccSign_07Test() {
        JSONObject data = new JSONObject();

        data.put("keyId", keyId);
        data.put("password", password);
        data.put("Digest", Digest);

        String Data = data.toString();
        log.info(Data);
        String response = kms.eccSign(keyId, password, Digest);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
    }

    @Test
    public void eccVerify_08Test() {
        JSONObject data = new JSONObject();

        data.put("keyId", keyId);
        data.put("Digest", Digest);
        data.put("value", value);

        String Data = data.toString();
        log.info(Data);
        String response = kms.eccVerify(keyId, Digest, value);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
    }

    @Test
    public void changePwd_09Test() {
        String response = kms.createKey(keySpec, password);

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