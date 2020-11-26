package com.tjfintech.common.functionTest.ScfTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.TestBuilder;

import static com.tjfintech.common.performanceTest.ConfigurationTest.tokenType;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;
@Slf4j
public class GoScfTest {
        TestBuilder testBuilder = TestBuilder.getInstance();
        Scf scf = testBuilder.getScf();
        public static long expireDate = System.currentTimeMillis() + 100000000;
        String tokenType = "c";
        int levelLimit = 5;
        String amount = "100.0";

    @Test
    public void IssuingApply_Test01() {
        //资产开立申请
        String response = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("success"));
        assertThat(response, containsString("data"));
        //开立审核
        String response1 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        //开立签收
        String response2 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
    }


}
