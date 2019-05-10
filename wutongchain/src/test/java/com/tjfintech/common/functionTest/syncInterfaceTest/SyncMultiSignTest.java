package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncMultiSignTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    UtilsClass utilsClass=new UtilsClass();

    /**
     * 同步多签token发行申请(1/2签名)
     * 同步签名多签交易
     */
    @Test
    public void SyncMutiIssueToken() throws InterruptedException {
        //正常情况下
        String tokenType = "CX-" + UtilsClass.Random(7);
        log.info(MULITADD3+ "发行" + tokenType + " token，数量为：" + 10000);
        String data = "MULITADD3" + "发行" + tokenType + " token，数量为：" + 10000;
        String response = multiSign.SyncIssueToken(utilsClass.SHORTMEOUT, MULITADD4, IMPPUTIONADD, tokenType, "100000", data);//向IMPPUTIONADD地址发行10000
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.SyncSign(utilsClass.SHORTMEOUT, Tx1, PRIKEY1);
        Thread.sleep(SLEEPTIME);
        String queryInfo = multiSign.Balance(IMPPUTIONADD,PRIKEY4,tokenType);
        assertThat("100000",containsString(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total")));
        assertThat("200",containsString(JSONObject.fromObject(response).getString("State")));
        assertThat("200",containsString(JSONObject.fromObject(response2).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(response2).getString("Message")));
        //超时情况下
        String tokenType1 = "CX-" + UtilsClass.Random(7);
        String response3 = multiSign.SyncIssueToken(utilsClass.SHORTMEOUT/10, MULITADD4, IMPPUTIONADD, tokenType1, "100000", data);//向IMPPUTIONADD地址发行10000
        String Tx2 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Tx");
        String response4 = multiSign.SyncSign(utilsClass.SHORTMEOUT,Tx2, PRIKEY1);
        assertThat("504",containsString(JSONObject.fromObject(response4).getString("State")));
        assertThat("timeout",containsString(JSONObject.fromObject(response4).getString("Message")));

    }

    /**
     * 同步多签转账(1/2签名)
     */
    @Test
    public void SyncMutiTransfer() throws InterruptedException {
        String tokenType = "CX-" + UtilsClass.Random(7);//随机一个tokentype
        String data = "MULITADD3" + "发行" + tokenType + " token，数量为：" + 10000;
        String response = multiSign.SyncIssueToken(utilsClass.SHORTMEOUT, MULITADD4, IMPPUTIONADD, tokenType, "10000", data);////向IMPPUTIONADD地址发行10000
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY1);
        Thread.sleep(SLEEPTIME);
        String transferData = "归集地址向" + MULITADD1 + "转账999个" + tokenType;
        log.info(transferData);
        List<Map>list=utilsClass.constructToken(MULITADD1,tokenType,"999");//封装token:接收地址，token类型，转账数量
        String syncTransfer = multiSign.SyncTransfer(utilsClass.SHORTMEOUT*2, PRIKEY4, transferData, IMPPUTIONADD, list);//转账操作
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD1, PRIKEY1, tokenType);
        assertThat("9001",containsString(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total")));
        assertThat("999",containsString(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total")));
        assertThat("200",containsString(JSONObject.fromObject(syncTransfer).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(syncTransfer).getString("Message")));
        log.info("回收token");
        String recycle = multiSign.SyncRecycle(utilsClass.SHORTMEOUT, IMPPUTIONADD, PRIKEY4, tokenType, "100");
        assertThat("200",containsString(JSONObject.fromObject(recycle).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(recycle).getString("Message")));
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);

    }

}
