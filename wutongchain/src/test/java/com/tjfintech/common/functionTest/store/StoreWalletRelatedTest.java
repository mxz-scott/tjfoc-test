package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class StoreWalletRelatedTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    /**
     * TC17创建两笔数据一样的存证
     * 预期：两者返回相同哈希，返回500 提示重复存证
     * @throws Exception
     */
    @Test
    public void TC17_CreateStoreDouble()throws Exception{
        String data="cxTest-"+ UtilsClass.Random(2);
        String response= store.CreateStore(data);
        Thread.sleep(1*1000);
        String response2= store.CreateStore(data);
        String hash1=JSONObject.fromObject(response).getString("data");
        String hash2=JSONObject.fromObject(response2).getString("message");
        assertThat(response, CoreMatchers.containsString("200"));
        assertThat(response2, CoreMatchers.containsString("500"));
        assertThat(hash2, CoreMatchers.containsString("Duplicate transaction"));
        assertThat(hash2, CoreMatchers.containsString(hash1));
    }

    /**
     * TC276根据哈希判断交易是否存在于钱包数据库
     * @throws Exception
     */
    @Test
    public void TC276_getInlocal()  throws Exception{

        for(int i = 0; i < 5; i++) {
            String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
            String response= store.CreateStore(Data);
            JSONObject jsonObject=JSONObject.fromObject(response);
            String storeHash = jsonObject.getString("data");
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME);
            String response2= store.GetInlocal(storeHash);
            assertThat(response2,containsString("200"));
        }
    }

    @Test
    public void TC_apiHealthTest() throws Exception {
        assertThat(store.GetApiHealth(),containsString("success"));
    }
}
