package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetTokenApiTransactionLatest {

    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    TestBuilder testBuilder = TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    FileOperation fopr = new FileOperation();

    private static String tokenRelease;
    private static String tokenAddr1;
    private static String actualAmount1;

    String fileName = resourcePath  + "token_upg.txt";

    @BeforeClass
    public static void init() throws Exception {
        SDKADD = TOKENADD;
        if (tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @Test
    public void test() throws Exception {

        List list = fopr.readForLine(fileName);
        tokenAddr1 = list.get(0).toString();
        tokenRelease =  list.get(1).toString();
        actualAmount1 =  list.get(2).toString();

        //查询旧版本数据
        String response1 = tokenModule.tokenGetBalance(tokenAddr1, tokenRelease);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1, JSONObject.fromObject(response1).getJSONObject("data").getString(tokenRelease));

        //旧版本数据做转账（通过地址）
        response1 = tokenModule.tokenTransfer(tokenAddr1,tokenAccount1,tokenRelease,"100","");
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        String hash1 = JSONObject.fromObject(response1).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        response1 = tokenModule.tokenGetBalance(tokenAddr1, tokenRelease);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals("900.123456", JSONObject.fromObject(response1).getJSONObject("data").getString(tokenRelease));

        //旧版本数据做转账（通过utxo哈希）
        List<Map> listutxo = utilsClass.tokenConstructUTXO(hash1,1,"100",tokenMultiAddr1);
        response1 = tokenModule.tokenTransfer(tokenAddr1,"",null,listutxo);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        response1 = tokenModule.tokenGetBalance(tokenAddr1, tokenRelease);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals("800.123456", JSONObject.fromObject(response1).getJSONObject("data").getString(tokenRelease));

        //旧版本数据做部分回收（通过地址）
        response1 = tokenModule.tokenDestoryByList(tokenAddr1,tokenRelease,"100","");
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        hash1 = JSONObject.fromObject(response1).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        response1 = tokenModule.tokenGetBalance(tokenAddr1, tokenRelease);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals("700.123456", JSONObject.fromObject(response1).getJSONObject("data").getString(tokenRelease));

        //旧版本数据做部分回收（通过utxo）
        HashMap<String, Object> mapSendMsg = new HashMap<>();
        listutxo = utilsClass.tokenConstrucDestroytUTXO(hash1,1,"100");
        response1 = tokenModule.tokenDestoryByList(null,listutxo,"",mapSendMsg);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        response1 = tokenModule.tokenGetBalance(tokenAddr1, tokenRelease);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals("600.123456", JSONObject.fromObject(response1).getJSONObject("data").getString(tokenRelease));

        //全部回收
        response1 = tokenModule.tokenDestoryByTokenType(tokenRelease,"");
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        response1 = tokenModule.tokenGetDestroyBalance();
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals("1000.123456", JSONObject.fromObject(response1).getJSONObject("data").getString(tokenRelease));

    }

}
