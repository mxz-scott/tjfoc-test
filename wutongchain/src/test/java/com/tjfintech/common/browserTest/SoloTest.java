package com.tjfintech.common.browserTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoloTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    @BeforeClass
    public static void beforeClass() throws Exception {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(SLEEPTIME);
    }

    @Before
    public void beforeConfig() throws Exception {

        issueAmount1 = "10000.1234567891234";
        issueAmount2 = "20000.87654321234";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "10000.1234567891";
            actualAmount2 = "20000.8765432123";
        }else {
            actualAmount1 = "10000.123456";
            actualAmount2 = "20000.876543";
        }

        log.info("发行两种token");
        tokenType = "Single-"+UtilsClass.Random(6);
        String isResult= soloSign.issueToken(PRIKEY1,tokenType,issueAmount1,"单签发行token",ADDRESS1);
        //Thread.sleep(SLEEPTIME);
        tokenType2 = "Single-"+UtilsClass.Random(6);
        String isResult2= soloSign.issueToken(PRIKEY1,tokenType2,issueAmount2,"单签发行token",ADDRESS1);
        assertThat(tokenType+"发行token错误",isResult, containsString("200"));
        assertThat(tokenType+"发行token错误",isResult2, containsString("200"));

        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = soloSign.BalanceByAddr(ADDRESS1, tokenType);
        String response2 = soloSign.BalanceByAddr(ADDRESS1, tokenType2);

        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString(actualAmount1));
        assertThat(tokenType+"查询余额不正确",response2, containsString(actualAmount2));
    }


    /**
     * Tc024单签正常流程:
     *
     */
    @Test
    public void TC024_SoloProgress() throws Exception {
        String transferData = "归集地址向" + ADDRESS3 + "转账100.25个" + tokenType+",并向"+ADDRESS5+"转账"+tokenType2;
        log.info(transferData);
        List<Map> listModel = soloSign.constructToken(ADDRESS3,tokenType,"100.25");
        log.info(ADDRESS3);
        List<Map> list=soloSign.constructToken(ADDRESS5,tokenType2,"200.555",listModel);
        String transferInfo= soloSign.Transfer(list,PRIKEY1, transferData);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));
        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        String queryInfo = soloSign.BalanceByAddr(ADDRESS3, tokenType);
        String queryInfo2 = soloSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("100.25"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("200.555"));
        log.info("3向4转账" + tokenType);
        List<Map> list1 = soloSign.constructToken(ADDRESS4,tokenType,"30");
        String recycleInfo = soloSign.Transfer(list1, PRIKEY3,"3向4转账"+tokenType);
        assertThat(recycleInfo, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType2,"80");
        String recycleInfo1 = soloSign.Transfer(list2, PRIKEY5,"5向4转账"+tokenType2);
        assertThat(recycleInfo1, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list3 = soloSign.constructToken(ADDRESS2,tokenType,"30");
        List<Map>list4= soloSign.constructToken(ADDRESS2,tokenType2,"70",list3);
        String recycleInfo2 = soloSign.Transfer(list4, PRIKEY4, "4向2转账两种token");
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list5 = soloSign.constructToken(ADDRESS2,tokenType2,"20");
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY5, "5向2转账"+tokenType2);
        assertThat(recycleInfo3, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list6 = (soloSign.constructToken(ADDRESS4,tokenType2,"30"));
        List<Map> list7= soloSign.constructToken(ADDRESS4,tokenType2,"50",list6);
        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY2, "2向4转账两种token");
        log.info(recycleInfo4);
        assertThat(recycleInfo4, containsString("200"));
        Thread.sleep(SLEEPTIME);
        String queryInfo3TK1 = soloSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(queryInfo3TK1, containsString("70.25"));
        String Info1 = multiSign.Recycle("", PRIKEY3, tokenType, "70.25");
        assertThat(Info1, containsString("200"));
        log.info("帐号3，token1余额正确");
        String queryInfo4TK1 = soloSign.BalanceByAddr(ADDRESS4, tokenType);
        assertThat(queryInfo4TK1, containsString("0"));
        log.info("帐号4，token1余额正确");
        String queryInfo4TK2 = soloSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(queryInfo4TK2, containsString("90"));
        String Info2 = multiSign.Recycle("", PRIKEY4, tokenType2, "90");
        assertThat(Info2, containsString("200"));
        log.info("帐号4，token2余额正确");
        String queryInfo5TK2 = soloSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(queryInfo5TK2, containsString("100.555"));
        String Info3 = multiSign.Recycle("", PRIKEY5, tokenType2, "100.555");
        assertThat(Info3, containsString("200"));
        log.info("帐号5，token2余额正确");
        String queryInfo6TK1 = soloSign.BalanceByAddr(ADDRESS2, tokenType);
        assertThat(queryInfo6TK1, containsString("30"));
        String Info4 = multiSign.Recycle("", PRIKEY2, tokenType, "30");
        assertThat(Info4, containsString("200"));
        log.info("帐号6，token1余额正确");
        String queryInfo6TK2 = soloSign.BalanceByAddr(ADDRESS2, tokenType2);
        assertThat(queryInfo6TK2, containsString("10"));
        String Info5 = multiSign.Recycle("", PRIKEY2, tokenType2, "10");
        assertThat(Info5, containsString("200"));
        log.info("帐号6，token2余额正确");
    }

}
