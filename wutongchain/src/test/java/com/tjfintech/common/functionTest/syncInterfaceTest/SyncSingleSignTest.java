package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import lombok.extern.slf4j.Slf4j;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PRIKEY1;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncSingleSignTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;
    private static String tokenType3;

    /**
     * 同步测试单签发行token
     */
    @Test
    public void SyncIssueToken() throws Exception {



    }
    /**
     * 同步测试单签转账交易
     */
    @Test
    public void SyncTransfer() throws Exception {


    }

}
