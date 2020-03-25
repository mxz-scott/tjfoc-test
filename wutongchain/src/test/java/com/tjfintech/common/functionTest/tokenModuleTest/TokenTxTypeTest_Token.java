package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TokenTxTypeTest_Token {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    Token tokenModule = testBuilder.getToken();
    SoloSign soloSign = testBuilder.getSoloSign();
    CommonFunc commonFunc = new CommonFunc();
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
    String subTypeRecycle="12";

    String typeAdmin="20";
    String subTypeAddColl="200";
    String subTypeDelColl="201";
    String subTypeAddIssue="202";
    String subTypeDelIssue="203";
    String subTypeFreezeToken="204";
    String subTypeRecoverToken="205";

    String zeroAddr="0000000000000000";


    @BeforeClass
    public static void beforeSetting()throws Exception
    {
        SDKADD = TOKENADD;
        BeforeCondition beforeCondition = new BeforeCondition();
        if(tokenAccount1.isEmpty()) {
            beforeCondition.createTokenAccount();
        }
        beforeCondition.tokenAddIssueCollAddr();
    }
    
    @Test
    public void checkStoreTx()throws Exception{

        /**|存证|0|
         * |基本存证|0|
         * |加密存证|1|
         */
        //SDKADD = TOKENADD; //设置sdk为token模块sdk
        //创建普通存证
        String Data="TxType tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        log.info("普通存证数据："+Data);
        String response1=tokenModule.tokenCreateStore(Data);
        String txHash1 = JSONObject.fromObject(response1).getString("data");
        
        //创建隐私存证

        Map<String,Object> map = new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);

        response1= tokenModule.tokenCreatePrivateStore(Data,map);
        String txHash2 = JSONObject.fromObject(response1).getString("data");

        sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");

        //SDKADD = rSDKADD;//查询使用sdk地址
        //检查普通存证信息
        JSONObject jsonObject = checkDataHeaderMsg(txHash1,versionStore,typeStore,subTypeStore);
        checkDataStore(jsonObject,Data);

        //检查隐私存证信息
        JSONObject jsonObjectPri = checkDataHeaderMsg(txHash2,versionStore,typeStore,subTypePriStore);
        String getDetailData = jsonObjectPri.getJSONObject("data").getJSONObject("Store").getString("StoreData");
        assertEquals(false,getDetailData.contains(Data));
        assertEquals(true,jsonObjectPri.getJSONObject("data").getJSONObject("Store").getJSONObject("Extra").isNullObject());//检查合约extra
    }

    @Test
    public void checkUTXOTx()throws Exception{

        /**|UTXO|1|
         * |UTXO发行|10|
         * |UTXO转账|11|
         * |UTXO回收|12|
         */
        SDKADD = TOKENADD; //设置sdk为token模块sdk

        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.tokenAddIssueCollAddr();

        //UTXO类交易 Type 1 SubType 10 11 12

        //单签发行给自己
        String tokenTypeS1 = "TxTypeSOLOTC_"+ UtilsClass.Random(6);
        String siData1= "单签" + tokenAccount1 + " 发行token " + tokenTypeS1;
        String amount1="10000";
        log.info(siData1);
        String response1 = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenTypeS1,amount1,siData1);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        String singleIssHash1 = JSONObject.fromObject(response1).getString("data");
        String tokenTypeS3 = "TxTypeSOLOTC_"+ UtilsClass.Random(6);
        String response11 = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenTypeS3,amount1,siData1);
        assertEquals("200",JSONObject.fromObject(response11).getString("state"));


        //单签发行给别人
        String tokenTypeS2 = "TxTypeSOLOTC_"+ UtilsClass.Random(6);
        String siData2 = "单签" + tokenAccount1 +"向" +  tokenAccount2 + " 发行token " + tokenTypeS2;
        String amount2 ="10000";
        log.info(siData2);
        String response2 = tokenModule.tokenIssue(tokenAccount1,tokenAccount2,tokenTypeS2,amount2,siData2);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        String singleIssHash2 = JSONObject.fromObject(response2).getString("data");


        //多签发行发行给自己
        String tokenTypeM1 = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amountM1 = "50000";
        String  mulDataM1= "多签"+ tokenMultiAddr1 + "发行给自己" + tokenTypeM1 + " token，数量为：" + amountM1;
        log.info(mulDataM1);
        String responseM1 = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenTypeM1, amountM1, mulDataM1);
        assertEquals("200",JSONObject.fromObject(responseM1).getString("state"));
        String multiIssHashM1 = JSONObject.fromObject(responseM1).getString("data");

        String tokenTypeM3 = "TxTypeMULTIC" + UtilsClass.Random(8);
        String responseM3 = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenTypeM3, amountM1, mulDataM1);
        assertEquals("200",JSONObject.fromObject(responseM3).getString("state"));


        //多签发行给别人
        String tokenTypeM2 = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amountM2 = "50000";
        String  mulDataM2 = "多签"+ tokenMultiAddr1 + "发行给" + tokenMultiAddr2 +" "+ tokenTypeM2 + " token，数量为：" + amountM2;
        log.info(mulDataM2);
        String responseM2 = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr2,tokenTypeM2,amountM2,mulDataM2);
        String multiIssHashM2 = JSONObject.fromObject(responseM2).getString("data");

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting");


        //单签转账
        assertEquals(JSONObject.fromObject(tokenModule.tokenGetBalance(tokenAccount1,"")).getJSONObject("data").getString(tokenTypeS1),amount1);
        assertEquals(JSONObject.fromObject(tokenModule.tokenGetBalance(tokenAccount1,"")).getJSONObject("data").getString(tokenTypeS3),amount1);
        String tranferSdata="transfer to "+tokenAccount3 + " with amount 3000";

        List<Map> listTFS = utilsClass.tokenConstructToken(tokenAccount3,tokenTypeS1,"3000.03");
        List<Map> listTFS2 = utilsClass.tokenConstructToken(tokenAccount3,tokenTypeS3,"3000",listTFS);

        List<Map> listST = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount3,tokenTypeS1,"3000.03");
        List<Map> listST2 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount1,tokenTypeS1,"6999.97",listST);//转回给自己7000
        List<Map> listST3 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount3,tokenTypeS3,"3000",listST2);
        List<Map> listST4 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount1,tokenTypeS3,"7000",listST3);//转回给自己7000

        String response4= tokenModule.tokenTransfer(tokenAccount1,tranferSdata,listTFS2);
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));
        String soTransfHash = JSONObject.fromObject(response4).getString("data");


        //多签转账
        assertEquals(JSONObject.fromObject( tokenModule.tokenGetBalance(tokenMultiAddr1,"")).getJSONObject("data").getString(tokenTypeM1),amountM1);
        assertEquals(JSONObject.fromObject( tokenModule.tokenGetBalance(tokenMultiAddr1,"")).getJSONObject("data").getString(tokenTypeM3),amountM1);
        String transferData = tokenMultiAddr1 + " 向 " + tokenMultiAddr3 + " 转账3000";
        log.info(transferData);
        List<Map> listTFM = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenTypeM1,"0.05");
        List<Map> listTFM2 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenTypeM3,"3000",listTFM);

        List<Map> listMT = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr3,tokenTypeM1,"0.05");
        List<Map> listMT2 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr1,tokenTypeM1,"49999.95",listMT);
        List<Map> listMT3 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr3,tokenTypeM3,"3000",listMT2);
        List<Map> listMT4 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr1,tokenTypeM3,"47000",listMT3);

        String response6 = tokenModule.tokenTransfer(tokenMultiAddr1,transferData,listTFM2);
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        String muTransfHash = JSONObject.fromObject(response6).getString("data");

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting");

        //单签回收bylist
        String recySoloAmount="600.04";
        log.info("单签回收");
        String desInfo1 = "destory 1111";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenTypeS1, recySoloAmount);
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount3, tokenTypeS3, "0.07",list);

        List<Map> listSD = commonFunc.constructUTXOTxDetailList(tokenAccount1,zeroAccount,tokenTypeS1,recySoloAmount);
        List<Map> listSD2 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenTypeS3,"0.07",listSD);
        List<Map> listSD3 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount1,tokenTypeS1,"6399.93",listSD2);//转回自己
        List<Map> listSD4 = commonFunc.constructUTXOTxDetailList(tokenAccount3,tokenAccount3,tokenTypeS3,"2999.93",listSD3);//转回自己

        String RecycleSoloInfo = tokenModule.tokenDestoryByList(list2,desInfo1);
        String soDesHash = JSONObject.fromObject(RecycleSoloInfo).getString("data");

        //多签回收 bytokentype
        log.info("多签回收");
        String desInfo2 = "destory 2222";
        List<Map> listMD = commonFunc.constructUTXOTxDetailList(tokenMultiAddr3,zeroAccount,tokenTypeM1,"0.05");
        List<Map> listMD2 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,zeroAccount,tokenTypeM1,"49999.95",listMD);

        String RecycleMultiInfo = tokenModule.tokenDestoryByTokenType(tokenTypeM1,desInfo2);
        String muDesHash = JSONObject.fromObject(RecycleMultiInfo).getJSONObject("data").getString("hash");
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting");


        //检查单签发行交易信息
        JSONObject jsonObject = checkDataHeaderMsg(singleIssHash1,versionSUTXO,typeUTXO,subTypeIssue);
        JSONObject uxtoJson= jsonObject.getJSONObject("data").getJSONObject("UTXO");
        assertEquals(siData1,uxtoJson.getString("Data"));
        assertEquals(1,uxtoJson.getJSONArray("Records").size());
        checkFromTo(uxtoJson,tokenAccount1,tokenAccount1,tokenTypeS1,amount1,0);

        uxtoJson.clear();
        jsonObject.clear();
        jsonObject = checkDataHeaderMsg(singleIssHash2,versionSUTXO,typeUTXO,subTypeIssue);
        uxtoJson= jsonObject.getJSONObject("data").getJSONObject("UTXO");
        assertEquals(siData2,uxtoJson.getString("Data"));
        assertEquals(1,uxtoJson.getJSONArray("Records").size());
        checkFromTo(uxtoJson,tokenAccount1,tokenAccount2,tokenTypeS2,amount2,0);


        //检查单签转账交易信息
        JSONObject jsonObject1 = checkDataHeaderMsg(soTransfHash,versionSUTXO,typeUTXO,subTypeTransfer);
        assertEquals(tranferSdata,jsonObject1.getJSONObject("data").getJSONObject("UTXO").getString("Data"));

        commonFunc.checkListArray(listST4,jsonObject1.getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records"));


        //检查多签发行交易信息
        JSONObject jsonObject2 = checkDataHeaderMsg(multiIssHashM1,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson.clear();
        uxtoJson= jsonObject2.getJSONObject("data").getJSONObject("UTXO");
        assertEquals(mulDataM1,uxtoJson.getString("Data"));
        assertEquals(1,uxtoJson.getJSONArray("Records").size());
        checkFromTo(uxtoJson,tokenMultiAddr1,tokenMultiAddr1,tokenTypeM1,amountM1,0);


        jsonObject2 = checkDataHeaderMsg(multiIssHashM2,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson.clear();
        uxtoJson= jsonObject2.getJSONObject("data").getJSONObject("UTXO");
        assertEquals(mulDataM2,uxtoJson.getString("Data"));
        assertEquals(1,uxtoJson.getJSONArray("Records").size());
        checkFromTo(uxtoJson,tokenMultiAddr1,tokenMultiAddr2,tokenTypeM2,amountM2,0);


        //检查多签转账交易信息
        JSONObject jsonObject3 = checkDataHeaderMsg(muTransfHash,versionMUTXO,typeUTXO,subTypeTransfer);
        uxtoJson.clear();
        uxtoJson = jsonObject3.getJSONObject("data").getJSONObject("UTXO");
        assertEquals(transferData,uxtoJson.getString("Data"));
        commonFunc.checkListArray(listMT4,uxtoJson.getJSONArray("Records"));


        //检查单签回收交易信息
        JSONObject jsonObject4 = checkDataHeaderMsg(soDesHash,versionMUTXO,typeUTXO,subTypeRecycle);
        uxtoJson.clear();
        log.info("****************");
        uxtoJson = jsonObject4.getJSONObject("data").getJSONObject("UTXO");
        log.info("list array:" + listSD4.toString()) ;
        commonFunc.checkListArray(listSD4,uxtoJson.getJSONArray("Records"));

        //检查多签回收交易信息
        JSONObject jsonObject5 = checkDataHeaderMsg(muDesHash,versionMUTXO,typeUTXO,subTypeRecycle);
        uxtoJson.clear();
        uxtoJson= jsonObject5.getJSONObject("data").getJSONObject("UTXO");
        commonFunc.checkListArray(listMD2,uxtoJson.getJSONArray("Records"));
    }

    public void checkFromTo(JSONObject jsonObject,String from,String to,String TokenType,String amount,int index)throws Exception{
        assertEquals(from,jsonObject.getJSONArray("Records").getJSONObject(index).getString("From"));
        assertEquals(to,jsonObject.getJSONArray("Records").getJSONObject(index).getString("To"));
        assertEquals(TokenType,jsonObject.getJSONArray("Records").getJSONObject(index).getString("TokenType"));
        assertEquals(amount,jsonObject.getJSONArray("Records").getJSONObject(index).getString("Amount"));
    }



    @Test
    public void checkAdminTx()throws Exception{

        SDKADD = TOKENADD;//将地址设置为token模块地址
        //发行token
        String issueToken = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,"100");
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting");
        assertEquals("100",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr1,"")).getJSONObject("data").getString(issueToken));

        //预先做删除归集地址、删除发行地址操作、解除token锁定，以便后续操作正常进行
        assertThat(tokenModule.tokenDelCollAddr(tokenAccount1),containsString("200"));
        assertThat(tokenModule.tokenDelMintAddr(tokenAccount1),containsString("200"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting");

        //Admin类交易 Type 20 SubType 200 201 202 203
        String response10= tokenModule.tokenAddCollAddr(tokenAccount1);
        String response11= tokenModule.tokenAddMintAddr(tokenAccount1);
        //冻结token
        String response14 = tokenModule.tokenFreezeToken(issueToken);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting");

        //删除归集地址
        String response12= tokenModule.tokenDelCollAddr(tokenAccount1);
        //删除发行地址
        String response13= tokenModule.tokenDelMintAddr(tokenAccount1);
        //解除冻结token
        String response15 = tokenModule.tokenRecoverToken(issueToken);

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting");

        //SDKADD = rSDKADD;//将地址设置为SDK地址
        //添加归集地址交易信息检查
        String txHash10 = JSONObject.fromObject(response10).getString("data");
        JSONObject jsonObject2 = checkDataHeaderMsg(txHash10,versionStore,typeAdmin,subTypeAddColl);
        checkAdmin(jsonObject2,"CollAddress",tokenAccount1);

        //添加发行地址交易信息检查
        String txHash11 = JSONObject.fromObject(response11).getString("data");
        JSONObject jsonObject3 = checkDataHeaderMsg(txHash11,versionStore,typeAdmin,subTypeAddIssue);
        checkAdmin(jsonObject3,"IssueAddress",tokenAccount1);

        //检查删除归集地址交易信息
        String txHash12 = JSONObject.fromObject(response12).getString("data");
        JSONObject jsonObject = checkDataHeaderMsg(txHash12,versionStore,typeAdmin,subTypeDelColl);
        checkAdmin(jsonObject,"CollAddress",tokenAccount1);

        //检查删除发行地址交易信息
        String txHash13 = JSONObject.fromObject(response13).getString("data");
        JSONObject jsonObject1 = checkDataHeaderMsg(txHash13,versionStore,typeAdmin,subTypeDelIssue);
        checkAdmin(jsonObject1,"IssueAddress",tokenAccount1);

        //冻结token交易信息检查
        String txHash31 = JSONObject.fromObject(response14).getString("data");
        JSONObject jsonObject4 = checkDataHeaderMsg(txHash31,versionStore,typeAdmin,subTypeFreezeToken);
        assertEquals(issueToken,jsonObject4.getJSONObject("data").getJSONObject("Admin").getString("FreezeToken"));

        //解除冻结token
        String txHash41 = JSONObject.fromObject(response15).getString("data");
        JSONObject jsonObject5 = checkDataHeaderMsg(txHash41,versionStore,typeAdmin,subTypeRecoverToken);
        assertEquals(issueToken,jsonObject5.getJSONObject("data").getJSONObject("Admin").getString("RecoverToken"));

    }


    public JSONObject checkDataHeaderMsg(String hash,String version,String type,String subType)throws Exception{
        log.info("hash:"+hash);
        
        JSONObject objectDetail = JSONObject.fromObject(tokenModule.tokenGetTxDetail(hash));
        JSONObject jsonObject = objectDetail.getJSONObject("data").getJSONObject("Header");

        assertEquals(version,jsonObject.getString("Version"));
        assertEquals(type,jsonObject.getString("Type"));
        assertEquals(subType,jsonObject.getString("SubType"));
        assertEquals(hash,jsonObject.getString("TransactionHash"));

        return objectDetail;
    }



    public void checkDataStore(JSONObject jsonDetail,String storeData)throws Exception{
        assertEquals(storeData,jsonDetail.getJSONObject("data").getJSONObject("Store").getString("StoreData"));//检查存证数据
        assertEquals(true,jsonDetail.getJSONObject("data").getJSONObject("Extra").isNullObject());//检查extra


        assertEquals(true,jsonDetail.getJSONObject("data").getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonDetail.getJSONObject("data").getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonDetail.getJSONObject("data").getJSONObject("UTXO").isNullObject());
        assertEquals(true,jsonDetail.getJSONObject("data").getJSONObject("Admin").isNullObject());
        assertEquals(true,jsonDetail.getJSONObject("data").getJSONObject("WVM").isNullObject());

    }

    public void checkAdmin(JSONObject jsonObject,String keywordTxdetail,String checkstr)throws Exception{

        JSONObject jsonObjectOrg2 =jsonObject.getJSONObject("data");
        assertThat(jsonObjectOrg2.getJSONObject("Admin").getJSONArray(keywordTxdetail).getString(0),containsString(checkstr));
        assertEquals(true,jsonObjectOrg2.getJSONObject("Admin").getJSONObject("extra").isNullObject());//检查extra

        //检查其他字段为空
        assertEquals(true,jsonObjectOrg2.getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("UTXO").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("Store").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("WVM").isNullObject());
    }

    //@AfterClass
    public static void resetAddr()throws Exception{
        //SDKADD = TOKENADD;
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.tokenAddIssueCollAddr();
    }
}
