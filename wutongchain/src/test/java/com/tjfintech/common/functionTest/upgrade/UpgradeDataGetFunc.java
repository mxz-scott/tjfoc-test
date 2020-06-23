package com.tjfintech.common.functionTest.upgrade;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.*;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetSDKStartWithApi;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetTestVersionLatest;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetTokenApiAddrSDK;
import com.tjfintech.common.utils.FileOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class UpgradeDataGetFunc {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Contract contract = testBuilder.getContract();
    Map<String, String> txTypeSubType = new HashMap<>();
    CommonFunc commonFunc = new CommonFunc();
    Token tokenModule = testBuilder.getToken();


    @Before
    public void updateKeyPairs()throws  Exception{
        //钱包开启时执行 关闭时不执行
        if(commonFunc.getSDKWalletEnabled()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.updatePubPriKey();
            beforeCondition.createAddresses();
            beforeCondition.collAddressTest();
        }
    }
    public Map<String,String> SaveResponseToHashMap_SDK(ArrayList<String> txList) throws Exception{
        SDKADD = rSDKADD;
        Map<String,String> mapTXHashResp = new HashMap<>();
        mapTXHashResp.clear();
        txTypeSubType.clear();

        //存储所有的交易hash查询结果
        for(int k = 0; k < txList.size(); k++){
            String response = store.GetTxDetail(txList.get(k));
            //if(!response.contains("{")) continue;
            log.info("tx detail ");
            JSONObject jsonObject = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Header");

            String txType = jsonObject.getString("Type");
            String txSubType = jsonObject.getString("SubType");

            txTypeSubType.put(txType + "_" + txSubType,"typecollection");//将所有类型和子类型全部保存到map中
            //仅取首个交易类型hash
//            if(storeHash.isEmpty() || priStoreHash.isEmpty() || contractHash.isEmpty()) {
            if(storeHash.isEmpty() || priStoreHash.isEmpty()) {
                //分别获取指定交易类型的交易hash 作为特定交易查看接口参数
                if (storeHash.isEmpty() && txType.equals("0") && txSubType.equals("0"))             storeHash = txList.get(k);
                else if (priStoreHash.isEmpty() && txType.equals("0") && txSubType.equals("1"))     priStoreHash = txList.get(k);
//                else if (contractHash.isEmpty() && txType.equals("2") && txSubType.equals("30"))    contractHash = txList.get(k);
            }
            mapTXHashResp.put(txList.get(k),response);
        }
        //assertEquals(subTypeNo,txTypeSubType.size()); //确认所有1.0以上的子交易类型全部覆盖

        mapTXHashResp.put("getheight",store.GetHeight());
        mapTXHashResp.put("getblockbyheight?number=1",store.GetBlockByHeight(1));//增加字段version

        String blockHash = JSONObject.fromObject(store.GetBlockByHeight(1)).getJSONObject("Data").getJSONObject("header").getString("blockHash");
        mapTXHashResp.put("getblockbyhash?hashData=****",store.GetBlockByHash(blockHash));

        mapTXHashResp.put("gettransactionindex?hashData=****",store.GetTransactionIndex(txHashList.get(1)));
        mapTXHashResp.put("gettransactionblock?hashData=****",store.GetTransactionBlock(txHashList.get(1)));
        mapTXHashResp.put("getpeerlist",store.GetPeerList()); //可能会出现节点顺序不一致的情况 无法直接存储比较

        mapTXHashResp.put("getledger",store.GetLedger(""));//存在版本号不相同的情况 无法直接存储比较
        log.info("pri store hash: " + priStoreHash + "\n" + PRIKEY1);
        mapTXHashResp.put("getstore",store.GetStore(storeHash));
        mapTXHashResp.put("get pri store",store.GetStorePost(priStoreHash,PRIKEY1));
        mapTXHashResp.put("getapihealth",store.GetApiHealth());

        //钱包开启时执行 否则不执行
        if(commonFunc.getSDKWalletEnabled()) {
            //UTXO交易
            mapTXHashResp.put("get multi utxo balance", multiSign.BalanceByAddr(IMPPUTIONADD, ""));
            mapTXHashResp.put("get solo utxo balance", soloSign.Balance(PRIKEY1, ""));
            mapTXHashResp.put("get zero account balance", multiSign.QueryZero(""));
            mapTXHashResp.put("get all utxo account balance info", commonFunc.getUTXOAccountBalance().toString());
        }
        return mapTXHashResp;
    }


    public ArrayList<String> getAllTxHashData()throws Exception{
        SDKADD = rSDKADD;
        ArrayList<String> txHashList = new ArrayList<>();
        if (!subLedger.isEmpty()) sleepAndSaveInfo(SLEEPTIME,"start waiting...");
        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        for(int i= blockHeight;i>0;i--){
            JSONArray blockTxArr = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONArray("txs");
            for(int k = 0; k < blockTxArr.size(); k++){
                txHashList.add(blockTxArr.getString(k));
            }
        }
        return txHashList;
    }


    public Map<String,String> SaveResponseToHashMap_Api(ArrayList<String> txList) throws Exception{
        Map<String,String> mapTXHashResp = new HashMap<>();
        mapTXHashResp.clear();
        txTypeSubType.clear();

        SDKADD = TOKENADD;//使用token api自带gettxdetail获取交易详情
        //存储所有的交易hash查询结果
        for(int k = 0; k < txList.size(); k++){
            String response = tokenModule.tokenGetTxDetail(txList.get(k));
            mapTXHashResp.put(txList.get(k),response);//将所有交易hash及根据交易hash查询的txdetail存储

            log.info("tx detail ");
            JSONObject jsonObject = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("Header");

            String txType = jsonObject.getString("Type");
            String txSubType = jsonObject.getString("SubType");

            //仅取首个交易类型hash
            if(storeHash.isEmpty() || priStoreHash.isEmpty()) {
                //分别获取指定交易类型的交易hash 作为特定交易查看接口参数
                if (storeHash.isEmpty() && txType.equals("0") && txSubType.equals("0"))             storeHash = txList.get(k);
                else if (priStoreHash.isEmpty() && txType.equals("0") && txSubType.equals("1"))     priStoreHash = txList.get(k);
            }
        }

        mapTXHashResp.put("getstore",tokenModule.tokenGetPrivateStore(storeHash,""));
        mapTXHashResp.put("get pri store",tokenModule.tokenGetPrivateStore(priStoreHash,tokenAccount1));

        //UTXO交易
        mapTXHashResp.put("get multi utxo balance",tokenModule.tokenGetBalance(tokenAccount1,""));
        mapTXHashResp.put("get zero account balance",tokenModule.tokenGetDestroyBalance());

        return mapTXHashResp;

    }


}
