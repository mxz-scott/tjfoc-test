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


    /**
     * 测试同步接口返回结果后立即查询数据
     */
    @Test
    public void SyncTest() throws Exception {
        syncFlag = true;
        int i = 0;
        int loop = 100000; // 循环次数

        while (i < loop) {

            StoreAndQuery(subLedger);
            i++;
        }

    }

    // 同步接口发送普通存证并查询
    public void StoreAndQuery(String id) throws Exception {
        String Data = "test11234567" + UtilsClass.Random(4);
        String response = store.CreateStore(Data);
        assertThat(response, containsString("200"));
        JSONObject jsonObject = JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");

        String response2= store.GetTxDetail(storeHash);
        assertThat(response2, containsString("200"));

    }

}
