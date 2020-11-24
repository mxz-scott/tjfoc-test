package com.tjfintech.common.functionTest.ScfTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.TestBuilder;
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

    @Test
    public void create_Test01() {

        String response = scf.AccountCreate(platformKeyID, PIN, "", "this is a test");

        assertThat(response, containsString("200"));
        assertThat(response, containsString("success"));
        assertThat(response, containsString("data"));
        assertThat(response, containsString("keyID"));
        assertThat(response, containsString("pubKey"));
    }
}
