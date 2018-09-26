package com.tjfintech.common;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import static com.tjfintech.common.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoloTest {
    SoloSign soloSign = new SoloSign();
    MultiSign multiSign = new MultiSign();
    UtilsClass utilsClass=new UtilsClass();
    public static String tokenType;
    public static String tokenType2;

    @Before
    public void beforeConfig() throws Exception {
        log.info("发行两种token1000个");
        tokenType = "SOLOTC-"+UtilsClass.Random(3);
        soloSign.issueToken(utilsClass.PRIKEY1,tokenType,"10000.123456789","发行token");
        tokenType2 = "SOLOTC-"+UtilsClass.Random(3);
        soloSign.issueToken(utilsClass.PRIKEY1,tokenType2,"20000.87654321","发行token");
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString("10000.123456789"));
        assertThat(tokenType+"查询余额不正确",response2, containsString("20000.87654321"));
    }



    /**
     * Tc03多签正常流程-发币：签名：查询：转账：查询:回收：查询
     *
     */
    @Test
    public void TC024_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list = new ArrayList();
        list.add(soloSign.constructToken(PUBKEY3,tokenType,"100.25"));
        list.add(soloSign.constructToken(PUBKEY5,tokenType2,"100.25"));
        log.info(transferData);
        String transferInfo=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));

        log.info("查询帐号3跟帐号4余额，判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY3, tokenType);
        String queryInfo2 = soloSign.Balance( PRIKEY5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("100.25"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("200.555"));

        log.info("3向4转账token1");
        List<Map> list1 = new ArrayList<>();
        list1.add(soloSign.constructToken(PUBKEY4,tokenType,"30"));
        String recycleInfo = soloSign.Transfer(list1, PRIKEY3,"3向4转账token1");
        assertThat(recycleInfo, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list2 = new ArrayList<>();
        list2.add(soloSign.constructToken(PUBKEY4,tokenType2,"80"));
        String recycleInfo1 = soloSign.Transfer(list2, PRIKEY3,"3向4转账token1");
        assertThat(recycleInfo1, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list3 = new ArrayList<>();
        list3.add(soloSign.constructToken(PUBKEY6,tokenType,"30"));
        list3.add(soloSign.constructToken(PUBKEY6,tokenType2,"70"));
        String recycleInfo2 = soloSign.Transfer(list3, PUBKEY4, "李四向小六转账30 TT001, 70 TT002");
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list4 = new ArrayList<>();
        list4.add(soloSign.constructToken(PUBKEY6,tokenType2,"20"));
        String recycleInfo3 = soloSign.Transfer(list4, PUBKEY5, "王五向小六转账20 TT002");
        assertThat(recycleInfo3, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list5 = new ArrayList<>();
        list5.add(soloSign.constructToken(PUBKEY4,tokenType2,"80"));
        String recycleInfo4 = soloSign.Transfer(list5, PUBKEY6, "小六向李四转账80");
        assertThat(recycleInfo4, containsString("200"));
        Thread.sleep(SLEEPTIME);
        log.info("查询回收后账户余额是否为0");
        String queryInfo3TK1 = soloSign.Balance(PRIKEY3, tokenType);
        assertThat(queryInfo3TK1, containsString("70.25"));
        String queryInfo4TK1 = soloSign.Balance(PRIKEY4, tokenType);
        assertThat(queryInfo4TK1, containsString("0"));
        String queryInfo4TK2 = soloSign.Balance(PRIKEY4, tokenType2);
        assertThat(queryInfo4TK2, containsString("90"));
        String queryInfo5TK2 = soloSign.Balance(PRIKEY5, tokenType2);
        assertThat(queryInfo5TK2, containsString("100.555"));
        String queryInfo6TK1 = soloSign.Balance(PRIKEY6, tokenType);
        assertThat(queryInfo6TK1, containsString("30"));
        String queryInfo6TK2 = soloSign.Balance(PRIKEY6, tokenType2);
        assertThat(queryInfo6TK2, containsString("10"));



    }







}