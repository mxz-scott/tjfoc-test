package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GoSmartToken;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class smtMultiInvalidTest {
    UtilsClass utilsClass = new UtilsClass();
    SmartTokenCommon stc = new SmartTokenCommon();
    CommonFunc commonFunc = new CommonFunc();
    GoSmartToken st = new GoSmartToken();

    private static String tokenType;

    @BeforeClass
    public static void BeforeClass()throws Exception{
        if (MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.createSTAddresses();
        }
    }

    /**
     * 多签正常流程-发币：签名：查询：转账：查询:回收：查询
     *
     */
    @Test
    public void TC03_multiProgress() throws Exception {

        //发行
        tokenType =  stc.beforeConfigIssueNewToken("1000.25");

        //转让
        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        List<Map> payList= stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        List<Map> collList= stc.smartConstructTokenList(MULITADD4,"test", "10",null);
        String transferResp= stc.smartTransfer(tokenType, payList, collList, "", "", transferData);

        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "990.25");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10");

        //销毁
        String destroyData1 = "销毁 ADDRESS1 中的" + tokenType;
        String destroyData2 = "销毁 MULITADD4 中的" + tokenType;
        List<Map> payList1 = stc.smartConstructTokenList(ADDRESS1, "test", "990.25",null);
        List<Map> payList2 = stc.smartConstructTokenList(MULITADD4, "test", "10",null);

        String destroyResp1 = stc.smartDestroy(tokenType, payList1, "", destroyData1);
        String destroyResp2 = stc.smartDestroy(tokenType, payList2, "", destroyData2);

        assertEquals("200",JSONObject.fromObject(destroyResp1).getString("state"));
        assertEquals("200",JSONObject.fromObject(destroyResp2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressNoBalance(MULITADD4, tokenType);

        log.info("查询回收账户余额");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "1000.25");
    }



    /**
     * 资产类型转换
     *
     */
    @Test
    public void TC_exchange() throws Exception {

        //发行
        tokenType =  stc.beforeConfigIssueNewToken("200");

        //转换
        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        List<Map> payList= stc.smartConstructTokenList(ADDRESS1, "test", "200",null);
        List<Map> collList= stc.smartConstructTokenList(MULITADD4,"test", "200",null);
        String transferResp= stc.smartExchange(tokenType, payList, collList, "NEW_TB001","", transferData);

        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressHasBalance(MULITADD4, "NEW_TB001", "200");


    }

    /**
     * 支出和收入金额不匹配，转账申请失败
     *
     */
    @Test
    public void TC_transferSoloMulti() throws Exception {

        //发行
        tokenType =  stc.beforeConfigIssueNewToken("200");

        //转账
        String transferData = "ADDRESS1 向 MULITADD4/ADDRESS2 转账10.123456/20.123456个" + tokenType;
        List<Map> payList= stc.smartConstructTokenList(ADDRESS1, "test", "200",null);
        List<Map> collList= stc.smartConstructTokenList(MULITADD4,"test", "10.123456",null);
        List<Map> collList2 = stc.smartConstructTokenList(ADDRESS2,"test", "20.123456",collList);
        String transferResp= st.SmartTransferReq(tokenType, payList, collList2, "", "", transferData);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "200");
        String querInfo = st.SmartGetBalanceByAddr(MULITADD4,"");
        String querInfo2 = st.SmartGetBalanceByAddr(ADDRESS2,"");
        assertEquals(false,querInfo.contains(tokenType));
        assertEquals(false,querInfo2.contains(tokenType));

    }

}
