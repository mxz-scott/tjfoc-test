package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    Store store = testBuilder.getStore();
//    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

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

//        sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        
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

//        sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response2= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount1);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(true,JSONObject.fromObject(response2).getString("data").contains("{&#34;test&#34;:&#34"));

        String response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount2);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(true,JSONObject.fromObject(response3).getString("data").contains("{&#34;test&#34;:&#34;json"));

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

//        sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

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

//       sleepAndSaveInfo(SLEEPTIME,"private store on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = tokenModule.tokenGetPrivateStore(hash,tokenAccount1);
        assertEquals("200",JSONObject.fromObject(res3).getString("state"));
        assertEquals("{&#34;test1&#34;:true,&#34;test2&#34;:&#34;30&#34;}",JSONObject.fromObject(res3).getString("data"));

        //20191114 暂未支持授权
    }

    @Test
    public void tokenCreatePrivateStoreWithMultiAddr()throws  Exception{

        JSONObject result = new JSONObject();
        result.put("test1", true);
        result.put("test2", "30");
        String Data = result.toString();
        log.info(Data);

        Map<String,Object>map=new HashMap<>();
        map.put("address1",tokenMultiAddr1);
        String response= tokenModule.tokenCreatePrivateStore(Data,map);
        assertEquals(true,response.contains("address can not be multi address!"));

        map.put("address2",tokenAccount1);
        response= tokenModule.tokenCreatePrivateStore(Data,map);
        assertEquals(true,response.contains("address can not be multi address!"));

    }

    //存证大数据
    @Test
    public void createBigSizeStore() throws Exception {

        Map<String,Object>map=new HashMap<>();
        map.put("address1",tokenAccount1);


        List<String> listData = new ArrayList<>();
        ArrayList<String> hashList = new ArrayList<>();

        SDKADD = TOKENADD;
        String Data = utilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath +
                "bigsize1.txt");
        String response= tokenModule.tokenCreatePrivateStore(Data,map);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        hashList.add(JSONObject.fromObject(response).getString("data"));
        listData.add(Data);



        String Data2 = utilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath
                +  "bigsize2.txt");
        String response2 = tokenModule.tokenCreatePrivateStore(Data2,map);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        hashList.add(JSONObject.fromObject(response2).getString("data"));
        listData.add(Data2);


        String Data3 = utilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath
                + "bigsize3.txt");
        String response3 = tokenModule.tokenCreatePrivateStore(Data3,map);
        assertEquals("400",JSONObject.fromObject(response3).getString("state"));
//        hashList.add(JSONObject.fromObject(response3).getString("data"));
//        listData.add(Data3);


//        sleepAndSaveInfo(SLEEPTIME,"multi store on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(response2,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        for(int i=0;i<hashList.size();i++){
            String hash = hashList.get(i);
            //确认交易上链
            assertEquals("200",JSONObject.fromObject(tokenModule.tokenGetTxDetail(hash)).getString("state"));

            //确认交易可以成功查询
            String resp= tokenModule.tokenGetPrivateStore(hash,tokenAccount1);
            assertEquals("200",JSONObject.fromObject(resp).getString("state"));
            assertEquals(listData.get(i),JSONObject.fromObject(resp).getString("data"));
        }

//        SDKADD = rSDKADD;
//        for(int i=0;i<hashList.size();i++){
//            String hash = hashList.get(i);
//            //确认交易上链
//            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hash)).getString("State"));
//
//            //确认交易可以成功查询
//            String resp= store.GetStorePost(hash,tokenAccount1);
//            assertEquals("200",JSONObject.fromObject(resp).getString("State"));
//            assertEquals(listData.get(i),JSONObject.fromObject(resp).getString("Data"));
//        }
    }

    //存证特殊字符
    @Test
    public void createSpecCharStore() throws Exception {

        Map<String,Object>map=new HashMap<>();
        map.put("address1",tokenAccount1);

        List<String> listData = new ArrayList<>();
        ArrayList<String> hashList = new ArrayList<>();

        SDKADD = TOKENADD;
        String Data = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~¥¦§¨©ª«¬®¯°±²³´中“”お⛑";;
        String response = tokenModule.tokenCreatePrivateStore(Data,map);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        hashList.add(JSONObject.fromObject(response).getString("data"));
        listData.add("!&#34;#$%&amp;&#39;()*+,-./:;&lt;=&gt;?@[\\]^_`{|}~¥¦§¨©ª«¬®¯°±²³´中“”お⛑");



        String Data2 = "<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>";
        String response2 = tokenModule.tokenCreatePrivateStore(Data2,map);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        hashList.add(JSONObject.fromObject(response2).getString("data"));
        listData.add("&lt;SCRIPT SRC=http://***/XSS/xss.js&gt;&lt;/SCRIPT&gt;");


        sleepAndSaveInfo(SLEEPTIME,"multi store on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

//        SDKADD = rSDKADD;

        for(int i=0;i<hashList.size();i++){
            String hash = hashList.get(i);
            //确认交易上链
            assertEquals("200",JSONObject.fromObject(tokenModule.tokenGetTxDetail(hash)).getString("state"));

            //确认交易可以成功查询
            String resp= tokenModule.tokenGetPrivateStore(hash,tokenAccount1);
            assertEquals("200",JSONObject.fromObject(resp).getString("state"));
            assertEquals(listData.get(i),JSONObject.fromObject(resp).getString("data"));
        }

//        SDKADD = rSDKADD;
//        for(int i=0;i<hashList.size();i++){
//            String hash = hashList.get(i);
//            //确认交易上链
//            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hash)).getString("State"));
//
//            //确认交易可以成功查询
//            String resp= store.GetStorePost(hash,"");
//            assertEquals("200",JSONObject.fromObject(resp).getString("State"));
//            assertEquals(listData.get(i),JSONObject.fromObject(resp).getString("Data"));
//        }

    }
}
