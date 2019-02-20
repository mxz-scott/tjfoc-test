package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorelnvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();

    /**
     * Tc16 发送存证交易，data为空字符串
     * 预期返回400，提示空data
     */
    @Test
    public void TC16_CreateStoreNull(){
            String data = "";
            String  response = store.CreateStore(data);
            String message=JSONObject.fromObject(response).getString("Message");
            assertThat(message,equalTo("Data is mandatory"));
            assertEquals(response.contains("400"),true);
        }

    /**
     * TC17创建两笔数据一样的存证
     * 预期：两者返回相同哈希，返回500 提示重复存证
     * @throws Exception
     */
    @Test
    public void TC17_CreateStoreDouble()throws Exception{
         String data="cxTest-"+ UtilsClass.Random(2);
         String response= store.CreateStore(data);
         Thread.sleep(SLEEPTIME);
         String response2= store.CreateStore(data);
         Thread.sleep(SLEEPTIME);
         String hash1=JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
         String hash2=JSONObject.fromObject(response2).getString("Message");
         assertThat(response,containsString("200"));
         assertThat(response2,containsString("500"));
         assertThat(hash2,containsString("Duplicate transaction"));
         assertThat(hash2,containsString(hash1));
   }


}
