package com.tjfintech.common.functionTest.JmlTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GoJml;
import com.tjfintech.common.Interface.Jml;
import static com.tjfintech.common.utils.UtilsClassJml.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
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

    @Test
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

        Map subject = UtilsClassJml.subject("321027197508106015", "尹平");
        String response1 = jml.AuthorizeAdd(subjectType, bankId, endTime, fileHash, subject);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));

        String authId = getValueByKey(response1);
        System.out.println("authId = " + authId);

        String response2 = jml.CreditdataQuery(requestId, authId, personId, personName, purpose);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));

        Map results = UtilsClassJml.results("03833002155", "accepted", "信用良好，授信批准", "王春勇", "321022195706281518", 30000, 36, "7.8", "xxx助农贷");
        String response3 = jml.CreditloanFeedback(authId, receiverPubkeys, results);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

    }

}
