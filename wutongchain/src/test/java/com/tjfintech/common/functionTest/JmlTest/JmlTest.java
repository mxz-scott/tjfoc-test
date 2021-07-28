package com.tjfintech.common.functionTest.JmlTest;

import com.alibaba.fastjson.JSON;

import com.tjfintech.common.GoJml;
import com.tjfintech.common.Interface.Jml;
import com.tjfintech.common.CommonFunc;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassJml.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Store.*;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassJml;
import com.tjfintech.common.utils.UtilsClassScf;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j

public class JmlTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Jml jml = testBuilder.getJml();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();

    @Test
    /**
     * 获取银行列表
     */
    public void Test001_JmlBankList() {
        String response1 = jml.BankList ();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    @Test
    public void Test002_JmlAuthorizeAdd() throws Exception {
        String[] receiverPubkeys = {"LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRTk0bmxNQVVBY2ZiVE5RSXN4ZGdGQ0t2dlFpMlQNCmt5dHBNMTFIK1N0aFJnZzQ5ZXdpQ0ZnRjM5Wlg3ZG5rR0lBS2c5RFZuemprYTFPUlFoa2dvR0Z1SVE9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t"};
        System.out.println("receiverPubkeys" + receiverPubkeys[0]);
        //新增授权用户
        Map subject = UtilsClassJml.subject("321027197508106015", "尹平");
        String response1 = jml.AuthorizeAdd(subjectType, bankId, endTime, fileHash, subject);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        String txId = gettxId(response1);
        System.out.println("txId = " + txId);
        //获取新增授权用户上链信息
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking = store.GetTxDetail(txId);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        //查询数据接口
        String authId = getValueByKey(response1);
        System.out.println("authId = " + authId);
        String response2 = jml.CreditdataQuery(requestId, authId, personId, personName, purpose);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("\"itemName\":\"网格数据\""));
        assertThat(response2, containsString("\"itemName\":\"婚姻数据\""));
        assertThat(response2, containsString("\"itemName\":\"户籍信息\""));
        assertThat(response2, containsString("\"itemName\":\"用气信息\""));
        assertThat(response2, containsString("\"itemName\":\"用水信息\""));
        assertThat(response2, containsString("\"itemName\":\"车辆信息\""));
        assertThat(response2, containsString("\"itemName\":\"社保缴纳\""));
        assertThat(response2, containsString("\"itemName\":\"房屋权属\""));
        assertThat(response2, containsString("\"itemName\":\"严重失信\""));

        //上传授信额度
        Map results = UtilsClassJml.results("03833002155", "accepted", "信用良好，授信批准", "王春勇", "321022195706281518", 30000, 36, "7.8", "xxx助农贷");
        String response3 = jml.CreditloanFeedback(authId, receiverPubkeys, results);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));
        //获取上传授信额度上链信息
        String txId1 = gettxId(response3);
        System.out.println("txId = " + txId1);
        commonFunc.sdkCheckTxOrSleep(txId1, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking2 = store.GetTxDetail(txId1);
        assertThat(checking2, containsString("200"));
        assertThat(checking2, containsString("success"));

    }

}
