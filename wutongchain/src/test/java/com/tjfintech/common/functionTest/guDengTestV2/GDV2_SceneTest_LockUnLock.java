package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_SceneTest_LockUnLock {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
        register_event_type = "1";
    }

    @Before
    public void IssueEquity()throws Exception {
        bizNoTest = "test" + Random(12);
        gdCompanyID = CNKey + "Sub4_" + Random(4);
        gdEquityCode = CNKey + "Token4_" + Random(4);

        register_product_ref = gdEquityCode;

        //重新创建账户
//        gdAccClientNo1 = "No000" + Random(10);
//        gdAccClientNo2 = "No100" + Random(10);
//        gdAccClientNo3 = "No200" + Random(10);
//        gdAccClientNo4 = "No300" + Random(10);
//        gdAccClientNo5 = "No400" + Random(10);
//        gdAccClientNo6 = "No500" + Random(10);
//        gdAccClientNo7 = "No600" + Random(10);
//        gdAccClientNo8 = "No700" + Random(10);
//        gdAccClientNo9 = "No800" + Random(10);
//       gdAccClientNo10 = "No900" + Random(10);
//
//       GDBeforeCondition gdBC = new GDBeforeCondition();
//       gdBC.gdCreateAccout();

//       sleepAndSaveInfo(3000);

        //发行
        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
    }
//    @After
    public void DestroyEquityAndAcc()throws Exception{
        //查询企业所有股东持股情况
        String response = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        String response10 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        String response11 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        String response12 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        String response13 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        String response14 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        String response15 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        String response16 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo7);
        String response17 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo8);
        String response18 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo9);
        String response19 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo10);


        //依次回收

        //依次销户

    }

    /***
     * 发行A
     * 冻结超出总余额
     * 回收B
     * 销户C
     */

    @Test
    public void lock()throws Exception{
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,5000,0);
    }



    /***
     * 全部冻结后 在冻结有效期外转账
     */

    @Test
    public void lock_OutOfCutOffDate()throws Exception{

        String response = "";
        //全部冻结
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2020-09-02",true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount6,0,gdEquityCode,true);

    }

    /***
     * 多次冻结
     */

    @Test
    public void multiLock()throws Exception{

        String response = "";
        for(int i =0 ;i<20;i++) {
            //全部冻结
            String bizNoTemp = "2000" + Random(12);
            uf.lock(bizNoTemp, gdAccount1, gdEquityCode, 50, 0, "2020-09-02", true);
        }

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount6,0,gdEquityCode,true);

    }

    /***
     * 同一账户持有不同股权代码时 其中一个股权代码存在冻结，不影响其他股权代码状态
     */

    @Test
    public void lockMatchEqcode()throws Exception{
        String EqCode1 = gdEquityCode;
        String EqCode2 = gdEquityCode + Random(8);
        String EqCode3 = gdEquityCode + Random(8);

        gdEquityCode = EqCode2;
        String response = "";
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);
        uf.shareIssue(EqCode2,shareList4,true);

        gdEquityCode = EqCode3;
        List<Map> shareList5 = gdConstructShareList(gdAccount5,1000,0);
        uf.shareIssue(EqCode3,shareList5,true);

        //冻结账户4 EqCode2 * 股权性质1 *100
        uf.lock(bizNoTest,gdAccount4,EqCode2,100,1,"2032-09-30",true);

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":100}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode1 + "\",\"shareProperty\":0"));
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode2 + "\",\"shareProperty\":0"));
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode1 + "\",\"shareProperty\":1"));
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode2 + "\",\"shareProperty\":1"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode3 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }

    /***
     * 冻结不存在的股权性质 股份列表无变更
     */

    @Test
    public void lock_NotExist()throws Exception{
        String response = "";
        //查询账户余额  总余额 1000

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,500,0,"2022-09-03",true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }


    @Test
    public void lock_LongRegObjectId()throws Exception {
        String response = "";
        //查询账户余额  总余额 1000

        GDBeforeCondition gdBF = new GDBeforeCondition();

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        log.info("股份冻结");
        String reason = "司法冻结";

        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(128);
        regInfo.put("register_registration_object_id", tempObjId);

        response = gd.GDShareLock(bizNoTemp, gdAccount1, gdEquityCode, 500, 0, reason, "2021-09-30", regInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Data too long for column 'object_id'"));

        sleepAndSaveInfo(4000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(false, response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":500}"));
    }


    @Test
    public void unlock_LongRegObjectId()throws Exception {
        String response = "";
        //查询账户余额  总余额 1000

        GDBeforeCondition gdBF = new GDBeforeCondition();

        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp, gdAccount1, gdEquityCode, 500, 0, "2021-09-02", true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(true, response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":500}"));


        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(128);
        regInfo.put("register_registration_object_id",tempObjId);

        response= gd.GDShareUnlock(bizNoTemp,gdEquityCode,200,regInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Data too long for column 'object_id'"));
        sleepAndSaveInfo(4000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(false, response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":300}"));


    }


    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void lock_MatchCase()throws Exception{

        //转板 大小写匹配检查
        String bizNoTemp = "2000" + Random(12);
        String response = uf.lock(bizNoTemp,gdAccount2,gdEquityCode.toLowerCase(),500,0,"2022-09-03",false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));

        bizNoTemp = "2000" + Random(12);
        response = uf.lock(bizNoTemp,gdAccount2,gdEquityCode.toUpperCase(),500,0,"2022-09-03",false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
    }
    
}
