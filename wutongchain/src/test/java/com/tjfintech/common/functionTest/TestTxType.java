package com.tjfintech.common.functionTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.performanceTest.StoreSemiTest.tokenType;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestTxType {

    public   final static int   SLEEPTIME=20*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    ContractTest ct =new ContractTest();
    UtilsClass utilsClass=new UtilsClass();
    String typeStore="0";
    String subTypeStore="0";
    String subTypePriStore="1";

    String versionStore="0";
    String versionSUTXO="0";
    String versionMUTXO="0";

    String typeUTXO="1";
    String subTypeIssue="10";
    String subTypeTransfer="11";
    String subTypeFrozen="13";
    String subTypeUnfrozen="14";
    String subTypeRecycle="15";

    String typeDocker="2";
    String subTypeCreateDocker="30";
    String subTypeDockerTx="32";
    String subTypeDeleteDocker="31";

    String typeAdmin="20";
    String subTypeAddColl="200";
    String subTypeDelColl="201";
    String subTypeAddIssue="202";
    String subTypeDelIssue="203";

    @Before
    public void beforeConfig() throws Exception {
        if(certPath!=""&& bReg==false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(SLEEPTIME);
            bReg=true;
        }
    }

    @Test
    public void testSDKConnections()throws Exception{
        Shell shellPeer1 = new Shell(PEER1IP, USERNAME, PASSWD);
        Shell shellPeer2 = new Shell(PEER2IP, USERNAME, PASSWD);
        Shell shellPeer4 = new Shell(PEER4IP, USERNAME, PASSWD);
        boolean bError=false;
        for(int i=0;i<10;i++) {

            log.info("**************--------------test times: "+i+"--------------**************");
            shellPeer1.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
            shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
            shellPeer4.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

            startPeer(PEER1IP);
            startPeer(PEER2IP);
            startPeer(PEER4IP);
            Thread.sleep(SLEEPTIME*2);

            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);



            if(store.GetHeight().contains("Inconsistent data") ||store.GetHeight().contains("rpc error: code = Unavailable desc = all SubConns are in TransientFailure") ){

                bError=true;
                break;

//                shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
//                shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
//                Thread.sleep(6000);
//                assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
//                continue;
            }
        }
        assertEquals(bError,false);
    }

    public void startPeer(String peerIP)throws Exception{
        Shell shell1=new Shell(peerIP,USERNAME,PASSWD);
        Thread.sleep(2000);
        shell1.execute("sh "+PTPATH+"peer/start.sh");
    }

    @Test
    public void checkStoreTx()throws Exception{

        //存证类交易 Type 0 SubType 0 1
        //创建普通存证
        Date dt=new Date();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
        String Data="TxType tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        log.info("普通存证数据："+Data);
        String response1=store.CreateStore(Data);

        //创建隐私存证
        String priData = "TxTypePri-" + UtilsClass.Random(2);
        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        log.info("隐私存证数据："+priData);
        String response2 = store.CreateStorePwd(priData,map);

        Thread.sleep(6000);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        checkTriMsg(txHash1,versionStore,typeStore,subTypeStore);
        checkStore(txHash1,Data,"store");


        //检查隐私存证信息
        //Thread.sleep(6000);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();
        checkTriMsg(txHash2,versionStore,typeStore,subTypePriStore);

        JSONObject jsonObjecttran=JSONObject.fromObject(store.GetTransaction(txHash2)).getJSONObject("Data");
        JSONObject jsonObjecttx=JSONObject.fromObject(store.GetTxDetail(txHash2)).getJSONObject("Data");

        //隐私存证storeData中为加密后数据，目前仅判断不包含明文原始data数据
        assertEquals(jsonObjecttran.getString("storeData").contains(priData), false);
        assertEquals("store",jsonObjecttran.getString("transactionType"));
        assertEquals(true,jsonObjecttran.getJSONObject("extra").isNullObject());//检查合约extra

        assertEquals(jsonObjecttx.getJSONObject("Store").getString("storeData").contains(priData), false);
        assertEquals("store",jsonObjecttx.getJSONObject("Store").getString("transactionType"));
        assertEquals(true,jsonObjecttx.getJSONObject("Store").getJSONObject("extra").isNullObject());//检查合约extra
    }
    @Test
    public void checkUTXOTx()throws Exception{
        //UTXO类交易 Type 1 SubType 10 11 13 14 15
        //单签发行
        String tokenTypeS = "TxTypeSOLOTC-"+ UtilsClass.Random(6);
        String siData="单签"+ADDRESS1+"发行token "+tokenTypeS;
        String amount="10000";
        log.info(siData);
        String response3= soloSign.issueToken(PRIKEY1,tokenTypeS,amount,siData,ADDRESS1);


        //多签发行
        String tokenTypeM = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount1 = "50000";
        String  mulData= "多签"+MULITADD3 + "发行给自己" + tokenTypeM + " token，数量为：" + amount1;
        log.info(mulData);
        String response51 = multiSign.issueToken(IMPPUTIONADD,tokenTypeM, amount1, mulData);
        assertThat(response51, containsString("200"));
        String Tx1 = JSONObject.fromObject(response51).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response52 = multiSign.Sign(Tx1, PRIKEY5);

        String tokenTypeM2 = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount12 = "50000";
        String  mulData2= "多签"+MULITADD3 + "发行给"+MULITADD7 +" "+ tokenTypeM2 + " token，数量为：" + amount12;
        log.info(mulData2);
        String response53 = multiSign.issueToken(IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12,mulData2);
        String Tx13 = JSONObject.fromObject(response53).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response54 = multiSign.Sign(Tx13, PRIKEY5);
        Thread.sleep(8000);

        //单签转账
        assertEquals(JSONObject.fromObject(soloSign.Balance(PRIKEY1,tokenTypeS)).getJSONObject("Data").getString("Total"),amount);
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenTypeS,"3000");
        String amountTransfer="3000";
        String tranferdata="transfer to "+ADDRESS3+" with amount "+amountTransfer;
        String response4= soloSign.Transfer(list,PRIKEY1,"transfer to "+ADDRESS3+" with amount 3000");

        //多签转账
        assertEquals(JSONObject.fromObject( multiSign.Balance(IMPPUTIONADD,PRIKEY4,tokenTypeM)).getJSONObject("Data").getString("Total"),amount1);
        String tranferAmount="3000";
        String transferData = IMPPUTIONADD+" 向 " + ADDRESS5 + " 转账 " + tranferAmount + " " +tokenTypeM;
        List<Map> listInit = utilsClass.constructToken(ADDRESS5, tokenTypeM, "3000");
        log.info(transferData);
        String response6 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, listInit);

        Thread.sleep(5000);

        //检查单签发行交易信息
        String txHash3 = JSONObject.fromObject(response3).getString("Data");
        checkTriMsg(txHash3,versionSUTXO,typeUTXO,subTypeIssue);
        JSONObject uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash3)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(siData,uxtoJson.getString("data"));
        checkFromTo(uxtoJson,ADDRESS1,ADDRESS1,tokenTypeS,amount);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTransaction(txHash3)).getJSONObject("Data"));
        checkFromTo(uxtoJson,ADDRESS1,ADDRESS1,tokenTypeS,amount);

        //检查单签转账交易信息
        String txHash4 = JSONObject.fromObject(response4).getString("Data");
        checkTriMsg(txHash4,versionSUTXO,typeUTXO,subTypeTransfer);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash4)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(tranferdata,uxtoJson.getString("data"));

        assertEquals(ADDRESS1,uxtoJson.getJSONArray("txRecords").getJSONObject(0).getString("From"));
        assertEquals(ADDRESS3,uxtoJson.getJSONArray("txRecords").getJSONObject(0).getString("To"));
        assertEquals(tokenTypeS,uxtoJson.getJSONArray("txRecords").getJSONObject(0).getString("TokenType"));
        assertEquals(amountTransfer,uxtoJson.getJSONArray("txRecords").getJSONObject(0).getString("Amount"));
        checkFromTo(uxtoJson,ADDRESS1,ADDRESS3,tokenTypeS,amountTransfer);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTransaction(txHash4)).getJSONObject("Data"));
        checkFromTo(uxtoJson,ADDRESS1,ADDRESS3,tokenTypeS,amountTransfer);

        //检查多签发行交易信息
        String txHash5 = JSONObject.fromObject(response52).getJSONObject("Data").get("TxId").toString();
        checkTriMsg(txHash5,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash5)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(mulData,uxtoJson.getString("data"));
        checkFromTo(uxtoJson,IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,amount1);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTransaction(txHash5)).getJSONObject("Data"));
        checkFromTo(uxtoJson,IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,amount1);

        String txHash51 = JSONObject.fromObject(response54).getJSONObject("Data").get("TxId").toString();
        checkTriMsg(txHash51,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash51)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(mulData2,uxtoJson.getString("data"));
        checkFromTo(uxtoJson,IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTransaction(txHash51)).getJSONObject("Data"));
        checkFromTo(uxtoJson,IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12);


        //检查多签转账交易信息
        String txHash6 = JSONObject.fromObject(response6).getJSONObject("Data").get("TxId").toString();
        checkTriMsg(txHash6,versionMUTXO,typeUTXO,subTypeTransfer);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash6)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(transferData,uxtoJson.getString("data"));
        checkFromTo(uxtoJson,IMPPUTIONADD,ADDRESS5,tokenTypeM,tranferAmount);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTransaction(txHash6)).getJSONObject("Data"));
        checkFromTo(uxtoJson,IMPPUTIONADD,ADDRESS5,tokenTypeM,tranferAmount);
    }

    public void checkFromTo(JSONObject jsonObject,String from,String to,String TokenType,String amount)throws Exception{
        assertEquals(from,jsonObject.getJSONArray("txRecords").getJSONObject(0).getString("From"));
        assertEquals(to,jsonObject.getJSONArray("txRecords").getJSONObject(0).getString("To"));
        assertEquals(TokenType,jsonObject.getJSONArray("txRecords").getJSONObject(0).getString("TokenType"));
        assertEquals(amount,jsonObject.getJSONArray("txRecords").getJSONObject(0).getString("Amount"));
    }
    @Test
    public void checkDockerTx()throws Exception{
        //Docker类交易 Type 2 SubType 30 31 32
        //创建合约
        log.info("创建合约"+ct.name);
        String response7 = ct.installTest();
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("Data").get("Figure").toString();
        Thread.sleep(30000);
        //发送合约交易initMobile
        log.info("发送合约交易initMobile");
        String response81 = ct.initMobileTest();
        Thread.sleep(6000);
        String txHash81 = JSONObject.fromObject(response81).getJSONObject("Data").get("Figure").toString();
        //发送合约交易querymobile
        log.info("发送合约交易querymobile");
        String response8 = ct.queryMobileTest("Mobile1");
        Thread.sleep(6000);
        String txHash8 = JSONObject.fromObject(response8).getJSONObject("Data").get("Figure").toString();

        //销毁合约
        log.info("销毁合约"+ct.name);
        String response9 = ct.destroyTest();
        Thread.sleep(6000);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("Data").get("Figure").toString();

        //检查合约创建交易信息
        checkTriMsg(txHash7,versionStore,typeDocker,subTypeCreateDocker);
        //Install chaincode [041801_2.0] success!
        assertEquals("Install chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
                store.GetTxDetail(txHash7)).getJSONObject("Data").getJSONObject("Contract").getString("message"));
        assertEquals("Install chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
                store.GetTransaction(txHash7)).getJSONObject("Data").getString("message"));


        //检查合约交易信息initMobile
        checkTriMsg(txHash81,versionStore,typeDocker,subTypeDockerTx);
        checkContractTx(txHash81,"initMobile","scDocker","200","1","Transaction excute success!");

        //querymobile
        checkTriMsg(txHash8,versionStore,typeDocker,subTypeDockerTx);
        checkContractTx(txHash8,"queryMobile","scDocker","200","1",
                "Transaction excute success!");
        assertThat(JSONObject.fromObject(store.GetTransaction(txHash8)).getJSONObject("Data").getJSONObject("contractResult").getString("payload"), containsString("Apple"));
        assertThat(JSONObject.fromObject(store.GetTxDetail(txHash8)).getJSONObject("Data").getJSONObject("Contract").getJSONObject("contractResult").getString("payload"), containsString("Apple"));

        //检查合约销毁交易信息
        checkTriMsg(txHash9,versionStore,typeDocker,subTypeDeleteDocker);
        //Delete chaincode [041801_2.0] success!
        assertEquals("Delete chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
                store.GetTxDetail(txHash9)).getJSONObject("Data").getJSONObject("Contract").getString("message"));
        assertEquals("Delete chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
                store.GetTransaction(txHash9)).getJSONObject("Data").getString("message"));
    }

    @Test
    public void checkAdminTx()throws Exception{
        //预先做删除归集地址操作，以便后续操作正常进行
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6),containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6),containsString("200"));
        Thread.sleep(6000);

        //Admin类交易 Type 20 SubType 200 201 202 203
        String response10= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response11= multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        Thread.sleep(6000);

        //添加归集地址交易信息检查
        String txHash10 = JSONObject.fromObject(response10).getString("Data");
        checkTriMsg(txHash10,versionStore,typeAdmin,subTypeAddColl);
        checkAdmin(txHash10,"collAddress","colladdress",ADDRESS6,"admin");

        //添加发行地址交易信息检查
        String txHash11 = JSONObject.fromObject(response11).getString("Data");
        checkTriMsg(txHash11,versionStore,typeAdmin,subTypeAddIssue);
        checkAdmin(txHash11,"issueAddress","issueaddress",ADDRESS6,"admin");

        //删除归集地址
        String response12= multiSign.delCollAddress(PRIKEY1,ADDRESS6);
        //删除发行地址
        String response13= multiSign.delissueaddress(PRIKEY1,ADDRESS6);
        Thread.sleep(6000);

        //检查删除归集地址交易信息
        String txHash12 = JSONObject.fromObject(response12).getString("Data");
        checkTriMsg(txHash12,versionStore,typeAdmin,subTypeDelColl);
        checkAdmin(txHash12,"collAddress","colladdress",ADDRESS6,"admin");

        //检查删除发行地址交易信息
        String txHash13 = JSONObject.fromObject(response13).getString("Data");
        checkTriMsg(txHash13,versionStore,typeAdmin,subTypeDelIssue);
        checkAdmin(txHash13,"issueAddress","issueaddress",ADDRESS6,"admin");
    }


    public void checkTriMsg(String hash,String version,String type,String subType)throws Exception{
        JSONObject jsonObject = JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data").getJSONObject("Header");
        assertEquals(version,jsonObject.getString("version"));
        assertEquals(type,jsonObject.getString("type"));
        assertEquals(subType,jsonObject.getString("subType"));

        JSONObject jsonObject2 = JSONObject.fromObject(store.GetTransaction(hash)).getJSONObject("Data").getJSONObject("header");
        assertEquals(version,jsonObject2.getString("version"));
        assertEquals(type,jsonObject2.getString("type"));
        assertEquals(subType,jsonObject2.getString("subType"));
    }

    public void checkContractTx(String hash,String method,String cttype,String ctResultStatus,String code,String Msg)throws Exception{
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("Contract");
        String sections=new String(decryptBASE64(jsonObject.getString("smartContract")));
        log.info(sections);
        assertEquals(ct.name,JSONObject.fromObject(sections).getString("Name"));//检查合约名称
        assertEquals(ct.version,JSONObject.fromObject(sections).getString("Version"));//检查合约Version

        String scArgs= new String(decryptBASE64(jsonObject.getJSONArray("smartContractArgs").getString(0)));
        log.info(jsonObject.getJSONArray("smartContractArgs").getString(0));
        assertThat(scArgs, containsString(method));
        assertEquals(cttype,jsonObject.getString("transactionType"));//检查合约类型scDocker
        assertEquals(ctResultStatus,jsonObject.getJSONObject("contractResult").getString("status"));//检查合约调用结果code status 200
        assertEquals(code,jsonObject.getString("code"));//检查合约交易结果code
        assertEquals(Msg,jsonObject.getString("message"));//检查合约交易结果message
        assertEquals(true,jsonObject.getJSONObject("writes").isNullObject());//检查合约writes
        assertEquals(true,jsonObject.getJSONObject("extra").isNullObject());//检查合约extra

        //检查其他字段为空


        assertEquals(true,jsonObjectOrg.getJSONObject("Store").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("UTXO").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("Admin").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("WVM").isNullObject());


        JSONObject jsonObject2 =JSONObject.fromObject(store.GetTransaction(hash)).getJSONObject("Data");
        JSONObject jsonObjectSC=JSONObject.fromObject(new String(decryptBASE64(jsonObject2.getString("smartContract"))));
        assertEquals(ct.name,jsonObjectSC.getString("Name"));//检查合约名称
        assertEquals(ct.version,jsonObjectSC.getString("Version"));//检查合约Version

        scArgs= new String(decryptBASE64(jsonObject2.getJSONArray("smartContractArgs").getString(0)));
        assertThat(scArgs, containsString(method));
        assertEquals(cttype,jsonObject2.getString("transactionType"));//检查交易类型
        assertEquals(ctResultStatus,jsonObject2.getJSONObject("contractResult").getString("status"));//检查合约调用结果code status 200
        assertEquals(code,jsonObject2.getString("code"));//检查合约交易结果code
        assertEquals(Msg,jsonObject2.getString("message"));//检查合约交易结果message
        assertEquals(true,jsonObject2.getJSONObject("writes").isNullObject());//检查合约writes
        assertEquals(true,jsonObject2.getJSONObject("extra").isNullObject());//检查合约extra
    }

    public void checkStore(String hash,String storeData,String transactionType)throws Exception{
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("Store");
        assertEquals(storeData,jsonObject.getString("storeData"));//检查存证数据
        assertEquals(transactionType,jsonObject.getString("transactionType"));//检查交易类型
        assertEquals(true,jsonObject.getJSONObject("extra").isNullObject());//检查extra

        //检查其他字段为空

        assertEquals(true,jsonObjectOrg.getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("UTXO").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("Admin").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("WVM").isNullObject());


        JSONObject jsonObject2 =JSONObject.fromObject(store.GetTransaction(hash)).getJSONObject("Data");
        assertEquals(storeData,jsonObject2.getString("storeData"));//检查存证数据
        assertEquals(transactionType,jsonObject2.getString("transactionType"));//检查交易类型
        assertEquals(true,jsonObject2.getJSONObject("extra").isNullObject());//检查extra

    }

    public void checkAdmin(String hash,String keywordTran,String keywordTxdetail,String Address,String txType)throws Exception{

        JSONObject jsonObjectOrg1 =JSONObject.fromObject(store.GetTransaction(hash)).getJSONObject("Data");
        JSONObject jsonObjectOrg2 =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");

        assertThat(jsonObjectOrg1.getJSONArray(keywordTran).getString(0),containsString(Address));
        assertEquals(txType,jsonObjectOrg1.getString("transactionType"));
        assertEquals(true,jsonObjectOrg1.getJSONObject("extra").isNullObject());//检查extra

        assertThat(jsonObjectOrg2.getJSONObject("Admin").getJSONArray(keywordTxdetail).getString(0),containsString(Address));
        assertEquals(txType,jsonObjectOrg2.getJSONObject("Admin").getString("transactionType"));
        assertEquals(true,jsonObjectOrg2.getJSONObject("Admin").getJSONObject("extra").isNullObject());//检查extra

        //检查其他字段为空
        assertEquals(true,jsonObjectOrg2.getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("UTXO").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("Store").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("WVM").isNullObject());
    }
}
