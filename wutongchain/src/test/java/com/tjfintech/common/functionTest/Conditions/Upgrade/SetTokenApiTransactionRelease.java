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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetTokenApiTransactionRelease {

    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    TestBuilder testBuilder = TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    FileOperation fopr = new FileOperation();

    private static String tokenRelease;
    private static String issueAmount1;
    private static String actualAmount1;

    String fileName = resourcePath  + "token_upg.txt";

    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    public void saveTokenToFile(String tokentype)throws Exception{

        log.info("save tokentype data to file " + fileName);
        //将token信息存储文件 备份
        fopr.appendToFile(tokentype,fileName);
    }

    @Test
    public void test() throws Exception {
        issueAmount1 = "1000.12345678912345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";

        } else {
            actualAmount1 = "1000.123456";

        }

        fopr.clearMsgDateForFile(fileName);

        log.info("多签发行两种token");
        //两次发行之前不可以有sleep时间
        tokenRelease = commonFunc.tokenModule_IssueToken(tokenMultiAddr1, tokenAccount1, issueAmount1);
        saveTokenToFile(tokenAccount1);
        saveTokenToFile(tokenRelease);
        saveTokenToFile(actualAmount1);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        log.info("查询归集地址中两种token余额");

        String response1 = tokenModule.tokenGetBalance(tokenAccount1, tokenRelease);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1, JSONObject.fromObject(response1).getJSONObject("data").getString(tokenRelease));

    }
}
