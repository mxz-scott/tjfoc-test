package com.tjfintech.common.functionTest.utxoMultiSign;

import com.tjfintech.common.CommonFunc;
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

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PRIKEY4;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiSignDetailTest {
    public  String tokenType;
    public  String tokenType2;
    public  String tokenType3;
    public  long timeMillislong;
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();


    /**
     * 多签的token发行
     * @throws Exception
     */
    @Before
    public void getutxotoken() throws Exception {
        tokenType  = IssueToken("10000"); //tokentype发行数量为10000
        tokenType2  = IssueToken("10000"); //tokentype2发行数量为10000
        tokenType3  = IssueToken("300"); //tokentype2发行数量为10000

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(DBSyncTime,"数据库同步时间"); //交易上链后sdk 拉取数据存数据库等待时间

        log.info("查询归集地址(接收地址)中的余额");
        String response1 = multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        String response3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType3);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals("10000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals("10000",JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response3).getString("State"));
        assertEquals("300",JSONObject.fromObject(response3).getJSONObject("Data").getString("Total"));
        long timeMillislong = System.currentTimeMillis(); //获取当前时间戳
        log.info("当前的时间戳"+timeMillislong);
    }


    /**(1/2)签
     * 多签冻结，转账，回收，恢复
     */
    @Test
    public void TC1276_getutxotoken() throws Exception {

        log.info("冻结token");//token被冻结之后无法进行转账操作但是可以进行回收操作
        log.info("冻结token前的"+tokenType);
        String freezeToken1 = multiSign.freezeToken(tokenType);
        String freezeToken2 = multiSign.freezeToken(tokenType2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(DBSyncTime,"数据库同步时间"); //交易上链后sdk 拉取数据存数据库等待时间


        log.info("执行转账操作，验证token是否冻结成功");
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
        log.info(transferData);
        List<Map> list=utilsClass.constructToken(MULITADD4,tokenType,"10");//封装数据
        multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);//调用转账接口

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(DBSyncTime,"数据库同步时间"); //交易上链后sdk 拉取数据存数据库等待时间

        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);//返回余额9900
        log.info("返回MULITADD4余额");
        String queryInfo4 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);//返回余额为0
        assertEquals("200",JSONObject.fromObject(freezeToken1).getString("State"));
        assertEquals("10000",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(freezeToken2).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));


        log.info("回收token_回收归集地址数量为100（tokenType）");
        multiSign.Recycle(IMPPUTIONADD,PRIKEY4,tokenType,"100");
        multiSign.Recycle(IMPPUTIONADD,PRIKEY4,tokenType2,"100");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(DBSyncTime,"数据库同步时间"); //交易上链后sdk 拉取数据存数据库等待时间

        log.info("查询回收后的归集地址的余额");
        String queryInfo1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2= multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo1).getString("State"));
        assertEquals("9900",JSONObject.fromObject(queryInfo1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("9900",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));


        log.info("恢复token");
        String recoverFrozenToken1 = multiSign.recoverFrozenToken(tokenType);
        String recoverFrozenToken2= multiSign.recoverFrozenToken(tokenType2);
        assertEquals("200",JSONObject.fromObject(recoverFrozenToken1).getString("State"));
        assertEquals("200",JSONObject.fromObject(recoverFrozenToken2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(DBSyncTime,"数据库同步时间"); //交易上链后sdk 拉取数据存数据库等待时间

    }

    /**
     * 验证获取交易utxo 交易详情中的ToAddr ,fromAddr字段
     * @throws Exception
     */
    @Test
    public void TC1276_getutxodetail_ToAddr() throws Exception {
        log.info("获取tokentype"+tokenType);
        log.info("获取utxo交易详情");
        String utxoDetail1 = multiSign.getUTXODetail(0, timeMillislong, tokenType, 10, IMPPUTIONADD, IMPPUTIONADD);
        String utxoDetail2 = multiSign.getUTXODetail(0, timeMillislong, tokenType2, 10, IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("200",JSONObject.fromObject(utxoDetail1).getString("State"));
        assertEquals("200",JSONObject.fromObject(utxoDetail2).getString("State"));

        log.info("设定ToAddr为不存在交易的转出地址（地址存在）");
        String utxoDetail3 = multiSign.getUTXODetail(0, timeMillislong, tokenType, 10, IMPPUTIONADD, "12");
        String utxoDetail4 = multiSign.getUTXODetail(0, timeMillislong, tokenType2, 10, IMPPUTIONADD, "12");
        assertEquals("null",JSONObject.fromObject(utxoDetail3).getString("Data"));
        assertEquals("null",JSONObject.fromObject(utxoDetail4).getString("Data"));

        log.info("设定ToAddr非法");
        String utxoDetail5 = multiSign.getUTXODetail(0, timeMillislong, tokenType, 10, IMPPUTIONADD, "SnpepdFA5kKZMjy2XRh7aX4g2ygwcJ7V8AMRhqSqcmWbeC5684k");
        String utxoDetail6 = multiSign.getUTXODetail(0, timeMillislong, tokenType2, 10, IMPPUTIONADD, "SnpepdFA5kKZMjy2XRh7aX4g2ygwcJ7V8AMRhqSqcmWbeC5684k");
        assertEquals("null",JSONObject.fromObject(utxoDetail5).getString("Data"));
        assertEquals("null",JSONObject.fromObject(utxoDetail6).getString("Data"));

        log.info("设定ToAddr为数值型数据（负数、正数、浮点数）");
        String utxoDetail7 = multiSign.getUTXODetail(0, timeMillislong, tokenType, 10, IMPPUTIONADD, 100.10);
        assertEquals("400",JSONObject.fromObject(utxoDetail7).getString("State"));
        assertEquals("Invalid parameter",JSONObject.fromObject(utxoDetail7).getString("Message"));

    }

    /**
     * 验证获取交易utxo 交易详情中的UTXOType字段
     * @throws Exception
     */
    @Test
    public void TC1276_getutxodetail_UTXOType() throws Exception {

        log.info("获取utxo交易详情——将参数UTXOtype修改为363");
        String utxoDetail1 = multiSign.getUTXODetail(0, timeMillislong, tokenType, 363, IMPPUTIONADD, IMPPUTIONADD);
        String utxoDetail2 = multiSign.getUTXODetail(0, timeMillislong, tokenType2, 363, IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("err:UTXOType is wrong",JSONObject.fromObject(utxoDetail1).getString("Message"));
        assertEquals("err:UTXOType is wrong",JSONObject.fromObject(utxoDetail2).getString("Message"));


        log.info("获取utxo交易详情——将参数UTXOtype修改为String类型");
        String utxoDetail3 = multiSign.getUTXODetail(0, timeMillislong, tokenType, "126", IMPPUTIONADD, IMPPUTIONADD);
        String utxoDetail4 = multiSign.getUTXODetail(0, timeMillislong, tokenType2, "111", IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("Invalid parameter",JSONObject.fromObject(utxoDetail3).getString("Message"));
        assertEquals("Invalid parameter",JSONObject.fromObject(utxoDetail4).getString("Message"));

        log.info("获取utxo交易详情——将参数UTXOtype赋值不相关的数值");
        String utxoDetail5 = multiSign.getUTXODetail(0, timeMillislong, tokenType, 13, IMPPUTIONADD, IMPPUTIONADD);
        String utxoDetail6 = multiSign.getUTXODetail(0, timeMillislong, tokenType2, 13, IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("null",JSONObject.fromObject(utxoDetail5).getString("Data"));
        assertEquals("null",JSONObject.fromObject(utxoDetail6).getString("Data"));

    }
    /**
     * 验证获取交易utxo 交易详情中的tokentype字段
     * @throws Exception
     */
    @Test
    public void TC1273_getutxodetail_tokentype() throws Exception {

        log.info("获取utxo交易详情——tokentype设置为不存在的字段");
        String utxoDetail1 = multiSign.getUTXODetail(0, timeMillislong, "STTTT", 10, IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("null",JSONObject.fromObject(utxoDetail1).getString("Data"));
    }

    /**
     * 验证获取交易utxo 交易详情中的endtimee字段
     * @throws Exception
     */
    @Test
    public void TC1272_getutxodetail_endtime() throws Exception {
        log.info("获取utxo交易详情——endtime设置早于starttime时间");
        String utxoDetail1 = multiSign.getUTXODetail(1558063234785L, 1558060526691L, tokenType, 10, IMPPUTIONADD, IMPPUTIONADD);
        String utxoDetail2 = multiSign.getUTXODetail(1558063234785L, 1558060526691L, tokenType2, 10, IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("err:EndTime must be greater than StartTime",JSONObject.fromObject(utxoDetail1).getString("Message"));
        assertEquals("err:EndTime must be greater than StartTime",JSONObject.fromObject(utxoDetail2).getString("Message"));

        log.info("获取utxo交易详情——endtime设置为负数");
        String utxoDetail3 = multiSign.getUTXODetail(1558063234785L, -1, tokenType, 10, IMPPUTIONADD, IMPPUTIONADD);
        String utxoDetail4 = multiSign.getUTXODetail(1558063234785L, -1, tokenType2, 10, IMPPUTIONADD, IMPPUTIONADD);
        assertEquals("Invalid parameter",JSONObject.fromObject(utxoDetail3).getString("Message"));
        assertEquals("Invalid parameter",JSONObject.fromObject(utxoDetail4).getString("Message"));

        log.info("获取utxo交易详情——设置body体为空");
        String utxoDetail5 = multiSign.getUTXODetail();
        assertEquals("200",JSONObject.fromObject(utxoDetail5).getString("State"));
    }

    /**
     * 验证获取总发行量，总回收量，总冻结量 （tokentype字段）
     */
    @Test
    public void TC1269_gettotal_tokentype(){
        log.info("正常查看信息");
        String gettotal1 = multiSign.gettotal(0, timeMillislong, tokenType);
        String gettotal2 = multiSign.gettotal(0, timeMillislong, tokenType2);
        assertEquals("200",JSONObject.fromObject(gettotal1).getString("State"));
        assertEquals("200",JSONObject.fromObject(gettotal2).getString("State"));

        log.info("tokentype设置为空或者无此字段");
        String gettotal3 = multiSign.gettotal(0, timeMillislong, "");
        assertEquals("200",JSONObject.fromObject(gettotal3).getString("State"));

        log.info("tokentype设置为空或者无此字段");
        String gettotal4 = multiSign.gettotal(0, timeMillislong, "SSTT");
        assertEquals("0",JSONObject.fromObject(gettotal4).getJSONObject("Data").getString("IssueAmount"));
        assertEquals("0",JSONObject.fromObject(gettotal4).getJSONObject("Data").getString("RecycleAmount"));

        log.info("tokentype设置数值型数据");
        String gettotal5 = multiSign.gettotal(0, timeMillislong, 111);
        assertEquals("Invalid parameter",JSONObject.fromObject(gettotal5).getString("Message"));

        log.info("tokentype设置为浮点数");
        String gettotal6 = multiSign.gettotal(0, timeMillislong, 11.11);
        assertEquals("Invalid parameter",JSONObject.fromObject(gettotal6).getString("Message"));
    }

    /**
     * 验证获取总发行量，总回收量，总冻结量 （endtime字段）
     */
    @Test
    public void TC1268_gettotal_endtime() {

        log.info("endtime设置早于starttime");
        String gettotal1 = multiSign.gettotal(1558063234785L, 1558060526691L,tokenType);
        String gettotal2 = multiSign.gettotal(1558063234785L, 1558060526691L, tokenType2);
        assertEquals("err:EndTime must be greater than StartTime",JSONObject.fromObject(gettotal1).getString("Message"));
        assertEquals("err:EndTime must be greater than StartTime",JSONObject.fromObject(gettotal2).getString("Message"));

        log.info("endtime设置为负数");
        String gettotal3 = multiSign.gettotal(0, -1,tokenType);
        String gettotal4 = multiSign.gettotal(0, -1, tokenType2);
        assertEquals("Invalid parameter",JSONObject.fromObject(gettotal3).getString("Message"));
        assertEquals("Invalid parameter",JSONObject.fromObject(gettotal4).getString("Message"));

        log.info("starttime设置为负数");
        String gettotal5 = multiSign.gettotal(-1, timeMillislong,tokenType);
        String gettotal6 = multiSign.gettotal(-1, timeMillislong, tokenType2);
        assertEquals("Invalid parameter",JSONObject.fromObject(gettotal5).getString("Message"));
        assertEquals("Invalid parameter",JSONObject.fromObject(gettotal6).getString("Message"));

        log.info("所有字段全部为空数");
        String gettotal = multiSign.gettotal();
        assertEquals("200",JSONObject.fromObject(gettotal).getString("State"));
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
