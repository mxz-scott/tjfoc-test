package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import java.util.Map;
import static com.tjfintech.common.utils.UtilsClass.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_AuxTool {

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


//    @Test
    public void checkOperationAndVersion()throws Exception{
        uf.checkJGHeaderOpVer(1,JSONObject.fromObject(store.GetHeight()).getInt("data"));
    }

//    @Test
    public void searchKey()throws Exception{
        uf.storeDataCheckKeyWord(13114,13319,"uri");
//        uf.storeDataCheckKeyWord(1,554,"DIR1011");
    }

    @Test
    public void verifyBlockAndTransactionTest()throws Exception{
        commonFunc.verifyBlockAndTransaction(SDKPATH);
    }


    /***
     * 通过关键字查找区块或者交易
     * @throws Exception
     */
//    @Test
    public void check2()throws Exception{
        int iHeightStart = 500;
        int iHeightEnd = 554;
        for(int i=iHeightStart;i<=iHeightEnd;i++) {
            Map temp = gdCF.findDataInBlock(i, "gdCmpyId01z3k4gF");
            String storeData = temp.get("storeData").toString();
            log.info(storeData);

        }
    }

}
