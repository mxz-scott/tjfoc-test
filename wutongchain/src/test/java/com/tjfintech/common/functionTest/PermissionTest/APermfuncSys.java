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
    UtilsClass utilsClass = new UtilsClass();

    String blockHash="blockhash111111";
    String txHash="txHash";
    String Data="test0000";

    String okCode="200";
    String okMsg="success";

    public String retAllow(String checkStr)throws Exception{
        String allow="*";
        boolean bNoPerm = false;
        if(checkStr.contains(NoPermErrCode)&&checkStr.contains(NoPermErrMsg)){
            bNoPerm = true;
        }
        if(checkStr.contains(okCode)) {
            allow = "1";
        }
        else if(bNoPerm)
        {
            allow="0";
        }
        return allow;
    }

    public String createStore()throws Exception {

        String response= store.CreateStore(Data);
        Thread.sleep(3000);
        if(response.contains("200")) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            txHash = jsonObject.getString("data");
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
     * tc278????????????????????????????????????[??????????????????????????????????????????????????????]
     * @throws Exception
     */
    public String getTransaction(String storeHash) throws  Exception {
        String response= store.GetTxDetail(storeHash);
        return retAllow(response);
    }

    /**
     * ??????????????????
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
     * ??????????????????
     */

    public String getHeight() throws  Exception {
        String response= store.GetHeight();
        return retAllow(response);
    }

    /**
     * ????????????????????????????????????
     */

    public String getBlockByHeight(int Height)  throws  Exception{
        String response= store.GetBlockByHeight(Height);

        if( response.contains(okCode)) {
            blockHash = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").get("blockId").toString();
        }
        return retAllow(response);
    }


    public String getBlockByBlockHash(String blockHash)  throws  Exception{
        String response= store.GetBlockByHash(blockHash);
        return retAllow(response);
    }


    /**
     * TC276??????????????????????????????????????????????????????
     * @throws Exception
     */
    public String getInlocal()  throws Exception{
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");
        Thread.sleep(3000);
        String response2= store.GetInlocal(storeHash);
        return retAllow(response2);
    }


    public String getPeerList()  throws Exception{
        String response= store.GetPeerList();
        return retAllow(response);
    }

//    public String txSearch(String keyWord)  throws Exception{
//        String response= store.GetTxSearch(0,1,keyWord);
//        return retAllow(response);
//    }
}
