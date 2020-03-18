package com.tjfintech.common.functionTest.upgrade;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetTestVersionLatest;
import com.tjfintech.common.utils.FileOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class UpgradeTestHistoryData {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Contract contract = testBuilder.getContract();
    ArrayList<String> txHashList = new ArrayList<>();
    Map<String, String> txTypeSubType = new HashMap<>();
    CommonFunc commonFunc = new CommonFunc();
    String storeHash = "";
    String priStoreHash = "";
    String contractHash = "";


    @BeforeClass
    public static void updateKeyPairs()throws  Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        if(IMPPUTIONADD.isEmpty())  beforeCondition.createAddresses();
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
//        以下三行单独执行时根据需要自行修改
//        SetTestVersionLatest setTestVersionLatest = new SetTestVersionLatest();
//        setTestVersionLatest.test();
//        subLedger="";//切换回主链测试
        //升级前执行回归测试

        txHashList = getAllTxHashData();
        log.info("hash no:" + txHashList.size());
        Map<String,String> beforeUpgrade = SaveResponseToHashMap(txHashList);

        //更新版本文件 远程调用
        SetTestVersionLatest setTestVersionLatest = new SetTestVersionLatest();
        setTestVersionLatest.test();//更新版本为最新版本

        if (!subLedger.isEmpty()) sleepAndSaveInfo(SLEEPTIME,"Latest version start waiting...");
        Map<String,String> afterUpgrade = SaveResponseToHashMap(txHashList);

        //比对升级前后hashresp Map内容是否完全一致

        ArrayList<String> diffRespList = new ArrayList<>();

        Iterator iter = beforeUpgrade.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
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
            assertEquals("data not same",false,true);
        }

    }

    //@Test
    public void test()throws Exception{
        txHashList = getAllTxHashData();
        //log.info("hash no:" + txHashList.size());
        Map<String,String> beforeUpgrade = SaveResponseToHashMap(txHashList);
        saveToFile(beforeUpgrade,"new.txt");
    }

    public void saveToFile(Map<String,String> mapHashResp,String fileName)throws Exception{
        FileOperation fileOperation = new FileOperation();
        String diffSaveFile = resourcePath + fileName;
        File saveFile = new File(diffSaveFile);
        if(saveFile.exists()) saveFile.delete();//如果存在则先删除
        Iterator iter = mapHashResp.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object val = mapHashResp.get(key);
            fileOperation.appendToFile(key.toString(),diffSaveFile);
            fileOperation.appendToFile(val.toString(),diffSaveFile);
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
            log.info("tx detail ");
            JSONObject jsonObject = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Header");

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

        //UTXO交易
        mapTXHashResp.put("get multi utxo balance",multiSign.Balance(IMPPUTIONADD,PRIKEY4, ""));
        mapTXHashResp.put("get solo utxo balance",soloSign.Balance(PRIKEY1,""));
        mapTXHashResp.put("get zero account balance",multiSign.QueryZero(""));
        mapTXHashResp.put("get all utxo account balance info",commonFunc.getUTXOAccountBalance().toString());

        //获取合约交易search/byprefix  search/bykey
        String resp = store.GetTxDetail(contractHash);
        log.info("contract hash:" + contractHash);
        String msg =  JSONObject.fromObject(resp).getJSONObject("Data").getJSONObject("Contract").getString("Message");
        String contractName = msg.substring(msg.indexOf('[') + 1,msg.lastIndexOf('_'));
        mapTXHashResp.put("search/byprefix?prefix=Mobile&cn=" + contractName,contract.SearchByPrefix("Mobile",contractName));
        mapTXHashResp.put("search/bykey?key=Mobile001&cn=" + contractName,contract.SearchByKey("Mobile001",contractName));

        return mapTXHashResp;

    }


    public ArrayList<String> getAllTxHashData()throws Exception{
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
}
