package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.conJGFileName;
import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

//import net.sf.json.JSONObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_JGData_RefObjParamExceptionScene {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);
    public int verTemp = 0;
    MinIOOperation mo = new MinIOOperation();
    String tempaccount_subject_ref,tempaccount_associated_account_ref,tempproduct_issuer_subject_ref;

    @Rule
    public TestName tm = new TestName();


    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
    }

    @Before
    public void BeforeTest()throws Exception{
        gdEquityCode = Random(20);
        gdCompanyID = "P1Re" + Random(8);

        tempaccount_subject_ref = account_subject_ref;
        tempaccount_associated_account_ref =account_associated_account_ref;
        tempproduct_issuer_subject_ref = product_issuer_subject_ref;
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        sleepAndSaveInfo(4000);//等待区块打块异步请求
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
        uf.updateBlockHeightParam(endHeight);


        //中间可能有做过重新赋值 执行完成后恢复原设置值
        account_subject_ref = tempaccount_subject_ref;
        account_associated_account_ref =tempaccount_associated_account_ref;
        product_issuer_subject_ref = tempproduct_issuer_subject_ref;

    }


    //挂牌登记 产品信息中部分必填字段没有填写或者为空

    /***
     * 测试必填字段 product_market_subject_ref
     * 第一次移除整个字段
     * 第二次字段填写为空
     * 第三次 正确填写
     * @throws Exception
     */
    @Test
    public void enterpriseRegisterParamExceptionTest() throws Exception {
        product_issuer_subject_ref = gdCompanyID;

        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        //第一次不填写
        prodInfo.remove("product_market_subject_ref");//不填写必填的发行场所主体对象

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000000,enSubInfo,
                        prodInfo,null,null);
        assertEquals("400",JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(4000);
        //检查涉及的所有对象版本信息
        String verSub = gdCF.getObjectLatestVer(gdCompanyID);
        String verProd = gdCF.getObjectLatestVer(gdEquityCode);
        assertEquals("-1",verSub);
        assertEquals("-1",verProd);

//        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
//        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(gdCompanyID,"0"),subjectType,"1");
//        //填充header content字段
//        enSubInfo.put("content",gdCF.constructContentTreeMap(subjectType,gdCompanyID,"0","create",String.valueOf(ts1)));
//        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
//        Boolean bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));
//        assertEquals("1检查数据是否一致" ,true,bSame);

        //检查OSS上存储的主体信息
        String storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/0","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/1","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的产品信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,gdEquityCode + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));



        //第二次填写为空
        prodInfo.put("product_market_subject_ref","");//填写空

        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000000,enSubInfo,
                prodInfo,null,null);
        assertEquals("400",JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(4000);
        //检查涉及的所有对象版本信息
        verSub = gdCF.getObjectLatestVer(gdCompanyID);
        verProd = gdCF.getObjectLatestVer(gdEquityCode);
        assertEquals("-1",verSub);
        assertEquals("-1",verProd);

//        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
//        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(gdCompanyID,"0"),subjectType,"1");
//        //填充header content字段
//        enSubInfo.put("content",gdCF.constructContentTreeMap(subjectType,gdCompanyID,"0","create",String.valueOf(ts1)));
//        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
//        Boolean bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));
//        assertEquals("1检查数据是否一致" ,true,bSame);

        //检查OSS上存储的主体信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/0","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的产品信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,gdEquityCode + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));



        //正确填写后重新挂牌
        prodInfo.put("product_market_subject_ref",product_market_subject_ref);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000000,enSubInfo,
                prodInfo,null,null);
        assertEquals("200",JSONObject.parseObject(response).getString("state"));
        String txId = JSONObject.parseObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        String verSub2 = gdCF.getObjectLatestVer(gdCompanyID);
        String verProd2 = gdCF.getObjectLatestVer(gdEquityCode);

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdCompanyID);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));


        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdEquityCode);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",prodType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-产品",true,gdCF.bCheckJGParams(mapChkKeys));


    }

    /***
     * 测试必填字段 account_depository_ref
     * 第一次移除整个字段
     * 第二次字段填写为空
     * 第三次 正确填写
     * @throws Exception
     */

    //开户时 账户中的开户机构主体引用 先未填写 再正确填写
    @Test
    public void createAccParamExceptionTest() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map enSubInfo = gdBC.init01PersonalSubjectInfo();
        Map accSH = gdBC.init02ShareholderAccountInfo();
        Map accFund = gdBC.init02FundAccountInfo();

        Boolean bSame = false;


        String cltNo = "test01" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        

        int gdClient = -1; //Integer.parseInt(gdCF.getObjectLatestVer(cltNo));//获取当前开户主体最新版本信息

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        accSH.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", accSH);

        //构造资金账户信息
        accFund.put("account_object_id", fundNo);  //更新账户对象标识字段

        //第一次删除整个字段
        accFund.remove("account_depository_ref");//先删除开户机构主体引用字段
        accFund.put("account_associated_account_ref", shareHolderNo);

        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);

        //构造个人/投资者主体信息
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        enSubInfo.put("subject_id", "");  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("400",JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(4000);
        //检查涉及的所有对象版本信息
        String verSub = gdCF.getObjectLatestVer(cltNo);
        String verSHAcc = gdCF.getObjectLatestVer(shareHolderNo);
        String verFundAcc = gdCF.getObjectLatestVer(fundNo);

        assertEquals("-1",verSub);
        assertEquals("-1",verSHAcc);
        assertEquals("-1",verFundAcc);


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
//        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(cltNo,"0"),subjectType,"2");
//        //填充header content字段
//        enSubInfo.put("content",gdCF.constructContentTreeMap(subjectType,cltNo,"0","create",String.valueOf(ts1)));
//        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
//        Boolean bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));
//        assertEquals("1检查数据是否一致" ,true,bSame);


//        //直接从minio上获取报送数据文件信息
//        Map getSHAccInfo = gdCF.constructJGDataFromStr(conJGFileName(shareHolderNo,"0"),accType,"1");
//        //填充header content 信息
//        accSH.put("content",gdCF.constructContentTreeMap(accType,shareHolderNo,"0","create",String.valueOf(ts2)));
//        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
//        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));
//        assertEquals("检查数据是否一致" ,true,bSame);

        //检查OSS上存储的主体信息
        String storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,cltNo + "/0","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,cltNo + "/1","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的证券账户信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,shareHolderNo + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的资金账户信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,fundNo + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));


        //第二次填写为空
        accFund.put("account_depository_ref", "");//传入空字段
        mapFundInfo.put("accountInfo", accFund);

        response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("400",JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(4000);
        //检查涉及的所有对象版本信息
        verSub = gdCF.getObjectLatestVer(cltNo);
        verSHAcc = gdCF.getObjectLatestVer(shareHolderNo);
        verFundAcc = gdCF.getObjectLatestVer(fundNo);

        assertEquals("-1",verSub);
        assertEquals("-1",verSHAcc);
        assertEquals("-1",verFundAcc);


//        直接从minio上通过对象标识+版本号的方式获取指定对象文件
//        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(cltNo,"0"),subjectType,"2");
//        //填充header content字段
//        enSubInfo.put("content",gdCF.constructContentTreeMap(subjectType,cltNo,"0","create",String.valueOf(ts1)));
//        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
//        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));
//        assertEquals("1检查数据是否一致" ,true,bSame);


//        //直接从minio上获取报送数据文件信息
//        Map getSHAccInfo = gdCF.constructJGDataFromStr(conJGFileName(shareHolderNo,"0"),accType,"1");
//        //填充header content 信息
//        accSH.put("content",gdCF.constructContentTreeMap(accType,shareHolderNo,"0","create",String.valueOf(ts2)));
//        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
//        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));
//        assertEquals("检查数据是否一致" ,true,bSame);

        //检查OSS上存储的主体信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,cltNo + "/0","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,cltNo + "/1","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的证券账户信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,shareHolderNo + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的资金账户信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,fundNo + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));


        //第三次正确填写后再次执行
        accFund.put("account_depository_ref", account_depository_ref);//正确填写开户机构主体引用字段
        accFund.put("account_associated_account_ref", "SH" + cltNo);//填写资金账户关联账户引用字段

        mapFundInfo.put("accountInfo", accFund);

        response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("200",JSONObject.parseObject(response).getString("state"));
        String txId = JSONObject.parseObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        account_associated_account_ref = "SH" + cltNo;
        //检查涉及的所有对象版本信息
        String verSub2 = gdCF.getObjectLatestVer(cltNo);
        String verSHAcc2 = gdCF.getObjectLatestVer(shareHolderNo);
        String verFundAcc2 = gdCF.getObjectLatestVer(fundNo);

        assertEquals("0",verSub2);
        assertEquals("0",verSHAcc2);
        assertEquals("0",verFundAcc2);

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",cltNo);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","2");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));


        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",shareHolderNo);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",accType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-产品",true,gdCF.bCheckJGParams(mapChkKeys));

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",fundNo);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",accType);
        mapChkKeys.put("subProdSubType","2");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-产品",true,gdCF.bCheckJGParams(mapChkKeys));
    }


    /***
     * 测试非必填字段 account_associated_account_ref
     * 第一次字段填写为空
     * 第二次正确填写
     * @throws Exception
     */
    @Test
    public void createAccParamException02Test() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map enSubInfo = gdBC.init01PersonalSubjectInfo();
        Map accSH = gdBC.init02ShareholderAccountInfo();
        Map accFund = gdBC.init02FundAccountInfo();

        Boolean bSame = false;

        String cltNo = "test01" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        int gdClient = -1; //Integer.parseInt(gdCF.getObjectLatestVer(cltNo));//获取当前开户主体最新版本信息

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        accSH.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", accSH);

        //构造资金账户信息
        accFund.put("account_object_id", fundNo);  //更新账户对象标识字段

        //第一次填写为空
        accFund.put("account_associated_account_ref","");//将账户关联账户引用填写为空字符串

        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);

        //构造个人/投资者主体信息
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        enSubInfo.put("subject_id", "");  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("400",JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(4000);
        //检查涉及的所有对象版本信息
        String verSub = gdCF.getObjectLatestVer(cltNo);
        String verSHAcc = gdCF.getObjectLatestVer(shareHolderNo);
        String verFundAcc = gdCF.getObjectLatestVer(fundNo);

        assertEquals("-1",verSub);
        assertEquals("-1",verSHAcc);
        assertEquals("-1",verFundAcc);


//        //直接从minio上获取报送数据文件信息
//        Map getSHAccInfo = gdCF.constructJGDataFromStr(conJGFileName(shareHolderNo,"0"),accType,"1");
//        //填充header content 信息
//        accSH.put("content",gdCF.constructContentTreeMap(accType,shareHolderNo,"0","create",String.valueOf(ts2)));
//        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
//        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));
//        assertEquals("检查数据是否一致" ,true,bSame);

        //检查OSS上存储的主体信息
        String storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,cltNo + "/0","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,cltNo + "/1","");
        assertEquals("未获取到更新的主体对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的证券账户信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,shareHolderNo + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));

        //检查OSS上存储的资金账户信息
        storeData2 = mo.getFileFromMinIO(minIOEP,jgBucket,fundNo + "/0","");
        assertEquals("未获取到更新的产品对象版本信息",true,storeData2.contains("错误"));



        //第二次正确填写后再次执行
        accFund.put("account_associated_account_ref", "SH" + cltNo);//正确填写开户机构主体引用字段

        mapFundInfo.put("accountInfo", accFund);

        response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("200",JSONObject.parseObject(response).getString("state"));
        String txId = JSONObject.parseObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        account_associated_account_ref = "SH" + cltNo;

        //检查涉及的所有对象版本信息
        String verSub2 = gdCF.getObjectLatestVer(cltNo);
        String verSHAcc2 = gdCF.getObjectLatestVer(shareHolderNo);
        String verFundAcc2 = gdCF.getObjectLatestVer(fundNo);

        assertEquals("0",verSub2);
        assertEquals("0",verSHAcc2);
        assertEquals("0",verFundAcc2);

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",cltNo);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","2");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));


        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",shareHolderNo);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",accType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-产品",true,gdCF.bCheckJGParams(mapChkKeys));

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",fundNo);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",accType);
        mapChkKeys.put("subProdSubType","2");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-产品",true,gdCF.bCheckJGParams(mapChkKeys));
    }
}
