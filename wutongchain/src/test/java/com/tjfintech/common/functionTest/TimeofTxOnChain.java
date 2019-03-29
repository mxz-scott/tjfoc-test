package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.MultiSign;
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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TimeofTxOnChain {

    public   final static int   SLEEPTIME=8*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();

    @Test
    public void TC277_searchStoreInlocal()  throws Exception{
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        long nowTime = new Date().getTime();
        JSONObject jsonObject=JSONObject.fromObject(response);

        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();

        GetLocal(nowTime,storeHash);
        }


    @Test
    public void TC277_searchUTXOInlocal()  throws Exception{
        String tokenType1 = "zz1X-" + UtilsClass.Random(10);
        String tokenType2 = "q1qX-" + UtilsClass.Random(10);
        log.info("*****************************"+tokenType1);
        log.info("*****************************"+tokenType2);
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType1, "1000", "issue");
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");

        String response3 = multiSign.issueToken(IMPPUTIONADD, tokenType2, "5000", "issue");
        assertThat(response3, containsString("200"));
        String Tx2 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Tx");

        long nowTime = new Date().getTime();
        String response2 = multiSign.Sign(Tx1, PRIKEY5);
        assertThat(response2, containsString("200"));
        String response4 = multiSign.Sign(Tx2, PRIKEY5);
        assertThat(response4, containsString("200"));

        JSONObject jsonObject=JSONObject.fromObject(response2);
        String storeHash1 = jsonObject.getJSONObject("Data").get("TxId").toString();

        JSONObject jsonObject1=JSONObject.fromObject(response4);
        String storeHash2 = jsonObject1.getJSONObject("Data").get("TxId").toString();


        GetLocal(nowTime,storeHash2);
    }
    public void GetLocal(long time ,String hash) throws Exception{

        String response2= store.GetInlocal(hash);
        if (response2.indexOf("200")!=-1){
            long nowTime = new Date().getTime();
               log.info(nowTime -time+"ms");


        }else{
            Thread.sleep(200);
            GetLocal(time,hash);
        }


    }
@Test
    public  void recTest()throws Exception{
        for(;;){
            TC277_searchUTXOInlocal();
        }
    }

}