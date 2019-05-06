package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfoc.utils.ReadFiletoByte.log;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
public class BeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();


    public void initTest()throws Exception{

        String toolPath="cd "+PTPATH+"toolkit;";
        String exeCmd="./toolkit permission ";

        SDKID=getSDKID();
        PEER1MAC=getMACAddr(PEER1IP,USERNAME,PASSWD).trim();
        PEER2MAC=getMACAddr(PEER2IP,USERNAME,PASSWD).trim();
        PEER3MAC=getMACAddr(PEER3IP,USERNAME,PASSWD).trim();
        PEER4MAC=getMACAddr(PEER4IP,USERNAME,PASSWD).trim();

        String preCmd=toolPath+exeCmd+"-p "+PEER1RPCPort+" -d "+SDKID+" -m ";
        String getPerm=toolPath+"./toolkit getpermission -p "+PEER1RPCPort;

        Shell shellPeer1=new Shell(PEER1IP,USERNAME,PASSWD);
        int iFlag=0;
        shellPeer1.execute(getPerm);
        ArrayList<String> stdout1 = shellPeer1.getStandardOutput();
        String resp = StringUtils.join(stdout1,"\n");
        log.info(resp);
        assertEquals(resp.contains("失败"), false);

        if(resp.contains(SDKID)) {
            for (String str1 : stdout1){
                if(str1.contains(SDKID))
                    break;
                iFlag++;
                //assertEquals(str.contains("FuncUpdatePeerPermission success:  true"),true);
            }
            log.info(stdout1.get(iFlag+1));
            if(stdout1.get(iFlag+1).contains(fullPerm)==false) {
                shellPeer1.execute(preCmd + "999");
                ArrayList<String> stdout = shellPeer1.getStandardOutput();
                resp = StringUtils.join(stdout1,"\n");
                log.info(resp);
                assertEquals(resp.contains(fullPerm),true);
                Thread.sleep(6000);
            }
        }
        else {
            shellPeer1.execute(preCmd + "999");
            ArrayList<String> stdout2 = shellPeer1.getStandardOutput();
            resp = StringUtils.join(stdout2,"\n");
            log.info(resp);
            assertEquals(resp.contains(fullPerm),true);
            Thread.sleep(6000);
        }
    }
    /**
     * 创建归集地址
     * 第一个参数为私钥。后续...参数为地址
     */
    @Test
    public  void collAddressTest() throws Exception{

        initTest();

        String response= multiSign.collAddress(PRIKEY1,IMPPUTIONADD);
        String response2= multiSign.collAddress(PRIKEY1,MULITADD3);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4=multiSign.collAddress(PRIKEY2,ADDRESS2);
        String response5= multiSign.collAddress(PRIKEY1,MULITADD4);
        String response6= multiSign.collAddress(PRIKEY1,MULITADD5);
        String response7= multiSign.collAddress(PRIKEY1,MULITADD7);
        String response8= multiSign.collAddress(PRIKEY1,MULITADD1);
        assertThat(response4,containsString("200"));
        assertThat(response,containsString("200"));
        assertThat(response2,containsString("200"));
        assertThat(response3,containsString("200"));
        assertThat(response5,containsString("200"));
        assertThat(response6,containsString("200"));
        assertThat(response7,containsString("200"));
        //2.0.1版本需要添加发行地址后才可以发行
        String response11= multiSign.addissueaddress(PRIKEY1,IMPPUTIONADD);
        String response12= multiSign.addissueaddress(PRIKEY1,MULITADD3);
        String response13= multiSign.addissueaddress(PRIKEY1,ADDRESS1);
        String response14=multiSign.addissueaddress(PRIKEY2,ADDRESS2);
        String response15= multiSign.addissueaddress(PRIKEY1,MULITADD4);
        String response16= multiSign.addissueaddress(PRIKEY1,MULITADD5);
        String response17= multiSign.addissueaddress(PRIKEY1,MULITADD7);

        String response18= multiSign.addissueaddress(PRIKEY1,MULITADD1);
        assertThat(response14,containsString("200"));
        assertThat(response11,containsString("200"));
        assertThat(response12,containsString("200"));
        assertThat(response13,containsString("200"));
        assertThat(response15,containsString("200"));
        assertThat(response16,containsString("200"));
        assertThat(response17,containsString("200"));


    }

//        /**
//     * 创建单签地址
//     *
//     */
//    @Test
//    public void TC12_createAdd() {
//        soloSign.genAddress(PUBKEY1);
//        soloSign.genAddress(PUBKEY2);
//        soloSign.genAddress(PUBKEY3);
//        soloSign.genAddress(PUBKEY4);
//        soloSign.genAddress(PUBKEY5);
//        soloSign.genAddress(PUBKEY6);
//        soloSign.genAddress(PUBKEY7);
//    }


    /**
     * 创建多签地址
     * 当数据库被清，库中没多签地址信息时候调用。
     */
    @Test
    public void createAdd() {
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY6);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        map.put("3", PUBKEY7);
        multiSign.genMultiAddress(M, map);
        M = 1;
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY3);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY3);
        map.put("2", PUBKEY4);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY4);
        map.put("2", PUBKEY5);
        multiSign.genMultiAddress(1, map);
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        multiSign.genMultiAddress(M, map);//34
    }

    /**
     * 测试用例T284的前提条件。发行对应token
     */

    public  void  T284_BeforeCondition(){
        String tokenType = "cx-chenxu";
        String amount="1000";
        //String amount = "1000";
        log.info(IMPPUTIONADD+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "IMPPUTIONADD" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY4);
    }

    public void updatePubPriKey()throws Exception{
        PRIKEY1 = getKeyPairsFromFile(certPath+"/keys1/key.pem");
        PRIKEY2 = getKeyPairsFromFile(certPath+"/keys2/key.pem");
        PRIKEY3 = getKeyPairsFromFile(certPath+"/keys3/key.pem");
        PRIKEY4 = getKeyPairsFromFile(certPath+"/keys4/key.pem");
        PRIKEY5 = getKeyPairsFromFile(certPath+"/keys5/key.pem");
        PRIKEY6 = getKeyPairsFromFile(certPath+"/keys6/key.pem");
        PRIKEY7 = getKeyPairsFromFile(certPath+"/keys7/key.pem");

        PUBKEY1 = getKeyPairsFromFile(certPath+"/keys1/pubkey.pem");
        PUBKEY2 = getKeyPairsFromFile(certPath+"/keys2/pubkey.pem");
        PUBKEY3 = getKeyPairsFromFile(certPath+"/keys3/pubkey.pem");
        PUBKEY4 = getKeyPairsFromFile(certPath+"/keys4/pubkey.pem");
        PUBKEY5 = getKeyPairsFromFile(certPath+"/keys5/pubkey.pem");
        PUBKEY6 = getKeyPairsFromFile(certPath+"/keys6/pubkey.pem");
        PUBKEY7 = getKeyPairsFromFile(certPath+"/keys7/pubkey.pem");

        ADDRESS1 =JSONObject.fromObject(soloSign.genAddress(PUBKEY1)).getJSONObject("Data").getString("Address");
        ADDRESS2 =JSONObject.fromObject(soloSign.genAddress(PUBKEY2)).getJSONObject("Data").getString("Address");
        ADDRESS3 =JSONObject.fromObject(soloSign.genAddress(PUBKEY3)).getJSONObject("Data").getString("Address");
        ADDRESS4 =JSONObject.fromObject(soloSign.genAddress(PUBKEY4)).getJSONObject("Data").getString("Address");
        ADDRESS5 =JSONObject.fromObject(soloSign.genAddress(PUBKEY5)).getJSONObject("Data").getString("Address");
        ADDRESS6 =JSONObject.fromObject(soloSign.genAddress(PUBKEY6)).getJSONObject("Data").getString("Address");
        ADDRESS7 =JSONObject.fromObject(soloSign.genAddress(PUBKEY7)).getJSONObject("Data").getString("Address");


        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        MULITADD1=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//123
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY6);
        MULITADD2=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//126
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        map.put("3", PUBKEY7);
        MULITADD3=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//167
        M = 1;
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        MULITADD4=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//12
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY3);
        MULITADD5=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//13
        map = new HashMap<>();
        map.put("1", PUBKEY3);
        map.put("2", PUBKEY4);
        MULITADD6=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//34
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        MULITADD7=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//34
        map = new HashMap<>();
        map.put("1", PUBKEY4);
        map.put("2", PUBKEY5);
        IMPPUTIONADD=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//45

//        log.info(PRIKEY1);
//        log.info("***************************************************************");
//        log.info(PRIKEY2);
//        log.info("***************************************************************");
//        log.info(PRIKEY3);
//        log.info("***************************************************************");
//        log.info(PRIKEY4);
//        log.info("***************************************************************");
//        log.info(PRIKEY5);
//        log.info("***************************************************************");
//        log.info(PRIKEY6);
//        log.info("***************************************************************");
//        log.info(PUBKEY1);
//        log.info("***************************************************************");
//        log.info(PUBKEY2);
//        log.info("***************************************************************");
//        log.info(PUBKEY3);
//        log.info("***************************************************************");
//        log.info(PUBKEY4);
//        log.info("***************************************************************");
//        log.info(PUBKEY5);
//        log.info("***************************************************************");
//        log.info(PUBKEY6);
//        log.info("***************************************************************");
//        log.info(PUBKEY7);
//        log.info("******:wq*********************************************************");

    }
}
