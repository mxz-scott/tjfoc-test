package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MixTxTest {

    public   final static int   SLEEPTIME=20*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();

    @Test
    public void test()throws Exception{
        Shell shellPeer1 = new Shell(PEER1IP, USERNAME, PASSWD);
        Shell shellPeer2 = new Shell(PEER2IP, USERNAME, PASSWD);
        Shell shellSDK=new Shell("10.1.3.247","root","root");
        int i=0;

        for(;;) {
            shellPeer1.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
            shellPeer2.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");

            startPeer(PEER1IP);
            startPeer(PEER2IP);
            Thread.sleep(SLEEPTIME*2);

            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);

            if(store.GetHeight().contains("Inconsistent data")){
                break;
//                i++;
//                shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
//                shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
//                Thread.sleep(6000);
//                assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
//                continue;
            }

        }
    }



@Test
    public void TestMultiTypeTx()throws Exception{
        String resp = store.GetHeight();

        Shell shellPeer1=new Shell(PEER1IP,USERNAME,PASSWD);
        shellPeer1.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer1.execute("cp "+ PTPATH + "peer/conf/basePkTm20s.toml "+ PTPATH +"peer/conf/base.toml");

        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer2.execute("cp "+ PTPATH + "peer/conf/basePkTm20s.toml "+ PTPATH +"peer/conf/base.toml");

        startPeer(PEER1IP);
        startPeer(PEER2IP);

        Thread.sleep(SLEEPTIME);

        //发送存证交易
        Date dt=new Date();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        String response2= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4= multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        String response5= multiSign.addissueaddress(PRIKEY1,ADDRESS1);

        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000","发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);
        log.info(MULITADD3+ "发行" + tokenTypeM + " token，数量为：" + amount);
        String data = "MULITADD3" + "发行" + tokenTypeM + " token，数量为：" + amount;
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
        jsonObject=JSONObject.fromObject(response7);
        String StoreHash7 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response8);
        String StoreHash8 = jsonObject.getJSONObject("Data").get("TxId").toString();


        //等待一个打包周期
        Thread.sleep(20000);

        response1=store.GetTransaction(StoreHash1);
        response2=store.GetTransaction(StoreHash2);
        response3=store.GetTransaction(StoreHash3);
        response4=store.GetTransaction(StoreHash4);
        response5=store.GetTransaction(StoreHash5);
        response6=store.GetTransaction(StoreHash6);
        response7=store.GetTransaction(StoreHash7);
        response8=store.GetTransaction(StoreHash8);

        String resp1 = store.GetHeight();

        int height =Integer.parseInt(JSONObject.fromObject(resp).getString("Data"));
        int height1=Integer.parseInt(JSONObject.fromObject(resp1).getString("Data"));
        assertEquals(height,height1-1);


        //恢复原始配置
        shellPeer1.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer1.execute("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml");
        shellPeer2.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer2.execute("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml");

        startPeer(PEER1IP);
        startPeer(PEER2IP);

        Thread.sleep(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));


    }

    public void startPeer(String peerIP)throws Exception{
        Shell shell1=new Shell(peerIP,USERNAME,PASSWD);
        Thread.sleep(2000);
        shell1.execute("sh "+PTPATH+"peer/start.sh");
    }


    public void TestTxTypeVersion() throws Exception{
        //此部分测试参考2.0交易类型文档进行测试，不测试0.9基本存证
        //存证类交易 Type 0 SubType 0 1
        //普通存证
        Date dt=new Date();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        String txHash = JSONObject.fromObject(response1).getString("Data");


        //隐私存证
        String data = "Testcx-" + UtilsClass.Random(2);
        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response = store.CreateStorePwd(data,map);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();


        //UTXO类交易 Type 1 SubType 10 11 13 14 15
        //单签发行
        String tokenTypeS = "TxTypeSOLOTC-"+ UtilsClass.Random(6);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000","单签ADDRESS1发行token "+tokenTypeS,ADDRESS1);

        //Docker类交易 Type 2 SubType 30 31 32

        //Admin类交易 Type 20 SubType 200 201 202 203
        String response2= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response4= multiSign.addissueaddress(PRIKEY1,ADDRESS6);



    }

    public void checkTransactionDetails(String...str)throws Exception{

    }
}
