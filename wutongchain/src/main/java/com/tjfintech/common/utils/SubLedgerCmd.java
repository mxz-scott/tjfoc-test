package com.tjfintech.common.utils;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.ToolTPName;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SubLedgerCmd {
    MgToolCmd mgToolCmd=new MgToolCmd();
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();


    public String getSubChainInfoFromAll(String shellIP,String rpcPort,String chainName,String Keyword){
        Shell shell1 = new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd = " getledger ";
        String cmd1 = "cd "+ ToolPATH + ";./" + ToolTPName + mainCmd + " -p "+ rpcPort;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        stdout.remove(0);//删除版本号信息行
        stdout.remove(0);//删除=================行
        String response =StringUtils.join(stdout,"");

        JSONObject jsonObject = JSONObject.fromObject(response);
        String ledgerNo = jsonObject.getString("number");
        JSONArray ledMemList = jsonObject.getJSONArray("subLedgers");

        boolean bExist=false;
        String value="";
        for(int i =0;i<ledMemList.size();i++)
        {
            //log.info(ledMemList.getString(i));
            if(ledMemList.getString(i).contains("\"name\":\""+ chainName +"\""))
            {
                bExist=true;
                log.info("Get subChain:"+chainName+" OK");
                log.info(ledMemList.getString(i));
                value=JSONObject.fromObject(ledMemList.getString(i)).getString(Keyword);
                log.info("Keyword " + Keyword + ": " + value);
            }
        }
        return value;

    }

    public void sendTxToMainActiveChain(String glbChain01, String glbChain02, String data)throws Exception{
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response1 = store.CreateStore(data);

        subLedger=glbChain02;
        String response2 = store.CreateStore(data);

        //向主链发送交易
        subLedger="";
        String response3 = store.CreateStore(data);

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

    }
}
