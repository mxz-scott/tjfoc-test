package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.java.tar.gz.FileUtil.log;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CommonFunc {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    Token tokenModule= testBuilder.getToken();
    UtilsClass utilsClass=new UtilsClass();


    //-----------------------------------------------------------------------------------------------------------
    //token模块相关通用函数
    public  String tokenModule_IssueToken(String issueAddr,String collAddr,String amount){
        String issueToken = "tokenSo-"+ UtilsClass.Random(8);
        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + amount;
        log.info(comments);
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,amount,comments);
        return issueToken;
    }

    public  String tokenModule_TransferToken(String from,String to, String tokenType,String amount){
        String comments = from + "向" + to + " 转账token：" + tokenType + " 数量：" + amount;
        log.info(comments);
        List<Map> list = utilsClass.tokenConstructToken(to,tokenType,amount);
        return tokenModule.tokenTransfer(from,comments,list);
    }

    public  String tokenModule_TransferTokenList(String from, List<Map> tokenList){
        String comments = from + "一转多";
        log.info(comments);
        return tokenModule.tokenTransfer(from,comments,tokenList);
    }

    public String tokenModule_DestoryToken(String addr,String tokenType,String amount){
        String comments = addr + "销毁token：" + tokenType + " 数量：" + amount;
        log.info(comments);
        List<Map> list = utilsClass.tokenConstructToken(addr,tokenType,amount);
        return tokenModule.tokenDestoryByList(list,comments);
    }

    public String tokenModule_DestoryTokenByList2(List<Map> list){
        return tokenModule.tokenDestoryByList(list,"destory by token list");
    }

    public String tokenModule_DestoryTokenByTokenType(String tokenType){
        String comments = "destory tokentype " + tokenType;
        return tokenModule.tokenDestoryByTokenType(tokenType,comments);
    }

    //-----------------------------------------------------------------------------------------------------------
    //sdk相关函数封装
    public  String sdkMultiIssueToken(String issueAddr, String  amount, String ToAddr,
                                      ArrayList<String> priKeys, ArrayList<String> pwds){
        String tokenType = "MUCX-" + UtilsClass.Random(8);
        String data = issueAddr + "发行" + tokenType + " token，数量为：" + amount;
        log.info(data);
        String response = multiSign.issueToken(issueAddr,ToAddr,tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String tx = Tx1;
        for(int i=0;i<priKeys.size();i++) {
            String response2 = "";
            if(pwds.get(i).isEmpty())
                response2 = multiSign.Sign(tx, priKeys.get(i));
            else
                response2 = multiSign.Sign(tx,priKeys.get(i),pwds.get(i));
            assertEquals("200", JSONObject.fromObject(response2).getString("State"));
            if(JSONObject.fromObject(response2).getJSONObject("Data").getString("IsCompleted").contains("true")) break;
            else
                tx = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        }
        return tokenType;
    }

    public String sdkSoloIssueToken(String Prikey,String amount,String toAddr)throws Exception{
        BeforeCondition beforeCondition=new BeforeCondition();
        beforeCondition.collAddressTest();

        String tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String data = "Prikey issue token: " + tokenType + "*" + amount + "to " + toAddr;
        log.info(data);
        String isResult= soloSign.issueToken(Prikey,tokenType,amount,data,toAddr);
        assertEquals("200",JSONObject.fromObject(isResult).getString("State"));
        return tokenType;
    }
}
