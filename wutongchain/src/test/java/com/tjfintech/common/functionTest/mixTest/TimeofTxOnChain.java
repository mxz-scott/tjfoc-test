package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetAppChain;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
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

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    FileOperation fileOperation = new FileOperation();


    String Data = "";
    String response = "";
    String txHash = "";
    FileOperation fileOper = new FileOperation();
    WVMContractTest wvmCt = new WVMContractTest();
    String ctHash = "123";
    String wvmFile = "wvm";
    String orgName = "TestExample";//样例合约中的内部合约名
    String ctName = "";
    List ctHashList = new ArrayList();


    public void searchTxOnChain(String type)  throws Exception{
        switch (type) {
            case "1": //存证
                Data = "\"test\":\"json" + UtilsClass.Random(4) + "\"";
                txHash = JSONObject.fromObject(store.CreateStore(Data)).getString("data");
                break;

            case "2"://合约安装
                ctName = "ontime_" + sdf.format(dt) + RandomUtils.nextInt(100000);
                // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
                // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
                fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

                //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
                String response1 = wvmCt.wvmInstallTest(wvmFile + "_temp.txt","");
                txHash = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
                ctHashList.add(ctHash);
                break;

            case "3"://合约调用
                String response2 = wvmCt.invokeNew(ctHash,"initAccount","AA","100");//初始化账户A 账户余额50
                txHash = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
                break;
            case "4"://合约销毁
                String response3 = wvmCt.wvmDestroyTest(ctHash);//初始化账户A 账户余额50
                txHash = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");
                break;
        }
        long nowTime = new Date().getTime();
        GetTxDetail(nowTime,txHash);
    }


    public void GetTxDetail(long time ,String hash) throws Exception{

        String response2= store.GetTxDetail(hash);
//        String response2= store.GetInlocal(hash);
        if (response2.indexOf("200")!=-1){
            long nowTime = new Date().getTime();
            fileOperation.appendToFile(hash + " on chain : " + (nowTime -time) + "ms",testResultPath + "onchain.txt");
//               log.info(nowTime -time + "ms tx on chain");
        }else{
            Thread.sleep(50);
            GetTxDetail(time,hash);
        }


    }

    public void GetLocal(long time ,String hash) throws Exception{
        String response2= store.GetInlocal(hash);
//        String response2= store.GetInlocal(hash);
        if (response2.indexOf("200")!=-1){
            long nowTime = new Date().getTime();
            fileOperation.appendToFile(hash + " sync db : " + (nowTime -time) + "ms",testResultPath + "storesyncdb.txt");

        }else{
            Thread.sleep(50);
            GetLocal(time,hash);
        }
    }


    @Test
    public  void OnChainTimeTest()throws Exception{
        SetAppChain setAppChain = new SetAppChain();
        setAppChain.createSubledger();

        for(int i=0;i<1000;i++){
            log.info("++++++++++++++++++++++++++++++++++++++++++++++++store " + i);
            searchTxOnChain("1");
        }

        for(int i=0;i<1000;i++){
            log.info("++++++++++++++++++++++++++++++++++++++++++++++++wvm install " + i);
            searchTxOnChain("2");
        }

        for(int i=0;i<1000;i++){
            log.info("++++++++++++++++++++++++++++++++++++++++++++++++wvm destory" + i);
            ctHash = ctHashList.get(i).toString();
            searchTxOnChain("4");
        }


        //合约调用
        String ctName = "ontime_" + sdf.format(dt) + RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmCt.wvmInstallTest(wvmFile + "_temp.txt","");
        ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
        sleepAndSaveInfo(2000);

        for(int i=0;i<1000;i++){
            log.info("++++++++++++++++++++++++++++++++++++++++++++++++wvm " + i);
            searchTxOnChain("3");
        }

    }

//    @Test
    public void checkBlockHash()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.setPermission999();
        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String preBlockHash=JSONObject.fromObject(store.GetBlockByHeight(blockHeight)).getJSONObject("data").getJSONObject("header").getString("previousHash");
        String currentBlockHash="";
        for(int i= blockHeight-1;i>0;i--){

            log.info("Block height: " + i);
            currentBlockHash  = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("data").getJSONObject("header").getString("blockHash");
            assertEquals(currentBlockHash,preBlockHash);
            preBlockHash=JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("data").getJSONObject("header").getString("previousHash");

        }
    }

}
