package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDSceneTest_ChangeProperty {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
    }

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);

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
        gdEquityCode = "gdEC" + Random(12);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        //发行
        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
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
     */

    @Test
    public void changeProperty_withLock_01()throws Exception{
        String response = "";
        //查询账户余额  总余额 1000

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //检查账户余额 总股权无变更

        //可用部分部分变更
        uf.changeSHProperty(gdAccount1,gdEquityCode,400,0,1,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,200,0,1,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更

        //无可用变更
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,false);

        //检查账户余额 总股权无变更

        //解除冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,400,true);

        //检查账户余额 总股权无变更

        //可用部分部分变更
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,400,0,1,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,300,0,1,true);

        //检查账户余额 总股权无变更

        //解除所有冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,100,true);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更
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

        //可用部分部分变更
        uf.changeSHProperty(gdAccount2,gdEquityCode,400,1,0,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,200,1,0,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更

        //无可用变更
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,false);

        //检查账户余额 总股权无变更

        //解除冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,400,true);

        //检查账户余额 总股权无变更

        //可用部分部分变更
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,400,1,0,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,300,1,0,true);

        //检查账户余额 总股权无变更

        //解除所有冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,100,true);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更
    }


    /***
     * 股份性质变更 双花测试
     */

    @Test
    public void changePropertyDoubleSend()throws Exception{

        String response1 = uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,false);
        String response2 = uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //异或判断两种其中只有一个上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);

        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":900,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":900,\"lockAmount\":0}"));
    }


    /***
     * 多次变更股权性质
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

        //发行
        response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        for(int i = 0;i < 30; i++){
            log.info("change time " + i);
            uf.changeSHProperty(gdAccount3,gdEquityCode,300,0,1,true);
            query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
            assertEquals(9000,getTotalAmountFromShareList(jsonArrayGet),0.0001);
        }

    }
    
}
