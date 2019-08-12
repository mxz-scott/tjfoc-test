package com.tjfintech.common.functionTest.PermissionTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.lang.*;

//import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
//import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class APermfuncSys {
    TestBuilder testBuilder=TestBuilder.getInstance();
    //SoloSign soloSign=testBuilder.getSoloSign();
    MultiSign multiSign=testBuilder.getMultiSign();
    Store store =testBuilder.getStore();

    String blockHash="blockhash111111";
    String txHash="txHash";
    String Data="test0000";

    String okCode="200";
    String okMsg="success";

    String errCode="404";
    String errMsg="does not found Permission";

    public String retAllow(String checkStr)throws Exception{
        String allow="*";
        if(JSONObject.fromObject(checkStr).getString("State").equals(okCode)) {
            allow = "1";
        }
        else if(checkStr.contains(errCode)&&checkStr.contains(errMsg))
        {
            allow="0";
        }
        log.info(allow);
        return allow;
    }

    public String createStore()throws Exception {

        String response= store.CreateStore(Data);
        Thread.sleep(3000);
        if(response.contains("200")) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            txHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        }
        return retAllow(response);
    }

    public String createStorePwd()throws  Exception {

        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response= store.CreatePrivateStore(Data,map);
        Thread.sleep(3000);
        return retAllow(response);
    }
    public String getStorePost(String postHash) throws Exception {
        String response1= store.GetStorePost(postHash,PRIKEY1);
        String response2= store.GetStorePostPwd(postHash,PRIKEY6,PWD6);
        //String response3= store.GetStorePost(postHash,PRIKEY3);

        Thread.sleep(3000);
        return retAllow(response2);
    }


    public String getStore(String storeHash) throws  Exception {
        String response= store.GetStore(storeHash);
        return retAllow(response);
    }


    /**
     * tc278通过交易哈希获取交易内容[不仅仅是存证交易也可能是其他交易类型]
     * @throws Exception
     */
    public String getTransaction(String storeHash) throws  Exception {
        String response= store.GetTransaction(storeHash);
        return retAllow(response);
    }

    /**
     * 获取交易索引
     * @throws Exception
     */
    public String getTransactionIndex(String storeHash) throws  Exception {
        String response= store.GetTransactionIndex(storeHash);
        return retAllow(response);
    }

    public String getTransationBlock(String storeHash)throws  Exception{
        String response=store.GetTransactionBlock(storeHash);
        return retAllow(response);
    }
    /**
     * 获取区块高度
     */

    public String getHeight() throws  Exception {
        String response= store.GetHeight();
        return retAllow(response);
    }

    /**
     * 根据高度查询某个区块信息
     */

    public String getBlockByHeight(int Height)  throws  Exception{
        String response= store.GetBlockByHeight(Height);

        if( response.contains(okCode)) {
            blockHash = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("header").get("blockHash").toString();
        }
        return retAllow(response);
    }


    public String getBlockByBlockHash(String blockHash)  throws  Exception{
        String response= store.GetBlockByHash(blockHash);
        return retAllow(response);
    }


    /**
     * TC276根据哈希判断交易是否存在于钱包数据库
     * @throws Exception
     */
    public String getInlocal()  throws Exception{
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(3000);
        String response2= store.GetInlocal(storeHash);
        return retAllow(response2);
    }


    public String getPeerList()  throws Exception{
        String response= store.GetPeerList();
        return retAllow(response);
    }

    public String txSearch(String keyWord)  throws Exception{
        String response= store.GetTxSearch(0,1,keyWord);
        return retAllow(response);
    }
}
