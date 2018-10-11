package com.tjfintech.common.performanceTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoloSemiTest {
    static TestBuilder testBuilder = TestBuilder.getInstance();
    private static SoloSign soloSign = testBuilder.getSoloSign();
    private static MultiSign multiSign = testBuilder.getMultiSign();
    private static UtilsClass utilsClass = new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    @Before
    public void beforeConfig() throws Exception {

    }
    public static void main(String[] args) throws  Exception {

        /**
         * Tc026不配置归集地址发token:
         *
         */
        log.info("-----不配置归集地址发token-----");
        log.info("删除归集地址Y/N");
       Scanner scanner = new Scanner(System.in);
        System.out.println(scanner.nextLine());
        log.info("发行Token");
        tokenType = "SOLOTC-" + UtilsClass.Random(6);
        String isResult = soloSign.issueToken(PRIKEY1, tokenType, "10000", "发行token");
        log.info(isResult);
        assertThat(isResult, containsString("400"));
        assertThat(isResult, containsString("invalid address"));
        /**
         * Tc252删除CA管理系统中的地址，确认不能发token:
         *
         */
        log.info("-----删除CA管理系统中的地址，确认不能发token-----");
        log.info("删除CA管理系统中的地址:4QqVU8DvcZNWQ7mAiuq8SFzZkhKW27PRAgo91Q716KqvK3jYxo");
        scanner = new Scanner(System.in);
        System.out.println(scanner.nextLine());
        log.info("发行Token");
        tokenType = "SOLOTC-" + UtilsClass.Random(6);
        String isResult1 = soloSign.issueToken(PRIKEY1, tokenType, "10000", "发行token");
        log.info(isResult1);
        assertThat(isResult1, containsString("tokenaddress verify failed"));
        log.info("恢复地址:4QqVU8DvcZNWQ7mAiuq8SFzZkhKW27PRAgo91Q716KqvK3jYxo");
        scanner = new Scanner(System.in);
        System.out.println(scanner.nextLine());
        /**
         * Tc255冻结token type测试:
         *
         */
        log.info("-----冻结token type测试-----");


            log.info("发行Token");
            tokenType = "SOLOTC-" + UtilsClass.Random(6);
            String isResult2 = soloSign.issueToken(PRIKEY1, tokenType, "10000", "发行token");
        log.info(isResult2);
        assertThat(isResult2, containsString("200"));
            log.info("冻结CA管理系统中的token type:"+tokenType);
            scanner = new Scanner(System.in);
            System.out.println(scanner.nextLine());

            String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType + ",并向" + PUBKEY4 + "转账";
            log.info(transferData);
            List<Map> list = utilsClass.constructToken(ADDRESS3, tokenType, "3000");
            String transferInfo = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list);
            log.info(transferInfo);
            assertThat(transferInfo, containsString("400"));
            String Info3 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "10000");
            assertThat(Info3, containsString("200"));

    }
}
