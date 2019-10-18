package com.tjfintech.common.functionTest.upgrade;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetVerLatest;
import com.tjfintech.common.functionTest.Conditions.SetVerRelease;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bouncycastle.util.StringList;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class UpgradeTestHistoryData {

    public   final static int   SHORTSLEEPTIME=3*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Contract contract = testBuilder.getContract();
    ArrayList<String> txHashList = new ArrayList<>();
    Map<String, String> txTypeSubType = new HashMap<>();
    String storeHash = "";
    String priStoreHash = "";
    String contractHash = "";


    @BeforeClass
    public static void updateKeyPairs()throws  Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.updatePubPriKey();
    }

    //@Test
    public void CheckAllBlockHash()throws Exception{
        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String preBlockHash=JSONObject.fromObject(store.GetBlockByHeight(blockHeight)).getJSONObject("Data").getJSONObject("header").getString("previousHash");
        String currentBlockHash="";
        for(int i= blockHeight-1;i>0;i--){

            log.info("Block height: " + i);
            currentBlockHash  = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONObject("header").getString("blockHash");
            assertEquals(currentBlockHash,preBlockHash);
            preBlockHash=JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONObject("header").getString("previousHash");

        }
    }

    @Test
    public void CheckUpgradeInteraceResp()throws Exception{
        SetVerRelease setVerRelease = new SetVerRelease();
        setVerRelease.test();//设置为发布版本进程
        subLedger="";//切换回主链测试
        //升级前执行回归测试

        txHashList = getAllTxHashData();
        log.info("hash no:" + txHashList.size());
        Map<String,String> beforeUpgrade = SaveResponseToHashMap(txHashList);

        //更新版本文件 远程调用
        SetVerLatest setVerLatest = new SetVerLatest();
        setVerLatest.test();//更新版本为最新版本
        log.info("before upgrade size : " + beforeUpgrade.size());
        log.info("before txlist size : " + txHashList.size());
        Map<String,String> afterUpgrade = SaveResponseToHashMap(txHashList);
        log.info("after upgrade size : " + afterUpgrade.size());
        log.info("after txlist size : " + txHashList.size());
        assertEquals(beforeUpgrade.size(),afterUpgrade.size());

        //比对升级前后hashresp Map内容是否完全一致

        ArrayList<String> diffRespList = new ArrayList<>();

        Iterator iter = beforeUpgrade.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            //log.info("Check TxHash: "+key);
            if(!beforeUpgrade.get(key).equals(afterUpgrade.get(key)))
            {
                diffRespList.add(key.toString());
                diffRespList.add(beforeUpgrade.get(key));
                diffRespList.add(afterUpgrade.get(key));
            }
        }
        String diffSaveFile = resourcePath + "diff.txt";
        File diff = new File(diffSaveFile);
        if(diff.exists()) diff.delete();//如果存在则先删除

        if(diffRespList.size() > 0) {
            for(int i = 0;i < diffRespList.size();i++){
                //log.info(diffRespList.get(i));
                FileOperation fileOperation = new FileOperation();
                fileOperation.appendToFile(diffRespList.get(i),diffSaveFile);
            }
            assertEquals("datas is not same",false,true);
        }

    }

    public Map<String,String> SaveResponseToHashMap(ArrayList<String> txList) throws Exception{
        Map<String,String> mapTXHashResp = new HashMap<>();
        mapTXHashResp.clear();
        txTypeSubType.clear();

        //存储所有的交易hash查询结果
        for(int k = 0; k < txList.size(); k++){
            String response = store.GetTxDetail(txList.get(k));
            //if(!response.contains("{")) continue;
            JSONObject jsonObject = JSONObject.fromObject(store.GetTxDetail(txList.get(k))).getJSONObject("Data").getJSONObject("Header");

            String txType = jsonObject.getString("Type");
            String txSubType = jsonObject.getString("SubType");

            txTypeSubType.put(txType + "_" + txSubType,"typecollection");//将所有类型和子类型全部保存到map中
            //仅取首个交易类型hash
            if(storeHash.isEmpty() || priStoreHash.isEmpty() || contractHash.isEmpty()) {
                //分别获取指定交易类型的交易hash 作为特定交易查看接口参数
                if (storeHash.isEmpty() && txType.equals("0") && txSubType.equals("0"))             storeHash = txList.get(k);
                else if (priStoreHash.isEmpty() && txType.equals("0") && txSubType.equals("1"))     priStoreHash = txList.get(k);
                else if (contractHash.isEmpty() && txType.equals("2") && txSubType.equals("30"))    contractHash = txList.get(k);
            }
            mapTXHashResp.put(txList.get(k),response);
        }

        Iterator iter = txTypeSubType.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object val = txTypeSubType.get(key);
            log.info(key.toString() + "with value:" + val.toString());
        }

        assertEquals(subTypeNo,txTypeSubType.size()); //确认所有1.0以上的子交易类型全部覆盖

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
        mapTXHashResp.put("get multi utxo balance",multiSign.Balance(IMPPUTIONADD,PRIKEY4, ""));
        mapTXHashResp.put("get solo utxo balance",soloSign.Balance(PRIKEY1,""));
        mapTXHashResp.put("get zero account balance",multiSign.QueryZero(""));
        mapTXHashResp.put("getapihealth",store.GetApiHealth());

        //获取合约交易search/byprefix  search/bykey
        String resp = store.GetTxDetail(contractHash);
        log.info("contract hash:" + contractHash);
        String msg =  JSONObject.fromObject(resp).getJSONObject("Data").getJSONObject("Contract").getString("Message");
        String contractName = msg.substring(msg.indexOf('[') + 1,msg.lastIndexOf('_'));
        mapTXHashResp.put("search/byprefix?prefix=Mobile&cn=" + contractName,contract.SearchByPrefix("Mobile",contractName));
        mapTXHashResp.put("search/bykey?key=Mobile001&cn=" + contractName,contract.SearchByKey("Mobile001",contractName));

        return mapTXHashResp;

    }


    public ArrayList<String> getAllTxHashData(){
        ArrayList<String> txHashList = new ArrayList<>();

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        for(int i= blockHeight;i>0;i--){
            JSONArray blockTxArr = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONArray("txs");
            for(int k = 0; k < blockTxArr.size(); k++){
                txHashList.add(blockTxArr.getString(k));
            }
        }
        return txHashList;
    }
}
