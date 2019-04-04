package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
public class BeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();


    public void initTest()throws Exception{

        String toolPath="cd "+PTPATH+"toolkit;";
        String exeCmd="./toolkit permission ";

        SDKID=getSDKID();
        PEER1MAC=getMACAddr(PEER1IP,USERNAME,PASSWD).trim();
        PEER2MAC=getMACAddr(PEER2IP,USERNAME,PASSWD).trim();
        PEER3MAC=getMACAddr(PEER3IP,USERNAME,PASSWD).trim();

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
            if(stdout1.get(iFlag+1).contains("[1 2 3 4 5 6 7 8 9 10 21 22 23 24 25 211 212 221 222 223 224 231 232 233 234 235 236 251 252 253 254]")==false) {
                shellPeer1.execute(preCmd + "999");
                ArrayList<String> stdout = shellPeer1.getStandardOutput();
                resp = StringUtils.join(stdout1,"\n");
                log.info(resp);
                assertEquals(resp.contains(" [1 2 3 4 5 6 7 8 9 10 21 211 212 22 221 222 223 224 23 231 232 233 234 235 236 24 25 251 252 253 254]"),true);
                Thread.sleep(6000);
            }
        }
        else {
            shellPeer1.execute(preCmd + "999");
            ArrayList<String> stdout2 = shellPeer1.getStandardOutput();
            resp = StringUtils.join(stdout2,"\n");
            log.info(resp);
            assertEquals(resp.contains(" [1 2 3 4 5 6 7 8 9 10 21 211 212 22 221 222 223 224 23 231 232 233 234 235 236 24 25 251 252 253 254]"),true);
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
        assertThat(response4,containsString("200"));
        assertThat(response,containsString("200"));
        assertThat(response2,containsString("200"));
        assertThat(response3,containsString("200"));
        assertThat(response5,containsString("200"));
        assertThat(response6,containsString("200"));
        //2.0.1版本需要添加发行地址后才可以发行
        String response11= multiSign.addissueaddress(PRIKEY1,IMPPUTIONADD);
        String response12= multiSign.addissueaddress(PRIKEY1,MULITADD3);
        String response13= multiSign.addissueaddress(PRIKEY1,ADDRESS1);
        String response14=multiSign.addissueaddress(PRIKEY2,ADDRESS2);
        String response15= multiSign.addissueaddress(PRIKEY1,MULITADD4);
        String response16= multiSign.addissueaddress(PRIKEY1,MULITADD5);
        assertThat(response14,containsString("200"));
        assertThat(response11,containsString("200"));
        assertThat(response12,containsString("200"));
        assertThat(response13,containsString("200"));
        assertThat(response15,containsString("200"));
        assertThat(response16,containsString("200"));

    }


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
}
