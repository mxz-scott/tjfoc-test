package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_InitUpdateTest {

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
    public void IssueEquity()throws Exception{
        gdEquityCode = "test_trf" + Random(8);
        //发行
        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1
    }

    /**
     * 更新系统合约后 再次发行确认不会更新平台公钥
     *
     * @throws Exception
     */
    @Test
    public void shareIssueAfterContractUpdateTest()throws Exception{

        //更新系统合约
        gd.GDEquitySystemInit(gdContractAddress, gdPlatfromKeyID);

        sleepAndSaveInfo(3000,"等待系统合约更新");

        gdEquityCode = "contractUpdate" + Random(8);
        //发行
        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1
    }

}
