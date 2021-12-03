package com.tjfintech.common.stableTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.SmartTokenCommon;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
public class Temp_Test {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Token tokenModule = testBuilder.getToken();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    VerifyTests vt = new VerifyTests();
    SmartTokenCommon stc = new SmartTokenCommon();

    private static String tokenType;
    List<String> listurl = new ArrayList();

    @BeforeClass
    public static void beforeConfig() throws Exception {

    }






}
