package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import lombok.extern.slf4j.Slf4j;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PRIKEY1;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncSingleSignTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;
    private static String tokenType3;

    /**
     * 同步测试单签发行token
     */
    @Test
    public void SyncIssueToken() throws Exception {
        //正常情况下（1500毫秒）
        tokenType = "SOLOTC-"+UtilsClass.Random(6);//随机生成tokentype
        String isResult = soloSign.SyncIssueToken(utilsClass.SHORTMEOUT, utilsClass.PRIKEY1, tokenType, "10000", "单签发行token", ADDRESS2);
        Thread.sleep(SLEEPTIME);
        tokenType2 = "SOLOTC-"+UtilsClass.Random(6);
        String isResult2 = soloSign.SyncIssueToken(utilsClass.SHORTMEOUT, utilsClass.PRIKEY2, tokenType2, "20000", "单签发行token", ADDRESS3);
        Thread.sleep(SLEEPTIME);
        assertThat("200",containsString(JSONObject.fromObject(isResult).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(isResult).getString("Message")));
        assertThat("200",containsString(JSONObject.fromObject(isResult2).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(isResult2).getString("Message")));

        //超时情况
        tokenType3 = "SOLOTC-"+ UtilsClass.Random(6);
        String isResult3 = soloSign.SyncIssueToken(utilsClass.SHORTMEOUT/10, utilsClass.PRIKEY3, tokenType3, "10000", "单签发行token", ADDRESS2);
        assertThat("504", containsString(JSONObject.fromObject(isResult3).getString("State")));
        assertThat("timeout",containsString(JSONObject.fromObject(isResult3).getString("Message")));


    }
    /**
     * 同步测试单签转账交易
     */
    @Test
    public void SyncTransfer() throws Exception {
        tokenType = "SOLOTC-"+UtilsClass.Random(6);//随机生成tokentype
        String isResult = soloSign.SyncIssueToken(utilsClass.SHORTMEOUT, utilsClass.PRIKEY1, tokenType, "10000", "单签发行token", ADDRESS2);
        Thread.sleep(SLEEPTIME);
        String transferData = "归集地址向" + "PUBKEY3" + "转账3000个" + "tokenType"+",并向"+"PUBKEY4"+"转账tokenType2";
        log.info(transferData);
        //构建token
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenType,"3000");
        String transfer = soloSign.SyncTransfer(utilsClass.SHORTMEOUT, list, PRIKEY1, transferData);
        Thread.sleep(SLEEPTIME);
        String response1 = soloSign.Balance( PRIKEY1, tokenType);
        assertThat(response1, containsString(tokenType + "\":\"7000\""));  //查询余额
        assertThat("200",containsString(JSONObject.fromObject(transfer).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(transfer).getString("Message")));

    }

}
