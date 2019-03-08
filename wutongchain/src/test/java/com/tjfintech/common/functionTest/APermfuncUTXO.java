package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.*;

//import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.MULITADD1;
//import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class APermfuncUTXO {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();

    String txMutli=MULITADD1;
    String issueMulTokenType="5555";
    String issueSolTokenType="6666";
    String multiAddr="";
    String issAmount="1000";
    String trsfAmount="10";


    String okCode="200";
    String okMsg="success";

    String errCode="404";
    String errMsg="does not found Permission";

    public String retAllow(String checkStr)throws Exception{
        String allow="*";
        if(checkStr.contains(okCode)) {
            allow = "1";
        }
        else if(checkStr.contains(errCode)&&checkStr.contains(errMsg))
        {
            allow="0";
        }
        return allow;
    }

    public String multiIssueToken(String mulIssAddr,String issueToken,String ToAddr)throws Exception{
        log.info("多签发行Token");
        String data = mulIssAddr+" 发行" + issueToken + "，数量为："+issAmount;
        String response="";
        if(ToAddr!="") {
            response = multiSign.issueToken(mulIssAddr, ToAddr, issueToken, issAmount, data);
        }
        else
            response = multiSign.issueToken(mulIssAddr, issueToken, issAmount, data);

        if(response.contains(okCode)) {
            txMutli = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        }

        return retAllow(response);
    }

    public String multiSign(String priKey,String passWd)throws Exception{
        log.info("多签签名");
        log.info(txMutli);
        String response = "";
        if (passWd=="") {
            response = multiSign.Sign(txMutli, priKey);
        }
        else
            response = multiSign.Sign(txMutli,priKey,passWd);
        if(response.contains("need more")) {
            txMutli = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        }
        return retAllow(response);
    }

    public String multiTransfer(String multiAddr,String priKey,String pwd,String toAddr,String transfToken) throws Exception {
        log.info("多签转账");
        String transferData = "归集地址向" + toAddr + "转账10个" + transfToken;
        //log.info(transferData);
        List<Map> list=utilsClass.constructToken(toAddr,transfToken,trsfAmount);
        log.info(transferData);
        String response="";
        if(pwd=="") {
            response = multiSign.Transfer(priKey, transferData, multiAddr, list);
        }
        else{
            response = multiSign.Transfer(priKey,pwd,transferData, multiAddr, list);
        }

        Thread.sleep(6000);


        //多签账户转账时需要进行多个签名，此时需要提取tx信息供下一个签名
        if(response.contains("need more")) {
            txMutli = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        }
        return retAllow(response);
    }

    public String recycle(String recyledAddr,String priKey,String pwd,String tokenType,String amount)throws  Exception{

        log.info("回收Token");
        String response="";
        //recyledAddr为空则表示回收单签账户，不为空则表示回收多签地址账户
        if(recyledAddr!="") {
            if(pwd!="")
            {
                response = multiSign.Recycle(recyledAddr, priKey,pwd ,tokenType, amount);
                log.info(pwd);
            }
            else
                response = multiSign.Recycle(recyledAddr, priKey, tokenType, amount);

        }
        else
            response = multiSign.Recycle( priKey, tokenType, amount);

        return retAllow(response);
    }

    public String getZeroAddrBalance(String tokenType)throws Exception  {
        log.info("查询回收账户余额");
        String response = multiSign.QueryZero(tokenType);
        return retAllow(response);
    }

    public String multiPostBalance(String multiAddr,String priKey,String tokenType)throws Exception {
        log.info("查询多签账户余额");
        String response = multiSign.Balance(multiAddr, priKey, tokenType);
        return retAllow(response);
    }
    //不受权限管控
    public String multiGenAddr(int M,String...str)throws Exception  {

        Map<String, Object> map = new HashMap<>();
        for(int i=0;i<str.length;i++) {
            map.put(String.valueOf(i+1), str[i]);
        }
        String response =multiSign.genMultiAddress(M, map);
        multiAddr = JSONObject.fromObject(response).getJSONObject("Data").getString("Address");
        //return multiAddr;
        return retAllow(response);

    }

    //---------------------------------------------------------------------------------------------------//
    //特殊操作例如冻结token、解除token冻结、上链归集地址、注销归集地址、验证私钥密码匹配性
    public String utxoFreeze(String priKey,String tokenType)throws Exception {
        log.info("冻结token");
        String response = multiSign.freezeToken(priKey, tokenType);
        return retAllow(response);
    }

    public String utxoRecoverToken(String priKey,String tokenType)throws Exception {
        log.info("解除被冻结token");
        String response = multiSign.recoverFrozenToken(priKey, tokenType);
        return retAllow(response);
    }

    public String addCollAddr(String priKey,String...collAddr)throws Exception  {
        log.info("向链上注册归集地址");
        String response = multiSign.collAddress(priKey,collAddr);
        return retAllow(response);
    }

    public String delCollAddr(String priKey,String...collAddr)throws Exception  {
        log.info("注销链上的归集地址");
        String response = multiSign.delCollAddress(priKey,collAddr);
        return retAllow(response);
    }

    public String validateKey(String priKey,String pwd)throws Exception  {
        log.info("检查私钥与密码的匹配性");
        String response = multiSign.CheckPriKey(priKey,pwd);
        return retAllow(response);
    }
    //------------------------------------------------------------------------------------//
    //the follow functions are defined for solo UTXO

    public String soloGenAddr(String pubKey)throws Exception  {
        log.info("生成单签地址");
        String response = soloSign.genAddress(pubKey);
        //return JSONObject.fromObject(response).getJSONObject("Data").getString("Address");
        return retAllow(response);
    }

    public String soloIssueToken(String priKey,String token,String toAddr)throws Exception  {
        log.info("单签发行token");
        String response= soloSign.issueToken(priKey,token,issAmount,"发行token",toAddr);
        return retAllow(response);
    }

    public String soloTransfer(String priKey,String toAddr,String token)throws Exception  {
        log.info("单签转账");
        List<Map> listModel = soloSign.constructToken(toAddr,token,trsfAmount);
        String response= soloSign.Transfer(listModel,priKey,"单签转账");
        Thread.sleep(3000);
        return retAllow(response);
    }
    public String soloBalance(String priKey,String tokenType)throws Exception  {
        log.info("单签余额查询");
        String response= soloSign.Balance( priKey, tokenType);
        return retAllow(response);
    }


}
