package com.tjfintech.common.functionTest.ScfTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.PostTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;
import static com.tjfoc.sdk.Client.log;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class Scf_DxTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Scf scf = testBuilder.getScf();
    Store store = testBuilder.getStore();
    public static double timeStampNow = System.currentTimeMillis();
    public static BigDecimal expireDate = new BigDecimal(timeStampNow + 1000000000);
    //public static long expireDate = System.currentTimeMillis() + 100000000;
    Kms kms = testBuilder.getKms();


    @BeforeClass
    public static void beforeConfig() throws Exception {
        ScfBeforeCondition bf = new ScfBeforeCondition();
        bf.B001_createPlatformAccount();
        bf.GetcommentsV2();
        bf.B002_createCoreCompanyAccount();
        bf.B003_installContracts();

        bf.B004_createSupplyAccounts();

        Thread.sleep(5000);
    }





}
