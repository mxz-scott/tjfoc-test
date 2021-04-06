package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.conJGFileName;
import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j

/***
 * 2021/1/7 确认异步场景下需要确认交易上链后才能正确判断预期 连续执行时不会报错 此部分考虑由业务端控制
 */
public class GDV2_JGData_CheckJGHeaderPrinciple_Test {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);


    @Rule
    public TestName tm = new TestName();

//    @BeforeClass
    public static void Before()throws Exception{
        TestBuilder tbTemp = TestBuilder.getInstance();
        Store storeTemp =tbTemp.getStore();
        beginHeigh = Integer.parseInt(JSONObject.fromObject(storeTemp.GetHeight()).getString("data"));
        start = (new Date()).getTime();

        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
        gdEquityCode = "updateTest" + Random(12);
    }

    @Before
    public void reset()throws Exception{
        gdCompanyID = "PrincipleSub" + Random(5);
        gdEquityCode = "PrincipleProd" + Random(5);
    }


    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
        uf.updateBlockHeightParam(endHeight);
    }

    @Test
    public void TCN031_ObjectId_sameSubDiffProd() throws Exception {
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
                prodInfo,null,null);
        assertEquals("200", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));

        prodInfo = gdBF.init03BondProductInfo();
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
                null,prodInfo,null);
        assertEquals("400", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));
        assertEquals("此产品对象标识[" + gdEquityCode + "]对应的产品在系统中已经存在",
                com.alibaba.fastjson.JSONObject.parseObject(response).getString("message"));

        prodInfo = gdBF.init03FundProductInfo();
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
                null,null,prodInfo);
        assertEquals("400", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));
        assertEquals("此产品对象标识[" + gdEquityCode + "]对应的产品在系统中已经存在",
                com.alibaba.fastjson.JSONObject.parseObject(response).getString("message"));
    }
    @Test
    public void TCN032_ObjectId_diffSubSameProd() throws Exception {
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
                prodInfo,null,null);
        assertEquals("200", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        gdCompanyID = "errSame" +  Random(6);
        enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
                prodInfo,null,null);
        assertEquals("400", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));
        Boolean bResult = response.contains("%!(EXTRA *errors.errorString=操作类型为[create],但根据对象标识[" + gdEquityCode +
                "]查询到版本不为0,请检查此对象标识是否已经存在") ||
                response.contains("此产品对象标识[" + gdEquityCode + "]对应的产品在系统中已经存在");
        assertEquals("检查返回信息是否包含以上两种之一",true,bResult);

//        assertEquals("此产品对象标识[" + gdEquityCode + "]对应的产品在系统中已经存在",
//                com.alibaba.fastjson.JSONObject.parseObject(response).getString("message"));
    }

    @Test
    public void TCN041_ObjectId_diffSubSameSHFundAcc() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map enSubInfo = gdBC.init01PersonalSubjectInfo();
        Map accSH = gdBC.init02ShareholderAccountInfo();
        Map accFund = gdBC.init02FundAccountInfo();

        String cltNo = "accOBJ0" + Random(8);
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
        accFund.put("account_associated_account_ref", shareHolderNo);
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);


        //构造个人/投资者主体信息
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        enSubInfo.put("subject_id", "");  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("200", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        cltNo = "accOBJ0" + Random(5);
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("400", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));
    }

    @Test
    public void TCN041_ObjectId_sameSubDiffSHAcc() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map enSubInfo = gdBC.init01PersonalSubjectInfo();
        Map accSH = gdBC.init02ShareholderAccountInfo();
        Map accFund = gdBC.init02FundAccountInfo();

        String cltNo = "accOBJ1" + Random(6);
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
        accFund.put("account_associated_account_ref", shareHolderNo);
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);

        //构造个人/投资者主体信息
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        enSubInfo.put("subject_id", "");  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("200", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        //变更股权账户对象标识
        shareHolderNo = shareHolderNo + "12";
        accSH.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", accSH);
        response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("400", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));
        Boolean bResult = response.contains("%!(EXTRA *errors.errorString=操作类型为[create],但根据对象标识[" +
                fundNo + "]查询到版本不为0,请检查此对象标识是否已经存在)") ||
                response.contains("资金账号对象标识[" + fundNo + "]在系统中已经存在");
        assertEquals("检查返回信息是否包含以上两种之一",true,bResult);
    }

    @Test
    public void TCN042_ObjectId_sameSubDiffFundAcc() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map enSubInfo = gdBC.init01PersonalSubjectInfo();
        Map accSH = gdBC.init02ShareholderAccountInfo();
        Map accFund = gdBC.init02FundAccountInfo();

        String cltNo = "accOBJ2" + Random(6);
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
        accFund.put("account_associated_account_ref", shareHolderNo);
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);

        //构造个人/投资者主体信息
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        enSubInfo.put("subject_id", "");  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("200", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        //变更资金账户对象标识
        fundNo = fundNo + "12";
        accFund.put("account_object_id", fundNo);  //更新账户对象标识字段
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);
        response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("400", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));

        Boolean bResult = response.contains("%!(EXTRA *errors.errorString=操作类型为[create],但根据对象标识["+ shareHolderNo +
                "]查询到版本不为0,请检查此对象标识是否已经存在") ||
                response.contains("股东账号对象标识[" + shareHolderNo + "]在系统中已经存在");
        assertEquals("检查返回信息是否包含以上两种之一",true,bResult);

//        assertEquals("股东账号对象标识[" + shareHolderNo + "]在系统中已经存在",
//                com.alibaba.fastjson.JSONObject.parseObject(response).getString("message"));
        //此处需增加报送数据规则校验
    }

    @Test
    public void TCN043_ObjectId_sameSubDiffSHFundAcc() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map enSubInfo = gdBC.init01PersonalSubjectInfo();
        Map accSH = gdBC.init02ShareholderAccountInfo();
        Map accFund = gdBC.init02FundAccountInfo();

        String cltNo = "accOBJ2" + Random(6);
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
        accFund.put("account_associated_account_ref", shareHolderNo);
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);

        //构造个人/投资者主体信息
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        enSubInfo.put("subject_id", "");  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("200", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        //变更资金账户和股权账户对象标识
        shareHolderNo = shareHolderNo + "12";
        accSH.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", accSH);

        fundNo = fundNo + "12";
        accFund.put("account_object_id", fundNo);  //更新账户对象标识字段
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);
        response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);
        assertEquals("200", com.alibaba.fastjson.JSONObject.parseObject(response).getString("state"));
        //此处需增加报送数据规则校验
    }


//    @Test
    public void check2()throws Exception{
        MinIOOperation mo = new MinIOOperation();
        for(int i=500;i<=554;i++) {
            Map temp = gdCF.findDataInBlock(i, "gdCmpyId01z3k4gF");
            String storeData = temp.get("storeData").toString();
            log.info(storeData);

        }
    }

}
