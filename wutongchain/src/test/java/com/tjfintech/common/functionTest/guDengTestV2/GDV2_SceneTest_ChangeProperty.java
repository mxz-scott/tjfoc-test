package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
/***
 * 股份性质变更异常场景测试用例
 */
public class GDV2_SceneTest_ChangeProperty {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
        register_event_type = 1;
    }

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);
        gdCompanyID = CNKey + "Sub3_" + Random(4);
        gdEquityCode = CNKey + "Token3_" + Random(4);

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

    public List initRegList(String addr)throws Exception{
        GDBeforeCondition gdBF = new GDBeforeCondition();
        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(addr) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(addr) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(addr));

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(addr));

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        return regListInfo;
    }

//    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
        uf.updateBlockHeightParam(endHeight);
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
     * 存在冻结的流通股 可用部分全部变更
     * 多次变更-全部变更
     */

    @Test
    public void changeProperty_withLock_01()throws Exception{
        String response = "";
        //查询账户余额  总余额 1000

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":500}"));


        //可用部分部分变更
        uf.changeSHProperty(gdAccount1,gdEquityCode,400,0,1,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":600,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,200,0,1,false);
        sleepAndSaveInfo(8000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":600,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        //无可用变更
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        //解除冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,400,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));


        //可用部分部分变更
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":600,\"lockAmount\":0}"));

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,400,0,1,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":600,\"lockAmount\":0}"));

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,300,0,1,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":100,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":900,\"lockAmount\":0}"));

        //解除所有冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,100,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":100,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":900,\"lockAmount\":0}"));

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,response.contains("{\"equityCode\":\"" + gdEquityCode + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }

    /***
     * 存在冻结的高管股 可用部分全部变更
     */

    @Test
    public void changeProperty_withLock_02()throws Exception{
        String response = "";
        //查询账户余额  总余额 1000

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,500,1,"2022-09-03",true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":500}"));

        //可用部分部分变更
        uf.changeSHProperty(gdAccount2,gdEquityCode,400,1,0,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":600,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,200,1,0,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":600,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        //无可用变更
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":500}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        //解除冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,400,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));


        //可用部分部分变更
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":400,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":600,\"lockAmount\":0}"));

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,400,1,0,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":400,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":600,\"lockAmount\":0}"));

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,300,1,0,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":100,\"lockAmount\":100}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":900,\"lockAmount\":0}"));

        //解除所有冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,100,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":100,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":900,\"lockAmount\":0}"));

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,response.contains("{\"equityCode\":\"" + gdEquityCode + "\",\"shareProperty\":1"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }


    /***
     * 股份性质变更 双花测试
     * 2020/12/24 因所有接口均走同步 不太可能会出现双花的情况 故做两种判断
     */

    @Test
    public void changePropertyDoubleSend()throws Exception{

        String response1 = uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,false);
        String response2 = uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String state = JSONObject.fromObject(response2).getString("state");

        if(!state.equals("200")){
            assertEquals("400", JSONObject.fromObject(response2).getString("state"));
        }
//        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        //异或判断两种其中只有一个上链
//        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
//                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);

        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        if(state.equals("400")) {

            assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                    "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":900,\"lockAmount\":0}"));
            assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                    "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":100,\"lockAmount\":0}"));
        }else {
            assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                    "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":800,\"lockAmount\":0}"));
            assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                    "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":200,\"lockAmount\":0}"));
        }
    }


    /***
     * 多次变更股权性质
     * 支持部分变更
     * 所有权及持股总数不变更  P17对应的需求点
     */

    @Test
    public void multChangeProperty()throws Exception{

        String response = "";
        //发行
        gdEquityCode = "gdEC" + Random(12);
        List<Map> shareList = gdConstructShareList(gdAccount3,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount3,3000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,2000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount3,3000,0, shareList3);

        uf.shareIssue(gdEquityCode,shareList4,true);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        for(int i = 0;i < 15; i++){
            log.info("change time " + i);
            uf.changeSHProperty(gdAccount3,gdEquityCode,600,0,1,true);
            query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
            //测试股权性质变更 所有权及持股总数不变更
            assertEquals("9000",getTotalAmountFromShareList(jsonArrayGet));
        }

    }

    /***
     * 冻结日期为当前之前的日期 对变更性质无影响
     */

    @Test
    public void changeProperty_ExceedCutOffDate()throws Exception{
        String response = "";
        //查询账户余额  总余额 1000

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2020-09-03",true);

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,response.contains("{\"equityCode\":\"" + gdEquityCode + "\",\"shareProperty\":1"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));


        response = uf.changeSHProperty(gdAccount1,gdEquityCode,1000,0,1,true);

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,response.contains("{\"equityCode\":\"" + gdEquityCode + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }


    /***
     * 同一账户持有不同股权代码时 其中一个股权代码存在冻结，不影响其他股权代码变更股权性质
     */

    @Test
    public void changeProperty_lockMatchEqcode()throws Exception{
        String EqCode1 = gdEquityCode;
        String EqCode2 = gdEquityCode + Random(8);
        String EqCode3 = gdEquityCode + Random(8);

        gdEquityCode = EqCode2;

        String response = "";
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);
        List<Map> shareList5 = gdConstructShareList(gdAccount5,1000,0);

        uf.shareIssue(EqCode2,shareList4,true);

        gdEquityCode = EqCode3;
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

        //变更4 5股权性质
        uf.changeSHProperty(gdAccount4,EqCode1,1000,1,0,true);
        uf.changeSHProperty(gdAccount5,EqCode3,1000,0,1,true);

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode3 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }


    /***
     * 变更不存在的股权性质 股份列表无变更
     */

    @Test
    public void changeProperty_NotExistProperty()throws Exception{
        //账户3只有股份性质 1 的股权代码
        String response = uf.changeSHProperty(gdAccount2,gdEquityCode,400,0,1,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("余额不足",JSONObject.fromObject(response).getString("message"));

        //检查账户余额 总股权无变更
        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
//        assertEquals(false,response.contains("\"shareProperty\":0"));//集成测试场景下 可能会出现账户股权性质0的情况


    }

    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void changeProperty_MatchCase()throws Exception{

        //变更股权性质 大小写匹配检查
        String response = uf.changeSHProperty(gdAccount4,gdEquityCode.toLowerCase(),1000,1,0,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message").trim());

        response = uf.changeSHProperty(gdAccount3,gdEquityCode.toUpperCase(),1000,0,1,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message").trim());
    }


    /***
     * 股权性质变更 同一个账户不同性质的股权代码
     */

    @Test
    public void changeProperty_MultiProperty()throws Exception{

        String EqCode2 = gdEquityCode + Random(8);
        gdEquityCode = EqCode2;

        String response = "";
        List<Map> shareList = gdConstructShareList(gdAccount4,1000,1);
        List<Map> shareList2 = gdConstructShareList(gdAccount4,1000,2, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount4,1000,3, shareList2);

        uf.shareIssue(EqCode2,shareList3,true);
        sleepAndSaveInfo(2000);


        //变更股权性质 变更多个
        uf.changeSHProperty(gdAccount4,gdEquityCode,500,1,4,false);
        uf.changeSHProperty(gdAccount4,gdEquityCode,500,2,5,false);
        response = uf.changeSHProperty(gdAccount4,gdEquityCode,500,3,6,false);

        commonFunc.sdkCheckTxOrSleep(
                JSONObject.fromObject(response).getJSONObject("data").getString("txId"), utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));

        String query = "";

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount4,500,1,0,mapShareENCN().get("1"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount4,500,2,0,mapShareENCN().get("2"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,500,3,0,mapShareENCN().get("3"), respShareList3);
        respShareList4 = gdConstructQueryShareList(gdAccount4,500,4,0,mapShareENCN().get("4"), respShareList4);
        respShareList4 = gdConstructQueryShareList(gdAccount4,500,5,0,mapShareENCN().get("5"), respShareList4);
        respShareList4 = gdConstructQueryShareList(gdAccount4,500,6,0,mapShareENCN().get("6"), respShareList4);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":2,\"sharePropertyCN\":\"" + mapShareENCN().get("2") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":3,\"sharePropertyCN\":\"" + mapShareENCN().get("3") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":4,\"sharePropertyCN\":\"" + mapShareENCN().get("4") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":5,\"sharePropertyCN\":\"" + mapShareENCN().get("5") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":6,\"sharePropertyCN\":\"" + mapShareENCN().get("6") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

    }


    /***
     * 股权性质变更 不同账户不同性质的股权代码 全部变更
     */

    @Test
    public void changeProperty_MultiProperty02()throws Exception{
        String EqCode2 = gdEquityCode + Random(8);
        gdEquityCode = EqCode2;
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,1);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,2, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,3, shareList2);

        uf.shareIssue(EqCode2,shareList3,true);



        //变更股权性质 大小写匹配检查
        String response = "";
        uf.changeSHProperty(gdAccount1,gdEquityCode,1000,1,4,false);
        uf.changeSHProperty(gdAccount2,gdEquityCode,1000,2,5,false);
        response = uf.changeSHProperty(gdAccount3,gdEquityCode,1000,3,6,false);


        commonFunc.sdkCheckTxOrSleep(
                JSONObject.fromObject(response).getJSONObject("data").getString("txId"), utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));

        String query = "";

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,4,0,mapShareENCN().get("4"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount2,1000,5,0,mapShareENCN().get("5"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount3,1000,6,0,mapShareENCN().get("6"),respShareList);

        log.info(respShareList.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList.size(),getShareList.size());
        assertEquals(true,respShareList.containsAll(getShareList) && getShareList.containsAll(respShareList));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":4,\"sharePropertyCN\":\"" + mapShareENCN().get("4") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":5,\"sharePropertyCN\":\"" + mapShareENCN().get("5") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));


        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":6,\"sharePropertyCN\":\"" + mapShareENCN().get("6") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

    }

    /***
     * 变更异常参数场景
     * TC2413
     */
    @Test
    public void changeProperty_Ex()throws Exception{
        listRegInfo = initRegList(gdAccount1);
        //数量使用负值
        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,gdAccount1,gdEquityCode,-10,0,2,listRegInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:json: cannot unmarshal number -10 into Go struct field SharesChange.Amount of type uint64",
                JSONObject.fromObject(response).getString("message"));

        //使用客户的keyId
        response= gd.GDShareChangeProperty(gdAccountKeyID1,gdAccount1,gdEquityCode,10,0,2,listRegInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        assertEquals("数字签名出错",JSONObject.fromObject(response).getString("message"));

        //使用其他客户的keyId
        response= gd.GDShareChangeProperty(gdAccountKeyID2,gdAccount1,gdEquityCode,10,0,2,listRegInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        assertEquals("数字签名出错",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 股权性质变更 登记对象标识使用超长的ID
     * @throws Exception
     */
    @Test
    public void shareChangePropertyLongObjectID() throws Exception {


        String eqCode = gdEquityCode;
        String address = gdAccount1;

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(128);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,500,oldProperty,newProperty,regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Data too long for column 'object_id'"));

        sleepAndSaveInfo(4000);

        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(gdAccClientNo1, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

//        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
//                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
    }

    /***
     * 股权性质变更 登记对象标识使用超长的ID
     * @throws Exception
     */
    @Test
    public void shareChangePropertyLongObjectID02() throws Exception {

        String eqCode = gdEquityCode;
        String address = gdAccount1;

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(128);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,500,oldProperty,newProperty,regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Data too long for column 'object_id'"));

        sleepAndSaveInfo(4000);

        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(gdAccClientNo1, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

//        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
//                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

    }

}
