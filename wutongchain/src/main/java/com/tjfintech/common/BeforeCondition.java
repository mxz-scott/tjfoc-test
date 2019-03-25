package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
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

    /**
     * 创建归集地址
     * 第一个参数为私钥。后续...参数为地址
     */
    @Test
    public  void collAddressTest() throws Exception{
        Shell shell1=new Shell("10.1.3.240","root","root");

        String toolPath="cd /root/zll/permission/toolkit;";
        String exeCmd="./toolkit permission ";
        String peerIP="10.1.3.240:9300";
        String sdkID="29dd9b8931e7a82b5c4067b0c80a1d53eba100bb3625f580558b509f01132ada60c5fe45fed42a9699c686e3cdabcb22a3441583d230fd9fd0e1db4928f81cd4";
        String preCmd=toolPath+exeCmd+"-p "+peerIP+" -d "+sdkID+" -m ";

        shell1.execute(preCmd+"999");
        ArrayList<String> stdout = shell1.getStandardOutput();
        for (String str : stdout) {
            log.info(str);
            assertEquals(str.contains("失败"), false);
            //assertEquals(str.contains("FuncUpdatePeerPermission success:  true"),true);
        }

        Thread.sleep(6000);

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
    public void TC12_createAdd() {
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
