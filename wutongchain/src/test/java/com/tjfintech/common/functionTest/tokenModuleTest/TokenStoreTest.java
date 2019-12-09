package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TokenStoreTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    Token tokenModule = testBuilder.getToken();

    /**
     * TC05-创建存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，data为交易哈希
     * 查询交易上链
     * getstore查询存证数据
     */
    @Test
    public void createStoreDataString() throws Exception {
        SDKADD = TOKENADD;
        String Data = "test11234567"+UtilsClass.Random(4);
        String response= tokenModule.tokenCreateStore(Data);
        String storeHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"store on chain waiting");

        //使用token模块getstore接口查询
        String response3 = tokenModule.tokenGetPrivateStore(storeHash,"");
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response3).getString("data"));

        SDKADD = rSDKADD;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(storeHash)).getString("State"));

        String response2= store.GetStore(storeHash);
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals(Data,JSONObject.fromObject(response2).getString("Data"));
    }


    /**
     * TC05-重复性检查 包括设定的检查时间
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，data为交易哈希
     * 查询交易上链
     * getstore查询存证数据
     */
    @Test
    public void createStoreDupDataString() throws Exception {
        SDKADD = TOKENADD;
        String Data = "test11234567";
        String response= tokenModule.tokenCreateStore(Data);
        String storeHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String response12 = tokenModule.tokenCreateStore(Data);
//        assertEquals(true,response12.contains("Duplicate transaction, hash: " + storeHash));
        assertThat(response12,
                anyOf(containsString("Duplicate transaction, hash: " + storeHash),
                        containsString("transactionFilter exist")));

        sleepAndSaveInfo(400,"waiting......"); //不超过检测时间间隔 模拟手动连续点击发送
        String response13 = tokenModule.tokenCreateStore(Data);
//        assertEquals(true,response13.contains("Duplicate transaction, hash: " + storeHash));
        assertThat(response13,
                anyOf(containsString("Duplicate transaction, hash: " + storeHash),
                        containsString("transactionFilter exist")));
        sleepAndSaveInfo(400,"waiting......"); //不超过检测时间间隔 模拟手动连续点击发送
        String response14 = tokenModule.tokenCreateStore(Data);
//        assertEquals(true,response14.contains("Duplicate transaction, hash: " + storeHash));
        assertThat(response14,
                anyOf(containsString("Duplicate transaction, hash: " + storeHash),
                        containsString("transactionFilter exist")));
        sleepAndSaveInfo(400,"waiting......"); //不超过检测时间间隔 模拟手动连续点击发送
        String response15 = tokenModule.tokenCreateStore(Data);
//        assertEquals(true,response15.contains("Duplicate transaction, hash: " + storeHash));
        assertThat(response15,
                anyOf(containsString("Duplicate transaction, hash: " + storeHash),
                        containsString("transactionFilter exist")));
        sleepAndSaveInfo(400,"waiting......"); //不超过检测时间间隔 模拟手动连续点击发送
        String response16 = tokenModule.tokenCreateStore(Data);
//        assertEquals(true,response16.contains("Duplicate transaction, hash: " + storeHash));
        assertThat(response16,
                anyOf(containsString("Duplicate transaction, hash: " + storeHash),
                        containsString("transactionFilter exist")));
        sleepAndSaveInfo(200,"waiting......"); //不超过检测时间间隔 模拟手动连续点击发送
        String response17 = tokenModule.tokenCreateStore(Data);
//        assertEquals(true,response16.contains("Duplicate transaction, hash: " + storeHash));
        assertThat(response17,
                anyOf(containsString("Duplicate transaction, hash: " + storeHash),
                        containsString("transactionFilter exist")));


        sleepAndSaveInfo(SLEEPTIME,"store on chain waiting"); //超过dup检测时间
        String response2 = tokenModule.tokenCreateStore(Data);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));

        //使用token模块getstore接口查询
        String response3 = tokenModule.tokenGetPrivateStore(storeHash,"");
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response3).getString("data"));

        SDKADD = rSDKADD;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(storeHash)).getString("State"));

        String response4= store.GetStore(storeHash);
        assertEquals("200",JSONObject.fromObject(response4).getString("State"));
        assertEquals(Data,JSONObject.fromObject(response4).getString("Data"));
    }

    /**
     *TC292-获取存证交易byhash
     * 通过TC05全局变量storeHash用于查询测试
     * 预期：返回200，Data为存证内容
     */
    @Test
    public void createStoreDataJson() throws  Exception {
        String rand = UtilsClass.Random(3);
        String Data = "{\"testJson\":\"json" + rand + "\"}";
        SDKADD = TOKENADD;
        String response= tokenModule.tokenCreateStore(Data);
        String storeHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"store on chain waiting");

        SDKADD = rSDKADD;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(storeHash)).getString("State"));

        String response2= store.GetStore(storeHash);
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals(Data,JSONObject.fromObject(response2).getString("Data"));
    }


    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void createStoreDataComplexJson() throws Exception {

        JSONObject fileInfo = new JSONObject();
        JSONObject data = new JSONObject();

        fileInfo.put("fileName", "201911041058.jpg");
        fileInfo.put("fileSize", "298KB");
        fileInfo.put("fileModel", "iphoneXR");
        fileInfo.put("fileLongitude", 123.45784545);
        fileInfo.put("fileStartTime", "1571901219");
        fileInfo.put("fileFormat", "jpg");
        fileInfo.put("fileLatitude", 31.25648);

        data.put("projectCode", UtilsClass.Random(10));
        data.put("waybillId", "1260");
        data.put("fileInfo", fileInfo);
        data.put("fileUrl", "/var/mobile/containers/data/111");
        data.put("projectName", "钰翔供应链测试006");
        data.put("projectId", "1234");
        data.put("fileType", "2");
        data.put("fileId", "");
        data.put("waybillNo", "y201911041032");
        String Data = data.toString();
        log.info(Data);

        SDKADD = TOKENADD;
        String response= tokenModule.tokenCreateStore(Data);
        String storeHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"store on chain waiting");

        SDKADD = rSDKADD;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(storeHash)).getString("State"));

        String response2= store.GetStore(storeHash);
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals(Data,JSONObject.fromObject(response2).getString("Data"));
//        assertEquals("1234",JSONObject.fromObject(response2).getJSONObject("Data").getString("projectId"));
//        assertEquals("jpg",JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("fileInfo").getString("fileFormat"));
    }

    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void createStoreDataComplexJson30() throws Exception {
        List<String> listData = new ArrayList<>();
        ArrayList<String> hashList = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            JSONObject fileInfo = new JSONObject();
            JSONObject data = new JSONObject();

            fileInfo.put("fileName", "201911041058.jpg");
            fileInfo.put("fileSize", "298KB");
            fileInfo.put("fileModel", UtilsClass.Random(6));
            fileInfo.put("fileLongitude", 123.45784545);
            fileInfo.put("fileStartTime", "1571901219");
            fileInfo.put("fileFormat", "jpg");
            fileInfo.put("fileLatitude", 31.25648);

            data.put("projectCode", UtilsClass.Random(10));
            data.put("waybillId", "1260");
            data.put("fileInfo", fileInfo);
            data.put("fileUrl", "/var/mobile/containers/data/111");
            data.put("projectName", "钰翔供应链测试005");
            data.put("projectId", "1234");
            data.put("fileType", "2");
            data.put("fileId", "");
            data.put("waybillNo", "y201911041032");
            String Data = data.toString();
            listData.add(Data);
            log.info(Data);

            SDKADD = TOKENADD;
            String response= tokenModule.tokenCreateStore(Data);
            String storeHash = JSONObject.fromObject(response).getString("data");
            assertEquals("200",JSONObject.fromObject(response).getString("state"));
            hashList.add(storeHash);
        }

        sleepAndSaveInfo(SLEEPTIME,"multi store on chain waiting......");

        SDKADD = rSDKADD;
        for(int i = 0;i<hashList.size();i++){
            String hash = hashList.get(i);
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hash)).getString("State"));

            String response2= store.GetStore(hash);
            assertEquals("200",JSONObject.fromObject(response2).getString("State"));
            assertEquals(listData.get(i),JSONObject.fromObject(response2).getString("Data"));
        }

    }
    

    //存证大数据
    @Test
    public void createBigSizeStore() throws Exception {
        List<String> listData = new ArrayList<>();
        ArrayList<String> hashList = new ArrayList<>();

        SDKADD = TOKENADD;
        String Data = UtilsClass.Random(10) + UtilsClass.readStringFromFile(resourcePath +
                "bigsize1.txt");
        String response = tokenModule.tokenCreateStore(Data);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        hashList.add(JSONObject.fromObject(response).getString("data"));
        listData.add(Data);



        String Data2 = UtilsClass.Random(10) + UtilsClass.readStringFromFile(resourcePath
                +  "bigsize2.txt");
        String response2 = tokenModule.tokenCreateStore(Data2);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        hashList.add(JSONObject.fromObject(response2).getString("data"));
        listData.add(Data2);


        String Data3 = UtilsClass.Random(10) + UtilsClass.readStringFromFile(resourcePath
                + "bigsize3.txt");
        String response3 = tokenModule.tokenCreateStore(Data3);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        hashList.add(JSONObject.fromObject(response3).getString("data"));
        listData.add(Data3);


        sleepAndSaveInfo(SLEEPTIME,"multi store on chain waiting......");
        SDKADD = rSDKADD;

        for(int i=0;i<hashList.size();i++){
            String hash = hashList.get(i);
            //确认交易上链
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hash)).getString("State"));

            //确认交易可以成功查询
            String resp= store.GetStore(hash);
            assertEquals("200",JSONObject.fromObject(resp).getString("State"));
            assertEquals(listData.get(i),JSONObject.fromObject(resp).getString("Data"));
        }

    }

     /**
     * TC18 连续发送N笔存证交易
     * @throws Exception
     */
   @Test
   public void TC18_CreateStoreDataString50()throws  Exception{
       List<String> listData = new ArrayList<>();
       ArrayList<String> hashList = new ArrayList<>();

       for (int i = 0; i < 50; i++) {
           String Data = "cx"+UtilsClass.Random(4);
           listData.add(Data);

           SDKADD = TOKENADD;
           String response= tokenModule.tokenCreateStore(Data);
           String storeHash = JSONObject.fromObject(response).getString("data");
           assertEquals("200",JSONObject.fromObject(response).getString("state"));
           hashList.add(storeHash);
       }

       sleepAndSaveInfo(SLEEPTIME,"multi store onchain waiting......");

       //token模块查询
       for(int i=0;i<hashList.size();i++){
           String hash = hashList.get(i);
           //确认交易上链
           assertEquals("200",JSONObject.fromObject(tokenModule.tokenGetTxDetail(hash)).getString("state"));

           //确认交易可以成功查询
           String response2= tokenModule.tokenGetPrivateStore(hash,"");
           assertEquals("200",JSONObject.fromObject(response2).getString("state"));
           assertEquals(listData.get(i),JSONObject.fromObject(response2).getString("data"));
       }

       SDKADD = rSDKADD;
       for(int i=0;i<hashList.size();i++){
           String hash = hashList.get(i);
           //确认交易上链
           assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hash)).getString("State"));

           //确认交易可以成功查询
           String response2= store.GetStore(hash);
           assertEquals("200",JSONObject.fromObject(response2).getString("State"));
           assertEquals(listData.get(i),JSONObject.fromObject(response2).getString("Data"));
       }
   }
}
