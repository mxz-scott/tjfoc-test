package com.tjfintech.common.performanceTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class StableAutoTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign=testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();

    /**
     * 每隔n秒执行一次普通存证　隐私存证　单签交易　多签交易
     */
    @Test
    public void T001_stableTest()throws Exception{
        while( true){
            int n = 1;

            String[] ledgerIds = {"0cb76d2645ce2bd402fe20024b4a10155ccdde2ace54dcf68fddb3e41d40e2bc",
                                "d818b8e09f01381ee1dad0ae0abd768642eb801647ec8c5b5b66c683782fa563",
                                "16b8fd58bc0a523a7a00718365d8fd3e88f03a1cda5533a6bd785df19a7fb1b6"};
//                                "357b32b8f0ee33fdcf38a9e293d58b4aee5f17a5aa13e8443a274bacbfaaa1e9"

            for (int i = 0; i < ledgerIds.length; i++){
                subLedger = ledgerIds[i];
                String Data1 = "\"test\":\"json"+ UtilsClass.Random(4)+"\"";
                String response= store.CreateStore(Data1);
                assertThat(response, containsString("200"));
                assertThat(response,containsString("data"));

                Thread.sleep(n*1000);//普通存证 休眠n秒

            }

        }
    }


    /**
     * 每隔n秒执行一次普通存证　隐私存证　单签交易　多签交易
     */
    @Test
    public  void stableTest()throws Exception{
        while( true){
            int n = 60;
            String Data1 = "\"test\":\"json"+ UtilsClass.Random(4)+"\"";
            String response= store.CreateStore(Data1);
            JSONObject jsonObject1=JSONObject.fromObject(response);
            String storeHash = jsonObject1.getJSONObject("data").get("figure").toString();
            Thread.sleep(SLEEPTIME);
            assertThat(response, containsString("200"));
            assertThat(response,containsString("data"));

            Thread.sleep(n*1000);//普通存证 休眠n秒

            String Data2 = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
            Map<String,Object> map=new HashMap<>();
            map.put("pubKeys",PUBKEY1);
            map.put("pubkeys",PUBKEY6);
            String response1= store.CreatePrivateStore(Data2,map);
            JSONObject jsonObject2=JSONObject.fromObject(response1);
            String StoreHashPwd = jsonObject2.getJSONObject("data").get("figure").toString();
            Thread.sleep(SLEEPTIME);
            assertThat(response1, containsString("200"));
            assertThat(response1,containsString("data"));

            Thread.sleep(n*1000);//隐私存证 休眠n秒

            String tokenType = "cx-8oVNI";
            String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
            Boolean flag=JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total").equals("0");
            if(flag){
                BeforeCondition beforeCondition=new BeforeCondition();
                beforeCondition.T284_BeforeCondition(tokenType);
                Thread.sleep(SLEEPTIME);
                queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
                assertThat(queryInfo, containsString("200"));
                log.info(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
                assertEquals(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total").equals("0"), false);
                /**
                 * 如果测试不通过请执行BeforeConditon类中的第二个方法.发行相应的币种
                 */
                List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType, "100");

                String transferInfo0 = multiSign.Transfer(PRIKEY4, "cx-test", IMPPUTIONADD, list0);//1 归集地址向多签地址转账
                Thread.sleep(SLEEPTIME);
                assertThat(transferInfo0, containsString("200"));
                List<Map> list1 = utilsClass.constructToken(ADDRESS1, tokenType, "100");
                String transferInfo1 = multiSign.Transfer(PRIKEY4, "cx-test", IMPPUTIONADD, list1);//1 归集地址向单签地址转账
                Thread.sleep(SLEEPTIME);
                assertThat(transferInfo1, containsString("200"));
            }
            String queryInfo1 = multiSign.BalanceByAddr(MULITADD4, tokenType);
            assertEquals(JSONObject.fromObject(queryInfo1).getJSONObject("data").getString("total").equals("0"), false);
            String queryInfo2= soloSign.BalanceByAddr(ADDRESS1, tokenType);
            assertEquals(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total").equals("0"), false);

            List<Map> list1 = utilsClass.constructToken(MULITADD5, tokenType, "0.5");
            String transferInfo1 = multiSign.Transfer(PRIKEY1, "cx-test", MULITADD4, list1);//1-2多签向多签地址转账
            assertThat(transferInfo1, containsString("200"));
            String transferData = "ADDRESS1向" + PUBKEY3 + "转账0.5个" + tokenType;
            List<Map> list=utilsClass.constructToken(ADDRESS3,tokenType,"0.5");
            String transferInfo2= multiSign.Transfer(PRIKEY1, transferData, ADDRESS1,list);//单签地址向单签地址转账
            assertThat(transferInfo1, containsString("200"));
            Thread.sleep(n*1000);//多签转账 单签转账 休眠n秒

        }
    }




}
