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
public class GDV2_TestIssueStop {

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

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);
        gdEquityCode = "test_trf" + Random(8);

        //发行
        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1
    }

    /**
     * 发行后 停止节点 再转让 此用例需要在节点数据清空后进行操作  目前用例半自动化
     * 步骤如下
     * 清空节点 sdk数据
     *
     * @throws Exception
     */
    @Test
    public void shareTransfer()throws Exception{
//        String code1 = gdEquityCode;
//        //第二次发行
//        gdEquityCode = "second" + Random(6);
//        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1

        String response = "";
        UtilsClass utilsClassTemp = new UtilsClass();
        utilsClassTemp.setAndRestartPeerList();

        sleepAndSaveInfo(SLEEPTIME*2);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        query = gd.GDGetEnterpriseShareInfo(code1);

        //当前可用余额500 转出小于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
                gdEquityCode,true);

//        //转之前的股权代码
//        uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
//                code1,true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        query = gd.GDGetEnterpriseShareInfo(code1);



    }

}
