package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TokenPrivateStoreTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule =testBuilder.getToken();

//    Store store =testBuilder.getStore();

    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }

    }


    /**
     *tc277获取隐私存证交易byhash
     * 预期：返回200，Data为存证内容
     * 错误私钥3，返回存证内容不正确
     */
    @Test
    public void tokenCreatePrivateStoreDataString() throws Exception {
        String Data = "cxTest-private" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);
        
        String response1= tokenModule.tokenCreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getString("data");

        sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");

        
        String response2= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount1);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response2).getString("data"));

        String response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount2);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response3).getString("data"));

        //使用无查询权限的用户进行查询
        String response4= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount3);
        assertThat(response4, containsString("you have no permission to get this transaction !"));
    }

     /**
     *TC09-创建隐私存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     *
     */

    @Test
    public void tokenCreatePrivateStoreCommonJson()throws  Exception {
        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        Map<String,Object>map=new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);

        String response1= tokenModule.tokenCreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getString("data");

        sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");


        String response2= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount1);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response2).getString("data"));

        String response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount2);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response3).getString("data"));

        //使用无查询权限的用户进行查询
        String response4= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount3);
        assertThat(response4, containsString("you have no permission to get this transaction !"));


    }

    /**
     * TC11查询隐私存证交易，数据格式为String
     * 创建2笔隐私存证交易，数据格式为string
     */
    @Test
    public void tokenGetPrivateStore2Tx() throws Exception {
        String data = "Testcx-" + UtilsClass.Random(2);
        String data2 = "Testcx-" + UtilsClass.Random(4);
        Map<String,Object>map=new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);
        String response = tokenModule.tokenCreatePrivateStore(data,map);
        String response2 = tokenModule.tokenCreatePrivateStore(data2,map);
        JSONObject jsonObject = JSONObject.fromObject(response);
        JSONObject jsonObject2 = JSONObject.fromObject(response2);
        String storeHash = jsonObject.getString("data");
        String storeHash2 = jsonObject2.getString("data");

        sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");

        String response6 = tokenModule.tokenGetPrivateStore(storeHash2,tokenAccount1);
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        assertEquals(data2,JSONObject.fromObject(response6).getString("data"));

        String response7 = tokenModule.tokenGetPrivateStore(storeHash,tokenAccount2);
        assertEquals("200",JSONObject.fromObject(response7).getString("state"));
        assertEquals(data,JSONObject.fromObject(response7).getString("data"));

    }

    // new private store ...
    @Test
    public void tokenCreatePrivateStoreComplexJson()throws  Exception{

        JSONObject result = new JSONObject();
        result.put("test1", true);
        result.put("test2", "30");
        String Data = result.toString();
        log.info(Data);

        Map<String,Object>map=new HashMap<>();
        map.put("addresses",tokenAccount1);
        String response= tokenModule.tokenCreatePrivateStore(Data,map);

       sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = tokenModule.tokenGetPrivateStore(hash,tokenAccount1);
        assertEquals("200",JSONObject.fromObject(res3).getString("state"));
        assertEquals(Data,JSONObject.fromObject(res3).getString("data"));

        //20191114 暂未支持授权
    }
}
