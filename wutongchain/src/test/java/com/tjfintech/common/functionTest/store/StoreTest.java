package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
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
public class StoreTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    /**
     * TC05-创建存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHash中用于查询测试
     */
    @Test
    public void TC05_createStore() throws Exception {

        String Data = "test11234567"+UtilsClass.Random(4);
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");
        JSONObject.fromObject(response).getInt("state");
        JSONObject.fromObject(response).getString("message");
        assertThat(response, containsString("200"));
        assertThat(response, containsString("success"));
        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String response2= store.GetTxDetail(storeHash);
        assertThat(response2, containsString("200"));
    }


    /**
     *TC292-获取存证交易byhash
     * 通过TC05全局变量storeHash用于查询测试
     * 预期：返回200，Data为存证内容
     */
    @Test
    public void TC292_getStore() throws  Exception {
        String Data = "{\"testJson\":\"json"+UtilsClass.Random(3)+"\"}";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String  storeHash = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String response2= store.GetTxDetail(storeHash);
        assertThat(response2, containsString("200"));
        assertThat(response2,containsString("json"));
        JSONObject.fromObject(response2).getString("data");
        JSONObject.fromObject(response2).getInt("state");
        JSONObject.fromObject(response2).getString("message");
    }


    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC006_createStore() throws Exception {

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

        String response= store.CreateStore(Data);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));

    }

    /**
     * 普通存证，内容为json
     * 数量为5
     */
    @Test
    public void TC_createJsonStore() throws Exception {

        for (int i = 0; i < 5; i++) {
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
            log.info(Data);

            String response= store.CreateStore(Data);
            Thread.sleep(1000);
            assertThat(response, containsString("200"));
            assertThat(response,containsString("data"));
            assertThat(response,containsString("success"));


        }

    }



    //存证大数据
    @Test
    public void TC278_createBigSizeStore() throws Exception {

        String Data = UtilsClass.Random(10) + utilsClass.readStringFromFile(testDataPath +
                "store/bigsize1.txt");
        String response = store.CreateStore(Data);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));


        String Data2 = UtilsClass.Random(10) + utilsClass.readStringFromFile(testDataPath
                + "store/bigsize2.txt");
        String response2 = store.CreateStore(Data2);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));


        String Data3 = UtilsClass.Random(10) + utilsClass.readStringFromFile(testDataPath
                + "store/bigsize3.txt");
        String response3 = store.CreateStore(Data3);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));

        assertEquals(response3,globalResponse);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String Data4 = UtilsClass.Random(10) + utilsClass.readStringFromFile(testDataPath
                + "store/bigsize4.txt");
        String response4 = store.CreateStore(Data4);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("data"));

        assertEquals(response4,globalResponse);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        //确认交易上链
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(
                commonFunc.getTxHash(response,utilsClass.sdkGetTxHashType00))).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(
                commonFunc.getTxHash(response4,utilsClass.sdkGetTxHashType00))).getString("state"));
    }

     /**
     * TC18 连续发送N笔存证交易
     * @throws Exception
     */
   @Test
   public void TC18_CreateStore50()throws  Exception{
       List<String>list=new ArrayList<>();
       for(int i=0;i<50;i++){
           list.add(store.CreateStore("cx"+UtilsClass.Random(4)));
       }
       //确认最后一个存证上链
       commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
               utilsClass.sdkGetTxDetailType,SLEEPTIME);

       for(int i=0;i<list.size();i++){
           assertThat(list.get(i), containsString("200"));
           JSONObject jsonObject=JSONObject.fromObject(list.get(i));
           String storeHash = jsonObject.getString("data");
           assertEquals(storeHash.isEmpty(),false);
           //检查交易上链
           assertEquals("200",JSONObject.fromObject(store.GetTxDetail(storeHash)).getString("state"));
       }
   }


//----------------------------------------------------------------------------------------------------------------

                                                                                                                                               //
                                                                                                                                                //    }
    @Test
    public void TC279_getTxDetail() throws  Exception {
        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        commonFunc.verifyTxDetailField(storeHash,"store", "0", "0", "0");
        commonFunc.verifyTxRawField(storeHash, "0", "0", "0");
        commonFunc.verifyRawFieldMatch(storeHash);

    }

    @Test
    public void TC279_getTxRaw() throws  Exception {
        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        commonFunc.verifyTxRawField(storeHash, "0", "0", "0");
        commonFunc.verifyRawFieldMatch(storeHash);
    }

    /**
     * tc279获取交易索引
     * @throws Exception
     */
    @Test
    public void TC279_getTransactionIndex() throws  Exception {
        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String response2= store.GetTransactionIndex(storeHash);
        JSONObject.fromObject(response2).getInt("state");
        JSONObject.fromObject(response2).getString("message");
        JSONObject.fromObject(response2).getInt("data");
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }

    /**
     * Tc275获取区块高度
     */
    @Test
    public void TC275_getHeight() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        jsonObject.getInt("state");
        jsonObject.getString("message");
        int height = jsonObject.getInt("data");
        assertThat(response,containsString("200"));
        assertThat(response,containsString("success"));

    }

    /**
     * TC274根据高度查询某个区块信息
     */
    @Test
    public void TC274_getBlockByHeight() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("data");
        assertThat(response,containsString("200"));
        int Height=4;
        String response2= store.GetBlockByHeight(Height-1);
        assertThat(response2,containsString("200"));
        JSONObject.fromObject(response2).getInt("state");
        JSONObject.fromObject(response2).getString("message");
        JSONObject.fromObject(response2).getJSONObject("data").getString("extra");
        JSONObject.fromObject(response2).getJSONObject("data").getString("txs");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("version");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("height");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("timestamp");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("blockId");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("previousId");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("worldStateRoot");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("transactionRoot");
    }

    @Test
    public void TC243_getBlockByHash() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("data");
        assertThat(response,containsString("200"));
        String response2= store.GetBlockByHeight(height-2);
        log.info(response2);
        String hash=JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("blockId");
        String response3= store.GetBlockByHash(hash);
        assertEquals(response2.equals(response3),true);
        JSONObject.fromObject(response3).getInt("state");
        JSONObject.fromObject(response3).getString("message");
        JSONObject.fromObject(response3).getJSONObject("data").getString("extra");
        JSONObject.fromObject(response3).getJSONObject("data").getString("txs");
        JSONObject.fromObject(response3).getJSONObject("data").getJSONObject("header").getString("version");
        JSONObject.fromObject(response3).getJSONObject("data").getJSONObject("header").getString("height");
        JSONObject.fromObject(response3).getJSONObject("data").getJSONObject("header").getString("timestamp");
        JSONObject.fromObject(response3).getJSONObject("data").getJSONObject("header").getString("blockId");
        JSONObject.fromObject(response3).getJSONObject("data").getJSONObject("header").getString("previousId");
        JSONObject.fromObject(response3).getJSONObject("data").getJSONObject("header").getString("worldStateRoot");
        JSONObject.fromObject(response3).getJSONObject("data").getJSONObject("header").getString("transactionRoot");
    }



    @Test
    public void TC254_getTransationBlock()throws  Exception{
        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String response2=store.GetTransactionBlock(storeHash);
        log.info(response2);
        JSONObject.fromObject(response2).getInt("state");
        JSONObject.fromObject(response2).getString("message");
        JSONObject.fromObject(response2).getInt("data");
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }


}
