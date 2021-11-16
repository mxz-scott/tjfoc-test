package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSON;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.gdConstructShareList;
import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.mapShareENCN;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
/***
 *  临时测试
 */
public class GDV2_temp_test {

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
        register_event_type = 1;
    }

    @Test
    public void IssueEquity()throws Exception{
        gdCompanyID = CNKey + "Sub2_" + Random(4);
        gdEquityCode = CNKey + "Token2_" + Random(4);

        List<Map> shareList3 = gdConstructShareList(gdAccount1,10,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount1,20,0);
        List<Map> shareList1 = gdConstructShareList(gdAccount1,101,0);

        //发行
        uf.shareIssue(gdEquityCode, shareList3, true);
        //增发
        uf.shareIncrease(gdEquityCode, shareList2, true);
        uf.shareIncrease(gdEquityCode, shareList1, true);

        //回收
        List<Map> shareList4 = gdConstructShareList(gdAccount1,120,0);
        String response1 = uf.shareRecycle(gdEquityCode,shareList4,true);

//        assertEquals(true,response1.contains(200));
    }


}
