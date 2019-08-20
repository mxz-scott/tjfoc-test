package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MixTxTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();

    @Before
    public void beforeConfig() throws Exception {
        if(bReg==false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            bf.createAdd();
            Thread.sleep(8000);
            bReg=true;
        }
    }

    @Test
    public void TestMultiTypeTx()throws Exception{
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6), CoreMatchers.containsString("200"));
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS1), CoreMatchers.containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6), CoreMatchers.containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS1), CoreMatchers.containsString("200"));

        Thread.sleep(6000);
        //设置打包时间为20s 使得各种类型的交易同时打包
        setAndRestartPeerList("cp "+ PeerPATH + "conf/basePkTm20s.toml "+ PeerPATH + "conf/" + PeerInfoConfig + ".toml");
        setAndRestartSDK(resetSDKConfig);
        String resp = store.GetHeight();

        //发送存证交易
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        String response2= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4= multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        String response5= multiSign.addissueaddress(PRIKEY1,ADDRESS1);

        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        log.info(ADDRESS1+"发行token "+tokenTypeS);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000",ADDRESS1+"发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);

        String data = IMPPUTIONADD + "发行" + tokenTypeM + " token，数量为：" + amount;
        log.info(data);
        String response7 = multiSign.issueToken(IMPPUTIONADD, tokenTypeM, amount, data);
        assertEquals("200",JSONObject.fromObject(response7).getString("State"));
        String Tx1 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response8 = multiSign.Sign(Tx1, PRIKEY5);

        assertThat(response1, CoreMatchers.containsString("200"));
        assertThat(response2, CoreMatchers.containsString("200"));
        assertThat(response3, CoreMatchers.containsString("200"));
        assertThat(response4, CoreMatchers.containsString("200"));
        assertThat(response5, CoreMatchers.containsString("200"));
        assertThat(response6, CoreMatchers.containsString("200"));
        assertThat(response7, CoreMatchers.containsString("200"));
        assertThat(response8, CoreMatchers.containsString("200"));


        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHash1 = jsonObject.getJSONObject("Data").get("Figure").toString();
        jsonObject=JSONObject.fromObject(response2);
        String StoreHash2 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response3);
        String StoreHash3 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response4);
        String StoreHash4 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response5);
        String StoreHash5 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response6);
        String StoreHash6 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response8);
        String StoreHash8 = jsonObject.getJSONObject("Data").get("TxId").toString();


        //等待一个打包周期
        sleepAndSaveInfo(20000,"等待一个打包周期");

        response1=store.GetTxDetail(StoreHash1);
        response2=store.GetTxDetail(StoreHash2);
        response3=store.GetTxDetail(StoreHash3);
        response4=store.GetTxDetail(StoreHash4);
        response5=store.GetTxDetail(StoreHash5);
        response6=store.GetTxDetail(StoreHash6);
        response8=store.GetTxDetail(StoreHash8);

        String resp1 = store.GetHeight();

        int height =Integer.parseInt(JSONObject.fromObject(resp).getString("Data"));
        int height1=Integer.parseInt(JSONObject.fromObject(resp1).getString("Data"));
//        assertEquals(height,height1-1);
        boolean bright = height1 > height ? true: false;
        assertEquals(true,bright);

    }
    @After
    public void  reset()throws Exception{
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
    }

}
