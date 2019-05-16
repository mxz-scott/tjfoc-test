package com.tjfintech.common.functionTest.utxoMultiSign;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfoc.base.MultiSignIssue;
import com.tjfoc.base.MultiSignTransferAccounts;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PRIKEY4;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiSignDetailTest {
    private static String tokenType;
    private static String tokenType2;
    private static long timeMillislong;
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    MultiSignIssue multiIssue = new MultiSignIssue();
    MultiSignTransferAccounts multiTrans = new MultiSignTransferAccounts();

    /**
     * 多签的token发行
     * @throws Exception
     */
    @Before
    public void getutxotoken() throws Exception {
        tokenType  = IssueToken("10000"); //tokentype发行数量为10000
        tokenType2  = IssueToken("10000"); //tokentype2发行数量为10000
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址(接收地址)中的余额");
        String response1 = multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals("10000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals("10000",JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));
        long timeMillislong = System.currentTimeMillis(); //获取当前时间戳
        log.info("当前的时间戳"+timeMillislong);
        Thread.sleep(SLEEPTIME);
    }


    /**(1/2)签
     * 多签冻结，转账，回收，恢复
     */
    @Test
    public void TC1276_getutxotoken() throws Exception {

        log.info("冻结token");//token被冻结之后无法进行转账操作但是可以进行回收操作
        String freezeToken1 = multiSign.freezeToken(tokenType);
        String freezeToken2 = multiSign.freezeToken(tokenType2);
        Thread.sleep(SLEEPTIME);


        log.info("执行转账操作，验证token是否冻结成功");
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
        log.info(transferData);
        List<Map> list=utilsClass.constructToken(MULITADD4,tokenType,"10");//封装数据
        multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);//调用转账接口
        Thread.sleep(SLEEPTIME);
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);//返回余额9900
        String queryInfo4 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);//返回余额为0
        assertEquals("200",JSONObject.fromObject(freezeToken1).getString("State"));
        assertEquals("10000",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(freezeToken2).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));


        log.info("回收token_回收归集地址数量为100（tokenType）");
        multiSign.Recycle(IMPPUTIONADD,PRIKEY4,tokenType,"100");
        multiSign.Recycle(IMPPUTIONADD,PRIKEY4,tokenType2,"100");

        Thread.sleep(SLEEPTIME);
        log.info("查询回收后的归集地址的余额");
        String queryInfo1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2= multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo1).getString("State"));
        assertEquals("9900",JSONObject.fromObject(queryInfo1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("9900",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
        Thread.sleep(SLEEPTIME);

        log.info("恢复token");
        String recoverFrozenToken1 = multiSign.recoverFrozenToken(tokenType);
        String recoverFrozenToken2= multiSign.recoverFrozenToken(tokenType2);
        assertEquals("200",JSONObject.fromObject(recoverFrozenToken1).getString("State"));
        assertEquals("200",JSONObject.fromObject(recoverFrozenToken2).getString("State"));
        Thread.sleep(SLEEPTIME);


    }

    @Test
    public void TC1276_getutxodetail() throws Exception {
        log.info("获取utxo交易详情");
        String utxoDetail1 = multiSign.getUTXODetail(0, timeMillislong, tokenType, 10, IMPPUTIONADD, IMPPUTIONADD);
        String utxoDetail2 = multiSign.getUTXODetail(0, timeMillislong, tokenType2, 10, IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("200",JSONObject.fromObject(utxoDetail1).getString("State"));
        assertEquals("200",JSONObject.fromObject(utxoDetail2).getString("State"));

        log.info("设定ToAddr为不存在交易的转出地址（地址存在）");
        multiSign.getUTXODetail(0, timeMillislong, tokenType, 10, IMPPUTIONADD, "SnpepdFA5kKZMjy2XRh7aX4g2ygwcJ7V8AMRhqSqcmWbeC5684k");
    }




    /**
     * 公共方法用于token发行
     */
    public String IssueToken(String amount){
        String tokenType = "MT-" + UtilsClass.Random(7);
        String data = "MULITADD1" + "发行" + tokenType + "，数量为：" + 10000;
        String response = multiSign.issueToken(IMPPUTIONADD,tokenType,amount,data);//调用方法发行token
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY5);
        assertThat(response2, containsString("200"));
        return tokenType;

    }




}
