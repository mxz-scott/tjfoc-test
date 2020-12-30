package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.util.Date;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CommonInterfaceTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Contract contract=testBuilder.getContract();



    @Test
    public void getMemberlist()throws  Exception{
        String response= store.GetMemberList();
        assertThat(response,containsString("state"));
        assertThat(response,containsString("200"));
        assertThat(response,containsString("message"));
        assertThat(response,containsString("success"));
        assertThat(response,containsString("data"));
        assertThat(response,containsString("memberList"));
        assertThat(response,containsString("id"));
        assertThat(response,containsString("version"));
        assertThat(response,containsString("port"));
        assertThat(response,containsString("shownName"));
        assertThat(response,containsString("inAddr"));
        assertThat(response,containsString("height"));
        assertThat(response,containsString("hashType"));
        assertThat(response,containsString("consensus"));
        assertThat(response,containsString("licOutTime"));
        assertThat(response,containsString("breaked"));
    }

    @Test
    public void getLedgers()throws  Exception{
        String response= store.GetLedger();
        assertThat(response,containsString("state"));
        assertThat(response,containsString("200"));
        assertThat(response,containsString("message"));
        assertThat(response,containsString("success"));
        assertThat(response,containsString("data"));
        assertThat(response,containsString("ledgers"));
        assertThat(response,containsString("name"));
        assertThat(response,containsString("memberList"));
        assertThat(response,containsString("id"));
        assertThat(response,containsString("port"));
        assertThat(response,containsString("shownName"));
        assertThat(response,containsString("inAddr"));
        assertThat(response,containsString("hashType"));
        assertThat(response,containsString("word"));
        assertThat(response,containsString("cons"));
        assertThat(response,containsString("timeStamp"));
        assertThat(response,containsString("id"));
        assertThat(response,containsString("number"));
    }

    @Test
    public void getAPIHealth()throws  Exception{
        String response= store.GetApiHealth();
        assertThat(response,containsString("200"));
        assertThat(response,containsString("success"));
    }


    /**
     * 获取区块原始数据信息，base64编码
     */
    @Test
    public void getBlockRawDetail() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("data");
        assertThat(response,containsString("200"));
        String response2= store.GetBlockRawDetail(height-1);
        assertThat(response2,containsString("200"));
        JSONObject.fromObject(response2).getInt("state");
        JSONObject.fromObject(response2).getString("message");
        JSONObject.fromObject(response2).getJSONObject("data").getString("extra");
        JSONObject.fromObject(response2).getJSONObject("data").getString("txs");
        JSONObject.fromObject(response2).getJSONObject("data").getString("raw");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("version");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("height");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("timestamp");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("blockHash");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("previousHash");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("worldStateRoot");
        JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("transactionRoot");

    }



    /**
     * 获取链上合约列表信息
     */
    @Test
    public void getSmartContractList() {
        String response2= contract.GetSmartContractList();
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
        JSONObject.fromObject(response2).getInt("state");
        JSONObject.fromObject(response2).getString("message");
        JSONObject.fromObject(response2).getString("data");
        assertThat(response2,containsString("Name"));
        assertThat(response2,containsString("Version"));
        assertThat(response2,containsString("ContractStatus"));
        assertThat(response2,containsString("Owner"));
        assertThat(response2,containsString("Src"));

    }









    //@Test
    public void getPeerlist()throws  Exception{

        String response= store.GetPeerList();
        assertThat(response,containsString("200"));
        assertThat(response,containsString("success"));
        assertThat(response,containsString("data"));
        assertThat(response,containsString("ip"));
        assertThat(response,containsString("state"));
    }

    //20200728 代码不再检查重复交易
//    @Test
    public void createStoreDupDataString() throws Exception {
        boolean bWalletEnabled = commonFunc.getSDKWalletEnabled();
        String Data = "test11234567";
        long nowTime = (new Date()).getTime();
        String response= store.CreateStore(Data);
        String storeHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

//        //立即发 不管钱包是否开启 连续发送应该都会报错
//        String response12 = store.CreateStore(Data);
//        assertThat(response12,
//                anyOf(containsString("Duplicate transaction body,tx hash: " + storeHash),
//                        containsString("transactionFilter exist")));

        //钱包关闭时sdk配置重复检查时间间隔不生效
        if(bWalletEnabled) {
            while((new Date()).getTime() - nowTime < 2000 ) {
                String response13 = store.CreateStore(Data);
                log.info(response13);
                assertThat(response13,
                        anyOf(containsString("Duplicate transaction body,"),
                                containsString("txId_hex:" + storeHash),
                                containsString("transactionFilter exist")));
                sleepAndSaveInfo(400, "waiting......"); //不超过检测时间间隔 模拟手动连续点击发送
            }
        }
        sleepAndSaveInfo(1500,"store on chain waiting"); //超过dup检测时间
        String response2 = store.CreateStore(Data);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(storeHash)).getString("state"));

        String response4= store.GetStore(storeHash);
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response4).getJSONObject("data").getJSONObject("store").getString("storeData"));
    }

}
