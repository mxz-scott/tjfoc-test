package com.tjfintech.common.functionTest;

import com.bw.base.SingleSignIssue;
import com.bw.base.SingleSignTransferAccounts;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.Util;
import com.tjfintech.common.utils.UtilsClass;
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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalSingleSignTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;
    SingleSignIssue singleSign = new SingleSignIssue();
    SingleSignTransferAccounts singleSignTrans = new SingleSignTransferAccounts();

    @Before
    public void beforeConfig() throws Exception {


        tokenType = "ST-"+UtilsClass.Random(6);
        String isResult= soloSign.issueTokenV2(tokenType,"10000.123456789","发行token");

        byte[] response1 = singleSign.singleSignIssueMethod(isResult, "C:\\Users\\Administrator\\Downloads\\163\\key1.pem");
        System.out.println("-----------------------多签发行--------------------------");
        System.out.println(Util.byteToHex(response1));

        //assertThat(tokenType+"发行token错误",isResult, containsString("200"));

        Thread.sleep(SLEEPTIME);
        //log.info("查询归集地址中token余额");
        //String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);

        //assertThat(tokenType+"查询余额错误",response1, containsString("200"));

        //assertThat(tokenType+"查询余额不正确",response1, containsString("10000.123456789"));

    }


    /**
     * Tc024单签正常流程:
     *
     */
//    @Test
//    public void TC024_SoloProgress() throws Exception {
//        String transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
//        log.info(transferData);
//        List<Map> listModel = utilsClass.constructToken(ADDRESS3,tokenType,"100.25");
//        log.info(ADDRESS3);
//        List<Map> list=utilsClass.constructToken(ADDRESS5,tokenType2,"200.555",listModel);
//        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
//        Thread.sleep(SLEEPTIME);
//        assertThat(transferInfo, containsString("200"));
//        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
//        String queryInfo = soloSign.Balance( PRIKEY3, tokenType);
//        String queryInfo2 = soloSign.Balance( PRIKEY5, tokenType2);
//        assertThat(queryInfo, containsString("200"));
//        assertThat(queryInfo, containsString("100.25"));
//        assertThat(queryInfo2, containsString("200"));
//        assertThat(queryInfo2, containsString("200.555"));
//        log.info("3向4转账token1");
//        List<Map> list1 = soloSign.constructToken(ADDRESS4,tokenType,"30");
//        String recycleInfo = soloSign.Transfer(list1, PRIKEY3,"3向4转账token1");
//        assertThat(recycleInfo, containsString("200"));
//        Thread.sleep(SLEEPTIME);
//        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType2,"80");
//        String recycleInfo1 = soloSign.Transfer(list2, PRIKEY5,"5向4转账token2");
//        assertThat(recycleInfo1, containsString("200"));
//        Thread.sleep(SLEEPTIME);
//        List<Map> list3 = soloSign.constructToken(ADDRESS2,tokenType,"30");
//        List<Map>list4= soloSign.constructToken(ADDRESS2,tokenType2,"70",list3);
//        String recycleInfo2 = soloSign.Transfer(list4, PRIKEY4, "李四向小六转账30 TT001, 70 TT002");
//        assertThat(recycleInfo2, containsString("200"));
//        Thread.sleep(SLEEPTIME);
//        List<Map> list5 = soloSign.constructToken(ADDRESS2,tokenType2,"20");
//        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY5, "王五向小六转账20 TT002");
//        assertThat(recycleInfo3, containsString("200"));
//        Thread.sleep(SLEEPTIME);
//        List<Map> list6 = (soloSign.constructToken(ADDRESS4,tokenType2,"30"));
//        List<Map> list7= soloSign.constructToken(ADDRESS4,tokenType2,"50",list6);
//        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY2, "小六向李四转账80");
//        log.info(recycleInfo4);
//        assertThat(recycleInfo4, containsString("200"));
//        Thread.sleep(SLEEPTIME);
//        String queryInfo3TK1 = soloSign.Balance(PRIKEY3, tokenType);
//        assertThat(queryInfo3TK1, containsString("70.25"));
//        String Info1 = multiSign.Recycle("", PRIKEY3, tokenType, "70.25");
//        assertThat(Info1, containsString("200"));
//        log.info("帐号3，token1余额正确");
//        String queryInfo4TK1 = soloSign.Balance(PRIKEY4, tokenType);
//        assertThat(queryInfo4TK1, containsString("0"));
//        log.info("帐号4，token1余额正确");
//        String queryInfo4TK2 = soloSign.Balance(PRIKEY4, tokenType2);
//        assertThat(queryInfo4TK2, containsString("90"));
//        String Info2 = multiSign.Recycle("", PRIKEY4, tokenType2, "90");
//        assertThat(Info2, containsString("200"));
//        log.info("帐号4，token2余额正确");
//        String queryInfo5TK2 = soloSign.Balance(PRIKEY5, tokenType2);
//        assertThat(queryInfo5TK2, containsString("100.555"));
//        String Info3 = multiSign.Recycle("", PRIKEY5, tokenType2, "100.555");
//        assertThat(Info3, containsString("200"));
//        log.info("帐号5，token2余额正确");
//        String queryInfo6TK1 = soloSign.Balance(PRIKEY2, tokenType);
//        assertThat(queryInfo6TK1, containsString("30"));
//        String Info4 = multiSign.Recycle("", PRIKEY2, tokenType, "30");
//        assertThat(Info4, containsString("200"));
//        log.info("帐号6，token1余额正确");
//        String queryInfo6TK2 = soloSign.Balance(PRIKEY2, tokenType2);
//        assertThat(queryInfo6TK2, containsString("10"));
//        String Info5 = multiSign.Recycle("", PRIKEY2, tokenType2, "10");
//        assertThat(Info5, containsString("200"));
//        log.info("帐号6，token2余额正确");
//    }


}
