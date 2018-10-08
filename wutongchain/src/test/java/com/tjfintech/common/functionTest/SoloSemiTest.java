package com.tjfintech.common.functionTest;

import com.tjfintech.common.MultiSign;
import com.tjfintech.common.SoloSign;
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
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoloSemiTest {
    SoloSign soloSign = new SoloSign();
    MultiSign multiSign = new MultiSign();
    UtilsClass utilsClass=new UtilsClass();
    public static String tokenType;
    public static String tokenType2;

    @Before
    public void beforeConfig() throws Exception {

    }


    /**
     * Tc026不配置归集地址发token:
     *
     */
    @Test
    public void TC026_SoloProgress() throws Exception {

        log.info("发行Token");
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String isResult=soloSign.issueToken(PRIKEY1,tokenType,"10000","发行token");
        log.info(isResult);
        assertThat(isResult, containsString("400"));
    }

    /**
     * Tc252删除CA管理系统中的地址，确认不能发token:
     *
     */
    @Test
    public void TC252_SoloProgress() throws Exception {
        log.info("发行Token");
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String isResult=soloSign.issueToken(PRIKEY1,tokenType,"10000","发行token");
        log.info(isResult);
        assertThat(isResult, containsString("tokenaddress verify failed"));
    }

    /**
     * Tc255单签接口双花测试:
     *
     */
    @Test
    public void TC0255_SoloProgress() throws Exception {
        log.info("发行Token");
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String isResult=soloSign.issueToken(PRIKEY1,tokenType,"10000","发行token");
        log.info(isResult);
        assertThat(isResult, containsString("200"));
        String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list=utilsClass.constructToken(ADDRESS3,tokenType,"3000");
        String transferInfo=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
        log.info(transferInfo);
        assertThat(transferInfo, containsString("400"));
        //assertThat(transferInfo, containsString("insufficient balance"));



        String Info3 = multiSign.Recycle("", PRIKEY5, tokenType2, "10000");
        assertThat(Info3, containsString("400"));
}
}
