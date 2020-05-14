package com.tjfintech.common.functionTest.upgrade;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.*;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class UpgradeTestHistoryData_TokenApi {

    public   final static int   SHORTSLEEPTIME=3*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Contract contract = testBuilder.getContract();
    Token tokenModule = testBuilder.getToken();

    ArrayList<String> txHashList = new ArrayList<>();
    Map<String, String> txTypeSubType = new HashMap<>();
    String storeHash = "";
    String priStoreHash = "";
    String contractHash = "";





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

        //2.4.1版本升级到2.4.2版本需要等待一段时间 因数据库重新建表，没有和之前的表名一致
        sleepAndSaveInfo(60*000);

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
        log.info("upgrade check complete!!");
    }

//    //@Test
//    public void test()throws Exception{
//        txHashList = getAllTxHashData();
//        //log.info("hash no:" + txHashList.size());
//        Map<String,String> beforeUpgrade = SaveResponseToHashMap(txHashList);
//        saveToFile(beforeUpgrade,"new.txt");
//    }
//
//    public void saveToFile(Map<String,String> mapHashResp,String fileName)throws Exception{
//        FileOperation fileOperation = new FileOperation();
//        String diffSaveFile = resourcePath + fileName;
//        File saveFile = new File(diffSaveFile);
//        if(saveFile.exists()) saveFile.delete();//如果存在则先删除
//        Iterator iter = mapHashResp.keySet().iterator();
//        while (iter.hasNext()) {
//            Object key = iter.next();
//            Object val = mapHashResp.get(key);
//            fileOperation.appendToFile(key.toString(),diffSaveFile);
//            fileOperation.appendToFile(val.toString(),diffSaveFile);
//        }
//
//    }

    public Map<String,String> SaveResponseToHashMap(ArrayList<String> txList) throws Exception{
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


    public ArrayList<String> getAllTxHashData()throws Exception{
        SDKADD = rSDKADD;//通过sdk获取所有交易hash
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
