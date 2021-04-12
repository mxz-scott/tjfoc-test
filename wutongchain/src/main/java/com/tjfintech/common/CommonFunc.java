package com.tjfintech.common;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.MysqlOperation;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.utils.FileOperation.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
@Slf4j
public class CommonFunc {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    Token tokenModule= testBuilder.getToken();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass=new UtilsClass();
    MgToolCmd mgToolCmd = new MgToolCmd();
    //获取所有地址账户与私钥密码信息
    JSONObject jsonObjectAddrPri;


    //-----------------------------------------------------------------------------------------------------------
    //获取交易hash函数 此处兼容


    //-----------------------------------------------------------------------------------------------------------
    public void setPermAndCheckResp(String peerIP,String peerPort,String remoteId,String permList)throws Exception{
        String toolPath = "cd " + ToolPATH + ";";
        String ledger = (subLedger != "") ? " -c " + subLedger:"";
        String exeCmd = "./" + ToolTPName + " permission " + ledger;
        String permSetResp1 = shExeAndReturn(peerIP,toolPath + exeCmd + " -p " + peerPort + " -d " + remoteId + " -m " + permList);
        assertEquals(true,permSetResp1.contains("send transaction success"));
        sleepAndSaveInfo(SLEEPTIME);
    }




    //-----------------------------------------------------------------------------------------------------------
    //token模块相关通用函数
    public  String tokenModule_IssueToken(String issueAddr,String collAddr,String amount){
        String issueToken = "tokenSoMU_"+ UtilsClass.Random(8);
        log.info("发行地址： " + issueAddr);
        log.info("归集地址： " + collAddr);
        String comments = collAddr + " 发行token：" + issueToken + " 数量：" + amount;
        log.info(comments);
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,amount,comments);
        return issueToken;
    }

    public  String tokenModule_TransferToken(String from,String to, String tokenType,String amount){
        log.info("转出地址 ：" + from);
        log.info("转入地址 ：" + to);
        String comments = "转账token：" + tokenType + " 数量：" + amount;
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

    /**
     * 通用余额查询函数 兼容单签、多签，不需传入私钥密码仅需传地址私钥密码的JSONObject、查询账户及tokentype
     * @param queryAccount 查询的账户地址
     * @param queryTokenType 查询tokentype
     * @return
     */
    public String  GetBalance(String queryAccount,String queryTokenType){
        String balanceAmount = "";
        String response = "";
        //单签多签使用同一个接口查询账户余额
        response = multiSign.BalanceByAddr(queryAccount,  queryTokenType);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        balanceAmount = JSONObject.fromObject(response).getJSONObject("data").getString("total");
        return balanceAmount;
    }

    /**
     * 通用转账接口 兼容单签、多签，不需传入私钥密码仅需传地址私钥密码的组合对，账户，及转账信息即可
     * @param trfAccount 转出账户地址
     * @param list 转出list 传入list时，需要根据单签还是多签地址传入单签list组合 还是多签list组合
     */
    public void  sdkTransfer(String trfAccount,List<Map> list){
        String response = "";
        jsonObjectAddrPri = mapAddrInfo();

        JSONObject jsonObjectAddrInfo = JSONObject.fromObject(jsonObjectAddrPri.getString(trfAccount));
        String prikey = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(0)).getString("priKey");
        String pwd = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(0)).getString("pwd");
        String sign = jsonObjectAddrInfo.getString("sign");

        //判断单签还是多签
        if(sign.equals("0")){
            //单签账户查询
            if(pwd.isEmpty()) {
                //不带密码转账
                response = soloSign.Transfer(list,prikey,"transfer solo token tpye");
            }
            else
            {
                //带密码私钥转账
                log.info("当前不支持单签账户带密码转账");
                assertEquals(true,false);
            }
        }
        else{
            //多签转账
            if(pwd.isEmpty()) {
                response = multiSign.Transfer(prikey, "升级后转账", trfAccount, list);
            }else{
                response = multiSign.Transfer(prikey, "", "升级后转账", trfAccount, list);
            }
            assertThat(response, CoreMatchers.containsString("200"));
            signUTXOLess1Time(response,jsonObjectAddrInfo);
        }
    }

    /**
     * 通用回收封装函数，兼容单签和多签，不需传入私钥密码仅需传地址私钥密码的JSONObject，账户、回收tokentype、回收数量
     * @param recAccount  回收账户地址
     * @param recTokenType  回收tokentype
     * @param recAmount  回收数量
     */
    public void  sdkRecycle(String recAccount,String recTokenType,String recAmount){
        String response = "";
        jsonObjectAddrPri = mapAddrInfo();

        JSONObject jsonObjectAddrInfo = JSONObject.fromObject(jsonObjectAddrPri.getString(recAccount));
        String prikey = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(0)).getString("priKey");
        String pwd = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(0)).getString("pwd");
        String sign = jsonObjectAddrInfo.getString("sign");

        //判断单签还是多签
        if(sign.equals("0")){
            //单签账户查询
            if(pwd.isEmpty()) {
                //不带密码回收
                response = multiSign.Recycle("", prikey, recTokenType, recAmount);
            }
            else
            {
                //带密码私钥回收
                response = multiSign.Recycle("", prikey,pwd, recTokenType, recAmount);
            }
        }
        else{
            //多签回收
            if(pwd.isEmpty()) {
                response = multiSign.Recycle(recAccount, prikey, recTokenType, recAmount);
            }else{
                response = multiSign.Recycle(recAccount, prikey,pwd, recTokenType, recAmount);
            }
            assertThat(response, CoreMatchers.containsString("200"));
            signUTXOLess1Time(response,jsonObjectAddrInfo);
        }
    }

    public void  sdkIssue(String issueAccount,String collAccount,String issueToken,String issueAmount){
        String response = "";
        jsonObjectAddrPri = mapAddrInfo();

        JSONObject jsonObjectAddrInfo = JSONObject.fromObject(jsonObjectAddrPri.getString(issueAccount));
        String prikey = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(0)).getString("priKey");
        String pwd = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(0)).getString("pwd");
        String sign = jsonObjectAddrInfo.getString("sign");
        String data = issueAccount + " issue " + issueToken + " * " + issueAmount;

        //判断单签还是多签
        if(sign.equals("0")){
            //单签账户发行
            if(pwd.isEmpty()) {
                //不带密码转账
                response = soloSign.issueToken(prikey,issueToken,issueAmount,data,"");
            }
            else {
                //带密码私钥转账
                log.info("当前不支持单签账户带密码发行");
                assertEquals(true,false);
            }
        }
        else{
            //多签转账
            response = multiSign.issueToken(issueAccount,collAccount,issueToken,issueAmount,prikey,pwd,data);
            assertThat(response, CoreMatchers.containsString("200"));
            signUTXOLess1Time(response,jsonObjectAddrInfo);
        }
    }


    public void signUTXOLess1Time(String response,JSONObject jsonObjectAddrInfo){
        String tx = "";
        String prikey = "";
        String pwd = "";
        if(response.contains("need more sign")) {
            log.info("need more sign");
            tx = JSONObject.fromObject(response).getJSONObject("data").getString("tx");
        }
        if(!tx.isEmpty()) {
            //第一组私钥密码在转账请求时使用签名
            for (int i = 1; i < jsonObjectAddrInfo.getJSONArray("keyList").size(); i++) {
                prikey = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(i)).getString("priKey");
                pwd = JSONObject.fromObject(jsonObjectAddrInfo.getJSONArray("keyList").get(i)).getString("pwd");
                String response2 = "";
                if (pwd.isEmpty())
                    response2 = multiSign.Sign(tx, prikey);
                else
                    response2 = multiSign.Sign(tx, prikey, pwd);
                assertEquals("200", JSONObject.fromObject(response2).getString("state"));
                if (JSONObject.fromObject(response2).getJSONObject("data").getString("isCompleted").contains("true"))
                    break;
                else
                    tx = JSONObject.fromObject(response2).getJSONObject("data").getString("tx");
            }
        }
    }


    public ArrayList<String> collAddrList(){
        ArrayList<String> addrList = new ArrayList<>();
        addrList.add(ADDRESS1);    addrList.add(ADDRESS2);    addrList.add(ADDRESS3);
        addrList.add(ADDRESS4);    addrList.add(ADDRESS5);    addrList.add(ADDRESS6);
        addrList.add(ADDRESS7);

        addrList.add(IMPPUTIONADD);
        addrList.add(MULITADD1);    addrList.add(MULITADD2);    addrList.add(MULITADD3);
        addrList.add(MULITADD4);    addrList.add(MULITADD5);    addrList.add(MULITADD6);
        addrList.add(MULITADD7);

        return addrList;
    }
    /***
     * 该函数的作用是将SDK涉及到的单签及多签地址 与签名次数、私钥（带密码）的私钥关联起来
     * @return
     */
    public JSONObject mapAddrInfo(){
        assertEquals(false,IMPPUTIONADD.isEmpty());

        Map<String,Object> mapAccInfo =  new HashMap<>();

        CommonFunc commonFunc = new CommonFunc();
        commonFunc.mapSoloAddrWithPri(mapAccInfo,ADDRESS1,PRIKEY1,"");
        commonFunc.mapSoloAddrWithPri(mapAccInfo,ADDRESS2,PRIKEY2,"");
        commonFunc.mapSoloAddrWithPri(mapAccInfo,ADDRESS3,PRIKEY3,"");
        commonFunc.mapSoloAddrWithPri(mapAccInfo,ADDRESS4,PRIKEY4,"");
        commonFunc.mapSoloAddrWithPri(mapAccInfo,ADDRESS5,PRIKEY5,"");
        commonFunc.mapSoloAddrWithPri(mapAccInfo,ADDRESS6,PRIKEY6,PWD6);
        commonFunc.mapSoloAddrWithPri(mapAccInfo,ADDRESS7,PRIKEY7,PWD7);

        String signNo = "1";
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY4,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY5,""));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,IMPPUTIONADD,signNo,jsonArray);

        jsonArray.clear();
        signNo = "3";
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY1,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY2,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY3,""));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,MULITADD1,signNo,jsonArray);

        jsonArray.clear();
        signNo = "3";
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY1,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY2,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY6,PWD6));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,MULITADD2,signNo,jsonArray);

        jsonArray.clear();
        signNo = "3";
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY1,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY6,PWD6));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY7,PWD7));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,MULITADD3,signNo,jsonArray);

        jsonArray.clear();
        signNo = "1";
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY1,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY2,""));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,MULITADD4,signNo,jsonArray);

        jsonArray.clear();
        signNo = "1";
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY1,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY3,""));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,MULITADD5,signNo,jsonArray);

        jsonArray.clear();
        signNo = "1";
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY3,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY4,""));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,MULITADD6,signNo,jsonArray);

        jsonArray.clear();
        signNo = "1";
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY1,""));
        jsonArray.add(commonFunc.constructJSONPriPwd(PRIKEY6,PWD6));
        commonFunc.mapMultiAddrWithPri(mapAccInfo,MULITADD7,signNo,jsonArray);

        System.out.println(JSONObject.fromObject(mapAccInfo).toString());
        return JSONObject.fromObject(mapAccInfo);

    }

    /**
     * 作用是将单签地址相关信息（签名次数、私钥及密码）组合塞进map中
     * @param mapAccInfo 即将被塞进的map
     * @param Account 单签地址
     * @param priKey 单签地址对应的私钥
     * @param pwd 单签地址私钥对应的密码 可以为空，为空则表示为不带密码的私钥
     */
    public void mapSoloAddrWithPri(Map<String,Object> mapAccInfo ,String Account,String priKey,String pwd){
        Map<String,Object> mapOneAccInfo =  new HashMap<>();

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(constructJSONPriPwd(priKey,pwd));

        mapOneAccInfo.put("keyList",jsonArray);
        mapOneAccInfo.put("sign","0");
        JSONObject jsonObject1 = JSONObject.fromObject(mapOneAccInfo);

        mapAccInfo.put(Account,jsonObject1);
    }

    /**
     * 作用是将多签地址相关信息（签名次数、私钥及密码）组合塞进map中
     * @param mapAccInfo 即将被塞进的map
     * @param Account 多签地址
     * @param sign 多签地址签名次数
     * @param jsonPriPwdArray 多签地址私钥密码数组
     */
    public void mapMultiAddrWithPri(Map<String,Object> mapAccInfo ,String Account,String sign,JSONArray jsonPriPwdArray){
        Map<String,Object> mapOneAccInfo =  new HashMap<>();

        mapOneAccInfo.put("keyList",jsonPriPwdArray);
        mapOneAccInfo.put("sign",sign);
        JSONObject jsonObject1 = JSONObject.fromObject(mapOneAccInfo);

        mapAccInfo.put(Account,jsonObject1);
    }

    /**
     * 将私钥和密码放进map中
     * @param priKey 私钥
     * @param pwd 密码 可以不带 不带则表示该私钥不带密码 即未加密
     * @return
     */
    public JSONObject constructJSONPriPwd(String priKey, String pwd){
        Map<String,Object> mapPrikeyPwd =  new HashMap<>();
        mapPrikeyPwd.put("priKey",priKey);
        if(!pwd.isEmpty()) mapPrikeyPwd.put("pwd",pwd);
        else mapPrikeyPwd.put("pwd","");
        return JSONObject.fromObject(mapPrikeyPwd);
    }

    /**
     * SDK utxo发行封装函数
     * @param issueAddr  发行地址
     * @param amount 发行数量
     * @param ToAddr 发向地址（归集地址）
     * @param priKeys 发行时进行签名使用的私钥数组
     * @param pwds 发行私钥对应的密码数组，与私钥数量一致，如果私钥不带密码则pwds中私钥对应的列传入空
     * @return 返回发行token
     */
    public  String sdkMultiIssueToken(String issueAddr, String  amount, String ToAddr,
                                      ArrayList<String> priKeys, ArrayList<String> pwds){
        String tokenType = "MUCX_" + UtilsClass.Random(8);
        String data = issueAddr + "发行" + tokenType + " token，数量为：" + amount;
        System.out.print(data);
        String response = multiSign.issueToken(issueAddr,ToAddr,tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("data").getString("tx");
        System.out.print("第一次签名");
        String tx = Tx1;
        for(int i=0;i<priKeys.size();i++) {
            String response2 = "";
            if(pwds.get(i).isEmpty())
                response2 = multiSign.Sign(tx, priKeys.get(i));
            else
                response2 = multiSign.Sign(tx,priKeys.get(i),pwds.get(i));
            assertEquals("200", JSONObject.fromObject(response2).getString("state"));
            if(JSONObject.fromObject(response2).getJSONObject("data").getString("isCompleted").contains("true")) break;
            else
                tx = JSONObject.fromObject(response2).getJSONObject("data").getString("tx");
        }
        return tokenType;
    }




    /**
     * 查询各个单签多签账户余额
     * @throws Exception
     */

    public Map<String, Object>  getUTXOAccountBalance() {
        CommonFunc commonFunc = new CommonFunc();
        String tokenColl = multiSign.tokenstate("");
        JSONArray tokenArr = JSONObject.fromObject(tokenColl).getJSONArray("data");
        ArrayList<String> tokenList = new ArrayList<>();

        for(int i=0;i< tokenArr.size();i++){
            String tt = tokenArr.getJSONObject(i).getString("TokenType");
            tokenList.add(tt);
        }
        Map<String, Object> mapAccToken = new HashMap<>();

        mapAccToken.put(ADDRESS1, commonFunc.getSDKUTXOAccTotalToken("",PRIKEY1,"",tokenList));
        mapAccToken.put(ADDRESS2, commonFunc.getSDKUTXOAccTotalToken("",PRIKEY2,"",tokenList));
        mapAccToken.put(ADDRESS3, commonFunc.getSDKUTXOAccTotalToken("",PRIKEY3,"",tokenList));
        mapAccToken.put(ADDRESS4, commonFunc.getSDKUTXOAccTotalToken("",PRIKEY4,"",tokenList));
        mapAccToken.put(ADDRESS5, commonFunc.getSDKUTXOAccTotalToken("",PRIKEY5,"",tokenList));
        mapAccToken.put(ADDRESS6, commonFunc.getSDKUTXOAccTotalToken("",PRIKEY6,PWD6,tokenList));
        mapAccToken.put(ADDRESS7, commonFunc.getSDKUTXOAccTotalToken("",PRIKEY7,PWD7,tokenList));

        mapAccToken.put(IMPPUTIONADD, commonFunc.getSDKUTXOAccTotalToken(IMPPUTIONADD,PRIKEY4,"",tokenList));
        mapAccToken.put(MULITADD1, commonFunc.getSDKUTXOAccTotalToken(MULITADD1,PRIKEY1,"",tokenList));
        mapAccToken.put(MULITADD2, commonFunc.getSDKUTXOAccTotalToken(MULITADD2,PRIKEY1,"",tokenList));
        mapAccToken.put(MULITADD3, commonFunc.getSDKUTXOAccTotalToken(MULITADD3,PRIKEY1,"",tokenList));
        mapAccToken.put(MULITADD4, commonFunc.getSDKUTXOAccTotalToken(MULITADD4,PRIKEY1,"",tokenList));
        mapAccToken.put(MULITADD5, commonFunc.getSDKUTXOAccTotalToken(MULITADD5,PRIKEY3,"",tokenList));
        mapAccToken.put(MULITADD6, commonFunc.getSDKUTXOAccTotalToken(MULITADD6,PRIKEY3,"",tokenList));
        mapAccToken.put(MULITADD7, commonFunc.getSDKUTXOAccTotalToken(MULITADD7,PRIKEY1,"",tokenList));


        System.out.print("Collection amount: " + mapAccToken.size() + "\n");
        System.out.print(mapAccToken.toString());

        return mapAccToken;

    }

    public ArrayList<String> getSDKUTXOAccTotalToken(String account, String accountPrikey, String pwd, ArrayList<String> tokenList){
        ArrayList<String> tokenAmountList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        for(String token:tokenList){
            String tokenTotal = "";
            tokenTotal = JSONObject.fromObject(
                    multiSign.BalanceByAddr(account, token)).getJSONObject("data").getString("total");
            if(!tokenTotal.equals("0"))
            {
                map.put("\"tokenType\"","\"" + token + "\"");
                map.put("\"value\"","\"" + tokenTotal + "\"");
                tokenAmountList.add(map.toString());
                map.clear();
            }
        }
        System.out.print(account + " with token account :" + tokenAmountList.toString() + "\n");
        return tokenAmountList;
    }

    //-----------------------------------------------------------------------------------------------------------
    //结果确认辅助函数 回收结果list map for 回收bytoken
    /***
     * 返回信息中的结果确认
     * @param address
     * @param amount
     * @return
     */
    public List<Map> ConstructDesByTokenRespList(String address, String amount){
        List<Map> tokenList = new ArrayList<>();
        Map<String,Object> amountMap = new LinkedHashMap<>();
        amountMap.put("\"address\"","\"" + address + "\"");
        amountMap.put("\"amount\"","\"" + amount + "\"");

        tokenList.add(amountMap);
        return tokenList;
    }
    /***
     * 返回信息中的结果确认
     * @param address
     * @param amount
     * @param list
     * @return
     */
    public List<Map> ConstructDesByTokenRespList(String address, String amount, List<Map> list){
        List<Map> tokenList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object> amountMap = new LinkedHashMap<String, Object>();
        amountMap.put("\"address\"","\"" + address + "\"");
        amountMap.put("\"amount\"","\"" + amount + "\"");


        tokenList.add(amountMap);
        return tokenList;
    }


    /***
     * 返回信息中的结果确认
     * @param from
     * @param to
     * @param tokenType
     * @param amount
     * @return
     */
    public List<Map> constructUTXOTxDetailList(String from, String to, String tokenType, String amount){
        List<Map> tokenList = new ArrayList<>();
        Map<String,Object> amountMap = new LinkedHashMap<>();

        amountMap.put("\"From\"","\"" + from + "\"");
        amountMap.put("\"To\"","\"" + to + "\"");
        amountMap.put("\"TokenType\"","\"" + tokenType + "\"");
        amountMap.put("\"Amount\"","\"" + amount + "\"");

        tokenList.add(amountMap);
        return tokenList;
    }
    /***
     * 返回信息中的结果确认
     * @param from
     * @param to
     * @param tokenType
     * @param amount
     * @param list
     * @return
     */
    public static List<Map> constructUTXOTxDetailList(String from, String to, String tokenType, String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new LinkedHashMap<>();

        amountMap.put("\"From\"","\"" + from + "\"");
        amountMap.put("\"To\"","\"" + to + "\"");
        amountMap.put("\"TokenType\"","\"" + tokenType + "\"");
        amountMap.put("\"Amount\"","\"" + amount + "\"");

        tokenList.add(amountMap);
        return tokenList;
    }


    public boolean checkListArray(List<Map> list, JSONArray jsonArray){
        boolean bResult = true;

        for(int i= 0;i<list.size();i++){
            String checkStr = list.get(i).toString().replaceAll("=",":").replaceAll(" ","");
            boolean bMatch = false;
            System.out.print("=================" + checkStr);
            System.out.print("+++++++++++++++++" + jsonArray.toString());
            for(int j = 0;j < jsonArray.size(); j++){
                if(checkStr.equals(jsonArray.get(j).toString())){
                    bMatch = true;
                    jsonArray.remove(j);
                    break;
                }
            }
            bResult = bResult && bMatch;
            assertEquals(true,bMatch);
        }
        System.out.print("matching complete.............");
        return bResult;
    }

    //----------------------------------------------------------------------------------------------------------//
    //以下为对配置文件修改 通过shell脚本方式
    //此部分读写不支持重复section的内容 例如 peer config.toml中会有多个 [[Members.Peers]]
    //此部分读写不支持重复section的内容 例如 sdk conf/config.toml中会有多个 [[Peers]]

    //读取sdk配置文件中指定配置项信息
    public boolean getSDKWalletEnabled(){
        boolean bEnabled = true;
        String resp = getSDKConfigValueByShell(utilsClass.getIPFromStr(SDKADD),"Wallet","Enabled");
        if(resp.contains("true")) bEnabled = true;
        else if(resp.contains("false")) bEnabled = false;
        return bEnabled;
    }


    public void setSDKTLSCertECDSA(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/ecdsa\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/ecdsa\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/ecdsa\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./ecdsa/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./ecdsa/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./ecdsa/key.pem"));
    }

    public void setSDKTLSCertExpired(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/expired\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/expired\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/expired\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./expired/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./expired/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./expired/key.pem"));
    }

    public void setSDKTLSCertDiffCa(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/diffca\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/diffca\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/diffca\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./diffca/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./diffca/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./diffca/key.pem"));
    }

    public void setSDKTLSCertDismatch(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/dismatch\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/dismatch\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/dismatch\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./dismatch/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./dismatch/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./dismatch/key.pem"));
    }

    public void setSDKCryptKeyType(String SDKIP,String keytype)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","KeyType","\"\\\"" + keytype + "\"\\\"");
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","KeyType").trim().contains(keytype));
    }

    public void setSDKCryptHashType(String SDKIP,String hashtype)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","HashType","\"\\\""+ hashtype + "\"\\\"");
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","HashType").trim().contains(hashtype));

    }

    public void setSDKWalletEnabled(String SDKIP, String flag)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Enabled",flag);
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Enabled").trim().contains(flag));
    }

    public void setSDKWalletAddrDBMysql(String SDKIP,String mysqlDBAddr)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mysqlDBAddr);

        String checkMyqlDBAddr = mysqlDBAddr.replaceAll("\"","").replaceAll("\\\\","");
//        System.out.print(checkMyqlDBAddr);

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMyqlDBAddr));

    }

    public void setSDKWalletDBMysqlAddrDBMongo(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mysqlDBAddr);
        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mongodb\"\\\"");
        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mongoDBAddr);

        String checkMongoDBAddr = mongoDBAddr.replaceAll("\"","").replaceAll("\\\\","");
        String checkMyqlDBAddr = mysqlDBAddr.replaceAll("\"","").replaceAll("\\\\","");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMyqlDBAddr));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mongodb"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMongoDBAddr));
    }

    public void setSDKWalletDBMongoAddrDBMysql(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mongodb\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mongoDBAddr);
        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mysqlDBAddr);

        String checkMongoDBAddr = mongoDBAddr.replaceAll("\"","").replaceAll("\\\\","");
        String checkMyqlDBAddr = mysqlDBAddr.replaceAll("\"","").replaceAll("\\\\","");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mongodb"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMongoDBAddr));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMyqlDBAddr));
    }

    public void setSDKWalletAddrDBMongo(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mongodb\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mongoDBAddr);
//        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mongodb\"\\\"");
//        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mongoDBAddr);

        String checkMongoDBAddr = mongoDBAddr.replaceAll("\"","").replaceAll("\\\\","");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mongodb"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMongoDBAddr));
//        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mongodb"));
//        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMongoDBAddr));
    }


    public void setSDKOnePeer(String SDKIP,String PeerIPPort,String TLSEnabled,String PeerTLSServerName){
        //获取第一个[[Peer]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(SDKIP,"grep -n Peer "+ SDKConfigPath + " | cut -d \":\" -f 1 |sed -n '1p'");
        //先删除conf/config.toml文件中的所有Peers
        shExeAndReturn(SDKIP,"sed -i '/Peer/d' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '/Address/d' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '/TLSEnabled/d' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '/TLSServerName/d' " + SDKConfigPath);

        shExeAndReturn(SDKIP,"sed -i '" + Integer.parseInt(lineNo) + "i[[Peer]]' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "iAddress = \"" + PeerIPPort + "\"' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "iTLSEnabled = " + TLSEnabled + "' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+3) + "iTLSServerName = \"" + PeerTLSServerName  + "\"' " +  SDKConfigPath);
    }

    public void setSDKApiOneLedger(String SDKIP,String ledger,String urls){
        //获取第一个[[ledgers]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(SDKIP,"grep -n ledgers "+ TokenApiConfigPath + " | cut -d \":\" -f 1 |sed -n '1p'");
        //先删除conf/config_api.toml文件中的所有ledgers
        if (lineNo!= ""){
            shExeAndReturn(SDKIP,"sed -i '/ledgers/d' " + TokenApiConfigPath);
            shExeAndReturn(SDKIP,"sed -i '/Ledger/d' " + TokenApiConfigPath);
            shExeAndReturn(SDKIP,"sed -i '/urls/d' " + TokenApiConfigPath);
            shExeAndReturn(SDKIP,"echo [[ledgers]] >>"+ TokenApiConfigPath );
        }
        //如果没有配置过[[ledgers]],先添加配置，获取行号
        else {
            shExeAndReturn(SDKIP,"echo [[ledgers]] >>"+ TokenApiConfigPath );
            lineNo = shExeAndReturn(SDKIP,"grep -n ledgers "+ TokenApiConfigPath + " | cut -d \":\" -f 1 |sed -n '1p'");
        }
        //添加应用链信息和回调地址配置
        if (ledger!= ""){
            shExeAndReturn(SDKIP,"sed -i '" + Integer.parseInt(lineNo) + "i[[ledgers]]' " + TokenApiConfigPath);
            shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "iLedger = \"" + ledger + "\"' " + TokenApiConfigPath);
            shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "iurls = " + urls + "' " + TokenApiConfigPath);
        }

    }

    public void addSDKPeerCluster(String SDKIP,String PeerIPPort,String TLSEnabled,String PeerTLSServerName){
        //获取第一个Rpc所在行号 前一行插入节点集群信息
        String lineNo = shExeAndReturn(SDKIP,"grep -n Rpc "+ SDKConfigPath + " | cut -d \":\" -f 1 |sed -n '$p'");

        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)-1) + "i[[Peer]]' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo))  + "iAddress = \"" + PeerIPPort + "\"' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "iTLSEnabled = " + TLSEnabled + "' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "iTLSServerName = \"" + PeerTLSServerName + "\"' " + SDKConfigPath);
    }

    public void setSDKApiCallbackDecrypt(String SDKApiIP ,String Decrypt)throws Exception{
        setSDKApiConfigValueByShell(SDKApiIP,"CallBack","Decrypt","\"\\\""+ Decrypt + "\"\\\"");
        assertEquals(true,getTokenApiConfigValueByShell(SDKApiIP,"CallBack","Decrypt").trim().contains(Decrypt));

    }

    public void setSDKApiCallbackLLocalId(String SDKApiIP ,String LocalId)throws Exception{
        setSDKApiConfigValueByShell(SDKApiIP,"CallBack","LocalId","\"\\\""+ LocalId + "\"\\\"");
        assertEquals(true,getTokenApiConfigValueByShell(SDKApiIP,"CallBack","LocalId").trim().contains(LocalId));

    }

    public void setSDKApiCallbackLLocalIds(String SDKApiIP ,String LocalId)throws Exception{
//        setSDKApiConfigValueByShell(SDKApiIP,"CallBack","LocalId","\"\\\""+ LocalId + "\"\\\"");
//        assertEquals(true,getTokenApiConfigValueByShell(SDKApiIP,"CallBack","LocalId").trim().contains(LocalId));

        //获取第一个[[ledgers]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(SDKApiIP,"grep -n LocalId "+ TokenApiConfigPath + " | cut -d \":\" -f 1 |sed -n '1p'");
        //先删除conf/config_api.toml文件中的所有ledgers
        if (lineNo!= ""){
            shExeAndReturn(SDKApiIP,"sed -i '/LocalId/d' " + TokenApiConfigPath);
        }
        //如果没有配置过LocalId,先添加配置，获取行号
//        else {
//            shExeAndReturn(SDKApiIP,"echo LocalId >>"+ TokenApiConfigPath );
//            lineNo = shExeAndReturn(SDKApiIP,"grep -n LocalId "+ TokenApiConfigPath + " | cut -d \":\" -f 1 |sed -n '1p'");
//        }
        //添加应用链信息和回调地址配置
        if (LocalId!= ""){
            shExeAndReturn(SDKApiIP,"sed -i '" + (Integer.parseInt(lineNo)) + "iLocalId = " + LocalId + "' " + TokenApiConfigPath);
        }

    }


    //------------------------------------------------------------------------------------------------------
    //修改节点conf/base.toml文件中的相关配置项信息
    public void setPeerTLSCertECDSA(String PeerIP)throws Exception{
        setPeerBaseByShell(PeerIP,"Rpc","TLSCaPath","\"\\\".\\/ecdsa\\/ca.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSCertPath","\"\\\".\\/ecdsa\\/cert.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSKeyPath","\"\\\".\\/ecdsa\\/key.pem\"\\\"");

        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCaPath").trim().contains("./ecdsa/ca.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCertPath").trim().contains("./ecdsa/cert.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSKeyPath").trim().contains("./ecdsa/key.pem"));
    }

    public void setPeerTLSCertDismatch(String PeerIP)throws Exception{
        setPeerBaseByShell(PeerIP,"Rpc","TLSCaPath","\"\\\".\\/dismatch\\/ca.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSCertPath","\"\\\".\\/dismatch\\/cert.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSKeyPath","\"\\\".\\/dismatch\\/key.pem\"\\\"");

        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCaPath").trim().contains("./dismatch/ca.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCertPath").trim().contains("./dismatch/cert.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSKeyPath").trim().contains("./dismatch/key.pem"));
    }
    public void setPeerTLSCertExpired(String PeerIP)throws Exception{
        setPeerBaseByShell(PeerIP,"Rpc","TLSCaPath","\"\\\".\\/expired\\/ca.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSCertPath","\"\\\".\\/expired\\/cert.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSKeyPath","\"\\\".\\/expired\\/key.pem\"\\\"");

        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCaPath").trim().contains("./expired/ca.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCertPath").trim().contains("./expired/cert.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSKeyPath").trim().contains("./expired/key.pem"));
    }

    public void setPeerLicence(String PeerIP,String value)throws Exception{
        setPeerBaseByShell(PeerIP,"Node","Licence","\"\\\".\\/" + value + "\"\\\"");
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Node","Licence").trim().contains(value));
    }

    public void setPeerContractEnabled(String PeerIP, String flag)throws Exception{
        setPeerBaseByShell(PeerIP,"Contract","Enabled",flag);
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Contract","Enabled").trim().contains(flag));
    }

    public void setPeerCryptKeyType(String PeerIP,String keytype)throws Exception{
        setPeerBaseByShell(PeerIP,"Crypt","KeyType","\"\\\"" + keytype + "\"\\\"");
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Crypt","KeyType").trim().contains(keytype));
    }

    public void setPeerCryptHashType(String PeerIP,String hashtype)throws Exception{
        setPeerBaseByShell(PeerIP,"Crypt","HashType","\"\\\"" + hashtype + "\"\\\"");
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Crypt","HashType").trim().contains(hashtype));
    }

    public void setPeerPackTime(String PeerIP,String PackTime)throws Exception{
        setPeerBaseByShell(PeerIP,"BlockChain","PackTime",PackTime);
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"BlockChain","PackTime").trim().contains(PackTime));
    }


    public void setPeerClusterOnePeer(String PeerIP,String addIP,String Port,String Type,String IPformat,String TcpType){
        shExeAndReturn(PeerIP,"echo \" \" > " + PeerMemConfigPath); //清空peer config.toml文件

        String peerID = getPeerId(addIP,USERNAME,PASSWD);
        String showName = "peer" + addIP.substring(addIP.lastIndexOf(".")+1);
        String addr =  IPformat + addIP + TcpType + Port;

        shExeAndReturn(PeerIP,"sed -i '1i[Members]' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '2i\n' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '3i[[Members.Peers]]' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '4iId = \"" + peerID + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '5iShownName = \"" + showName + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '6iAddr = \"" + addr + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '7iType = " + Type + "' " + PeerMemConfigPath);
    }

    public void addPeerCluster(String PeerIP,String addIP,String Port,String Type,String IPformat,String TcpType){
        //获取第一个[[Peer]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(PeerIP,"grep -n Type "+ PeerMemConfigPath + " | cut -d \":\" -f 1 |sed -n '$p'");
        String resp = shExeAndReturn(PeerIP,"cat " + PeerMemConfigPath);
        if(resp.contains(addIP) && resp.contains(Port)) return;

        String peerID = getPeerId(addIP,USERNAME,PASSWD);
        String showName = "peer" + addIP.substring(addIP.lastIndexOf(".")+1);
        String addr =  IPformat + addIP + TcpType + Port;
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "i\n' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "i[[Members.Peers]]' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+3) + "iId = \"" + peerID + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+4) + "iShownName = \"" + showName + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+5) + "iAddr = \"" + addr + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+6) + "iType = " + Type + "' " + PeerMemConfigPath);
    }

    public void setPeerClusterWithOneDataPeer(){
        //设置节点Peer4为数据节点
        setPeerConfigOneData(PEER1IP);
        setPeerConfigOneData(PEER2IP);
        setPeerConfigOneData(PEER4IP);
    }

    public void setPeerCluster(){
        //设置所有节点均为共识节点
        setPeerConfig(PEER1IP);
        setPeerConfig(PEER2IP);
        setPeerConfig(PEER4IP);
    }

    public void setPeerConfigOneData(String PeerIP){
        //设置节点Peer1 config 配置文件 节点Peer4为数据节点
        setPeerClusterOnePeer(PeerIP,PEER1IP,PEER1TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER2IP,PEER2TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER4IP,PEER4TCPPort,"1",ipv4,tcpProtocol);
    }

    public void setPeerConfig(String PeerIP){
        //设置节点Peer1 config 配置文件 均为共识节点
        setPeerClusterOnePeer(PeerIP,PEER1IP,PEER1TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER2IP,PEER2TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER4IP,PEER4TCPPort,"0",ipv4,tcpProtocol);
    }

    public void uploadFileToPeer(String PeerIP,String...filelist){
        for (String file : filelist) {
            uploadFiletoDestDirByssh(srcShellScriptDir + file,PeerIP,USERNAME,PASSWD,destShellScriptDir,"");
        }
    }

    public boolean checkProgramActive(String queryIP, String programName){
        boolean bRunning = false;

        String resp = shExeAndReturn(queryIP,"ps -ef|grep " + programName + " |grep -v grep");

        //需要保证进程名称的唯一性
        if (!resp.trim().isEmpty()){
            bRunning = true;
        }
        return bRunning;
    }

    //赋值权限999 区分是否主子链
    public void setPerm999WithParam(String id)throws Exception{
        String toolPath="cd "+ ToolPATH +";";
        String exeCmd="./" + ToolTPName + " permission ";

        String ledger = "";
        ledger = (subLedger!="") ? " -c " + subLedger : "";
        String preCmd = toolPath + exeCmd + "-p " + PEER1RPCPort + " -s tokenapi " + ledger + " -d " + id + " -m ";
        String getCertainPerm = toolPath + "./" + ToolTPName + " getpermission -p " + PEER1RPCPort + " -d " + id + ledger;

        //如果没有权限 则设置权限  修改为赋权限 兼容升级时权限列表变更需要重新赋予权限问题
//        if(!shExeAndReturn(PEER1IP,getCertainPerm).contains(fullPerm)){
            assertEquals(true,shExeAndReturn(PEER1IP,preCmd + "999").contains("success"));
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals(true,shExeAndReturn(PEER1IP,getCertainPerm).contains(fullPerm));
//        }
    }

    public String getIDByMgTool(String remoteIP,String keyPath) {
        String result = "";
        Shell shellTest = new Shell(remoteIP,USERNAME,PASSWD);
        shellTest.execute("cd "+ ToolPATH+";./" + ToolTPName + " getid -p " + keyPath);

        ArrayList<String> stdout3 = shellTest.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.contains("id:"))
            {
                result = str1.split(":")[1];
                break;
            }
        }
        return result;
    }

    public String sdkCheckTxOrSleep(String hashData,String type,long sleeptime)throws Exception{

        if(hashData.equals("null") || hashData.equals("") ||hashData.equals(null) ||hashData.isEmpty()){
            sleepAndSaveInfo(SLEEPTIME/2,"hash is null waiting .....");
            return "";
        }
        if(hashData.contains(" ")){
            sleepAndSaveInfo(SLEEPTIME/2,"hash is invalid waiting .....");
            return "";
        }

        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        log.info("开始时间 " + nowTime);

        assertEquals(false,hashData.isEmpty());//先检查hash是否为空，为空则不执行
        log.info("query hash  " + hashData);

        //20210120 3.1 sdk使用默认数据库非mysql 即不存在配置项
//        Boolean bWallet = true;
//        if(type.equals(utilsClass.sdkGetTxDetailType)){
//            bWallet = getSDKWalletEnabled();//获取sdk 钱包是否开启
//        }
        long internal = 0;

        Boolean bOK = false;
        Boolean bInlocal = false;
        String state ="";

        //查询交易是否上链
        while((new Date()).getTime() - nowTime < sleeptime && bOK == false){
            //当前支持旧版本SDK gettxdetail接口 tokenapi gettxdetail接口
             switch (type){
                case "0":
                    state = JSONObject.fromObject(store.GetTxDetail(hashData)).getString("state");
                    break;
                case "1":
                    state = JSONObject.fromObject(tokenModule.tokenGetTxDetail(hashData)).getString("state");
                    break;
                case "2":
                    state = JSONObject.fromObject(store.GetTxDetail(hashData)).getString("state");
                    break;
                 default:
                     log.info("Wrong type! ");
            }


            if(state.equals("200"))
                bOK = true;
            else
                sleepAndSaveInfo(1000,"等待再次检查交易是否上链时间");
        }
        log.info("============================= 查询交易上链 " + bOK + " 等待时间 " +
                hashData + " " + ((new Date()).getTime() - nowTime));
        if(bOK){
            sleepAndSaveInfo(2000,"============================= 等待数据库同步数据");
        }


//        return hashData + " " + ((new Date()).getTime() - nowTime);
        return "";
    }

    public void sdkCheckTxOrSleepNoDBQuery(String hashData, String type, long sleeptime)throws Exception{
        if (hashData.equals(null) || hashData.equals("")) {
            log.info("hash is null");
            sleepAndSaveInfo(SLEEPTIME);
            return;
        }
        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        log.info("开始时间 " + nowTime);

        Boolean bOK = false;
        String state ="";

        //查询交易是否上链
        while((new Date()).getTime() - nowTime < sleeptime && bOK == false){
            //当前支持旧版本SDK gettxdetail接口 tokenapi gettxdetail接口
            if(type.equals("0")) state = JSONObject.fromObject(store.GetTxDetail(hashData)).getString("state");
            else if(type.equals("1")) state = JSONObject.fromObject(tokenModule.tokenGetTxDetail(hashData)).getString("state");
            if(state.equals("200"))
                bOK = true;
            else
                sleepAndSaveInfo(250,"等待再次检查交易是否上链时间");
        }
        long internal = (new Date()).getTime() - nowTime;
        log.info("============================= 查询交易上链 " + bOK + " 等待时间 " +
                hashData + " " + internal);
    }

    /***
     * 此函数用于从交易返回结果response中获取sdk或者token api交易hash
     * 不适用管理工具hash获取
     * @param response 交易返回信息 目前应该是一个json字符串
     * @param type 根据特定类型获取 因sdk不同交易或版本不同解析json所需要获取的json字段不同
     * @return
     */
    public String getTxHash(String response, String type){
        String hash = "";
        assertEquals(false,response.isEmpty()); //检查response是否为空，为空则失败 不向下执行
        //0* 为SDK旧版本接口获取type类型起始 *为交易类型 以所有交易类型小序号为主 0表示存证 1表示 utxo
        //1* 为token api接口获取type类型起始
        //2* 为SDK新版本接口获取type类型起始
        switch (type){
            case "00" :
                //旧版本sdk接口 存证/隐私存证/docker创建合约/docker合约交易/docker合约销毁/utxo单签回收/WVM合约交易
                hash = JSONObject.fromObject(response).getString("data");
                break;
            case "01" :
                //旧版本sdk接口 utxo hash 单签发行/转账 除单签回收
                hash = JSONObject.fromObject(response).getString("data");
                break;
            case "02" :
                //旧版本sdk接口 utxo hash 多签发行/转账/回收
                hash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
                break;
            case "10" :
                //token api v1 接口 hash 除destroybytype
                hash = JSONObject.fromObject(response).getString("data");
                break;
            case "11" :
                //token api v1 接口 destroybytype
                hash = JSONObject.fromObject(response).getJSONObject("data").getString("hash");
                break;
            case "20" :
                //新版本SDK v2接口
                hash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
                break;
            case "21" :
                //新版本SDK v2接口
                hash = JSONObject.fromObject(response).getString("data");
                break;
            case "mg" :
                //管理工具命令执行
                assertEquals(true,response.contains("success:"));
                hash = response.substring(response.lastIndexOf("success:") + 8).trim();
                break;
            default:
                log.info("test hash " + hash);
                log.info("resolve hash type " + type);
                log.info("Can not resolve tx hash,please check type 00 01 02 10 20 or mg!");
        }
        return hash;
    }

    public Boolean checkDataInMysqlDB(String sdkIP,String table,String key,String checkData)throws Exception{
        assertEquals("检查数据不为空",false,checkData.isEmpty());
        MysqlOperation mysqlOperation = new MysqlOperation();

        String dbConfig = getSDKConfigValueByShell(utilsClass.getIPFromStr(sdkIP),"Wallet","DBPath");//token api db无交易hash存储表

        String database = getStrByReg(dbConfig,"\\/(.*?)\\?");
        String mysqlIP = utilsClass.getIPFromStr(dbConfig);
        if(!subLedger.isEmpty()) table = table + "_sub_" +subLedger.toLowerCase();
        String temp = mysqlOperation.checkDataExist(mysqlIP,database,table,key,checkData);
        return temp.contains(checkData);
    }

    public Boolean uploadFile(){
            uploadFileToPeer(PEER1IP, "startWithParam.sh", "SetConfig.sh", "GetConfig.sh");
            uploadFileToPeer(PEER2IP, "startWithParam.sh", "SetConfig.sh", "GetConfig.sh");
            uploadFileToPeer(PEER3IP, "startWithParam.sh", "SetConfig.sh", "GetConfig.sh");
            uploadFileToPeer(PEER4IP, "startWithParam.sh", "SetConfig.sh", "GetConfig.sh");

            //确认目标目录中存在被传输文件
            String resp = shExeAndReturn(PEER1IP, "ls " + destShellScriptDir);
            assertEquals(true, resp.contains("startWithParam.sh"));
            assertEquals(true, resp.contains("SetConfig.sh"));
            assertEquals(true, resp.contains("GetConfig.sh"));
            shExeAndReturn(PEER1IP, "chmod +x " + destShellScriptDir + "*.sh");
            shExeAndReturn(PEER1IP,"sed -i 's/\\\r//g' " + destShellScriptDir + "*.sh");

            resp = shExeAndReturn(PEER2IP, "ls " + destShellScriptDir);
            assertEquals(true, resp.contains("startWithParam.sh"));
            assertEquals(true, resp.contains("SetConfig.sh"));
            assertEquals(true, resp.contains("GetConfig.sh"));
            shExeAndReturn(PEER2IP, "chmod +x " + destShellScriptDir + "*.sh");
            shExeAndReturn(PEER2IP,"sed -i 's/\\\r//g' " + destShellScriptDir + "*.sh");

            resp = shExeAndReturn(PEER3IP, "ls " + destShellScriptDir);
            assertEquals(true, resp.contains("startWithParam.sh"));
            assertEquals(true, resp.contains("SetConfig.sh"));
            assertEquals(true, resp.contains("GetConfig.sh"));
            shExeAndReturn(PEER3IP, "chmod +x " + destShellScriptDir + "*.sh");
            shExeAndReturn(PEER3IP,"sed -i 's/\\\r//g' " + destShellScriptDir + "*.sh");

            resp = shExeAndReturn(PEER4IP, "ls " + destShellScriptDir);
            assertEquals(true, resp.contains("startWithParam.sh"));
            assertEquals(true, resp.contains("SetConfig.sh"));
            assertEquals(true, resp.contains("GetConfig.sh"));
            shExeAndReturn(PEER4IP, "chmod +x " + destShellScriptDir + "*.sh");
            shExeAndReturn(PEER4IP,"sed -i 's/\\\r//g' " + destShellScriptDir + "*.sh");

            return true;
    }

    public void clearDockerImages(String peerIP,String keyWork)throws Exception{
        shellExeCmd(peerIP,"docker rm -f `docker ps -aq`");
        shellExeCmd(peerIP,"docker images|grep " + keyWork + "|awk '{print $1}'|xargs docker rmi");
    }


    public String hexToBase64String(String HexStr)throws Exception{
        String hash = HexStr;
        //目前现有梧桐链应用hex hash是60位 base64大概是44位 以长度作为判断 后续根据实际情况进行调整
        if(HexStr.length() > 50){
            byte[] decodeHex = hexStr2Bytes(HexStr);
            hash = (new BASE64Encoder()).encodeBuffer(decodeHex);
        }
        return hash;
    }
    public byte[] hexStr2Bytes(String src){
        /*对输入值进行规范化整理*/
        src = src.trim().replace(" ", "").toUpperCase(Locale.US);
        //处理值初始化
        int m=0,n=0;
        int iLen=src.length()/2; //计算长度
        byte[] ret = new byte[iLen]; //分配存储空间

        for (int i = 0; i < iLen; i++){
            m=i*2+1;
            n=m+1;
            ret[i] = (byte)(Integer.decode("0x"+ src.substring(i*2, m) + src.substring(m,n)) & 0xFF);
        }
        return ret;
    }


    public Boolean compareHashMap(Map<String,String> before ,Map<String,String> after,String keyWord)throws Exception{
        ArrayList<String> diffRespList = new ArrayList<>();
        Boolean bSame = true;

        log.info("前Map检查数据长度： " + before.size() + "\n后Map检查数据长度： " +  after.size());
        assertEquals("前后Map存储信息个数不等请确认",before.size(),after.size());

        Iterator iter = before.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if(!before.get(key).equals(after.get(key)))
            {
                diffRespList.add(key.toString());
                diffRespList.add(before.get(key));
                diffRespList.add(after.get(key));
            }
        }
        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String diffSaveFile = testResultPath  + keyWord + "compare" + sdf.format(nowTime) + "-diff.txt";
        File diff = new File(diffSaveFile);
        if(diff.exists()) diff.delete();//如果存在则先删除

        if(diffRespList.size() > 0) {
            for(int i = 0;i < diffRespList.size();i++){
                //log.info(diffRespList.get(i));
                FileOperation fileOperation = new FileOperation();
                fileOperation.appendToFile(diffRespList.get(i),diffSaveFile);
            }
            log.info("data not same");
            bSame = false;
        }
        return  bSame;
    }

    public ArrayList<String> getTxFromBlock(int start,int end) {
        log.info(start + "  " + end);
        ArrayList <String> txList = new ArrayList<>();
        for(int i = start; i <= end ;i++){
            String[] temp = getTxsArray(i);
            log.info("temp size " + temp.length);
            for(int j=0;j<temp.length;j++){
                log.info("11 " + temp[j]);
                txList.add(temp[j]);
            }
        }
        log.info("返回交易个数 " + txList.size());
        return txList;
    }

    //获取除指定类型外的交易hash
    public ArrayList<String> getTxArrayExceptKeyWord(ArrayList<String> srcArr,String... keyWord){
        ArrayList<String> txSpecArr = new ArrayList<>();
        for(int i =0 ;i<srcArr.size();i++){
            Boolean bContain = false;
            String resp = store.GetTxDetail(srcArr.get(i));
            for(int k =0;k<keyWord.length;k++){
                if(resp.contains(keyWord[k])) {
                    bContain = true;
                    break;
                }
            }
            if(!bContain)  txSpecArr.add(srcArr.get(i));
        }
        log.info("返回交易个数 " + txSpecArr.size());
        return txSpecArr;
    }

    //获取除指定类型的交易hash
    public ArrayList<String> getTxArrayWithKeyWord(ArrayList<String> srcArr,String... keyWord){
        ArrayList<String> txSpecArr = new ArrayList<>();
        for(int i =0 ;i<srcArr.size();i++){
            Boolean bContain = false;
            String resp = store.GetTxDetail(srcArr.get(i));
            for(int k =0;k<keyWord.length;k++){
                if(resp.contains(keyWord[k])) {
                    bContain = true;
                    break;
                }
            }
            if(bContain)  txSpecArr.add(srcArr.get(i));
        }
        log.info("返回交易个数 " + txSpecArr.size());
        return txSpecArr;
    }

    //根据区块高度获取区块中的交易列表
    public String[] getTxsArray(int i) {
        String txsList = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("data").getString("txs");
        txsList = txsList.substring(2);
        txsList = StringUtils.substringBefore(txsList, "\"]");
        String[] txs = txsList.split("\",\"");
        return txs;
    }

    /**
     * 调用go编译的schema校验数据的exe进行schema和数据检验
     * @param commonDir schema文档和数据文件 需要存放在同一个目录
     * @param exeName  支持传入schema文件校验数据文件/数据字符串
     * @param schemaFileName 传入的schema文件
     * @param data 传入的待校验的数据文件或者数据
     * @return
     */
    public Boolean schemaCheckData(String commonDir, String exeName, String schemaFileName, String data,String dataType){
        Boolean bResult = true;
        BufferedReader br = null;
        BufferedReader brError = null;
        log.info(data);
        if(!dataType.equals("1")){
//            //此处为字符串作为参数传入的场景处理 文件名作为参数处理时不需要额外处理
//            //作为将要调用exe的传参 需要进行多重转义 并且需要将\n和空格删除
            data = "\"" + data.replaceAll("(\n)( )?","").replaceAll("\"","\\\\\\\"") + "\"";
        }
        String cmd = commonDir + exeName + " " + commonDir + " " +  schemaFileName + " " + data + " " + dataType;

        try {
            //执行exe  cmd可以为字符串(exe存放路径)也可为数组，调用exe时需要传入参数时，可以传数组调用(参数有顺序要求)
            Process p = Runtime.getRuntime().exec(cmd);
            String line = null;
            //获得子进程的输入流。
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            //获得子进程的错误流。
            brError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = br.readLine()) != null  || (line = brError.readLine()) != null) {
                //输出exe输出的信息以及错误信息
                System.out.println(line);
                bResult = line.contains("The document is valid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return  bResult;
    }

    /**
     *  验证交易详情字段
     * @param txId
     * @param txType
     * @param version
     * @param type
     * @param subType
     */
    public void verifyTxDetailField(String txId, String txType, String version, String type, String subType){
        String response = store.GetTxDetail(txId);

        assertEquals(JSONObject.fromObject(response).getString("state").equals("200"),true);
        assertEquals(JSONObject.fromObject(response).getString("message").equals("success"),true);

        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("version").equals(version),true);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("type").equals(type),true);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("subType").equals(subType),true);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("transactionId").equals(txId),true);
        JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("timestamp");

        JSONObject.fromObject(response).getJSONObject("data").getString("raw");

        switch (txType){
            case "store":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("store").getString("storeData");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("store").getString("extra");
                break;
            case "wvm_install":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("version");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("args");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("random");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("src");
                break;
            case "wvm_invoke":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("version");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("args");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getString("result");
                break;
            case "wvm_destroy":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("version");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("args");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name");
                break;
            case "smt_issue":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("version");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("args");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getString("txRecords");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getString("result");
                break;
            case "smt_transfer":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("utxo").getString("txRecords");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("utxo").getString("data");
                break;
            case "smt_exchange":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("utxo").getString("txRecords");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("utxo").getString("data");
                break;
            case "smt_destroy":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("utxo").getString("txRecords");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("utxo").getString("data");
                break;
            case "smt_freeze":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("version");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("args");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getString("result");
                break;
            case "smt_recover":
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("version");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("args");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name");
                JSONObject.fromObject(response).getJSONObject("data").getJSONObject("wvm").getString("result");
                break;
        }

    }

    /**
     * 验证交易原始数据详情字段
     * @param txId
     * @param version
     * @param type
     * @param subType
     */
    public void verifyTxRawField(String txId, String version, String type, String subType){
        String response = store.GetTxRaw(txId);

        assertEquals(JSONObject.fromObject(response).getString("state").equals("200"),true);
        assertEquals(JSONObject.fromObject(response).getString("message").equals("success"),true);

        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("version").equals(version),true);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("type").equals(type),true);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("subType").equals(subType),true);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("transactionId").equals(txId),true);
        JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header").getString("timestamp");

        JSONObject.fromObject(response).getJSONObject("data").getString("data");
        JSONObject.fromObject(response).getJSONObject("data").getString("pubkey");
        JSONObject.fromObject(response).getJSONObject("data").getString("sign");
        JSONObject.fromObject(response).getJSONObject("data").getString("raw");
        JSONObject.fromObject(response).getJSONObject("data").getString("result");
        JSONObject.fromObject(response).getJSONObject("data").getString("extra");
        JSONObject.fromObject(response).getJSONObject("data").getJSONObject("txproof").getString("left");
        JSONObject.fromObject(response).getJSONObject("data").getJSONObject("txproof").getString("right");

    }


    /**
     * 验证交易详情与交易原始数据raw字段值一致
     * @param txId
     */
    public void verifyRawFieldMatch(String txId){
        String response1 = store.GetTxRaw(txId);
        String response2 = store.GetTxDetail(txId);

        String raw1 = JSONObject.fromObject(response1).getJSONObject("data").getString("raw");
        String raw2 = JSONObject.fromObject(response2).getJSONObject("data").getString("raw");

        assertEquals(raw1.equals(raw2),true);
    }

    public Boolean compareTwoStr(String str1,String str2){
        Boolean bSame = true;
        log.info(str1);
        log.info(str2);
        char ch;
        char[] arr1 = str1.toCharArray();
        char[] arr2 = str2.toCharArray();
        for(int i=0;i<arr1.length;i++)
        {
            if(arr1[i]!=arr2[i]) {
                bSame = false;
                String keyWord1 = str1.substring(i,i+10);
                String keyWord2 = str2.substring(i,i+10);
                log.info("test diff from column " + i + " arr1 " + keyWord1 + " arr2 " + keyWord2);
                break;
            }
        }
        return bSame;
    }

    public void verifyBlockAndTransaction(String toolPath){

        String result = shExeAndReturn(utilsClass.getIPFromStr(SDKADD),
                "cd " + toolPath + ";./verifyBlockTX su -p " + SDKADD + " -l " + subLedger);
        log.info("区块验证结果 **********************************************************************");
        log.info(result);
        String OKMsg = "successfully";
        String BlockErr = "错误";
        String TXErr = "tx verify failed";

        if(result.contains(BlockErr) || result.contains(TXErr)){
            assertEquals("交易或区块验证失败",false,true);
        }

        assertEquals(true,result.contains("All blocks verified successfully"));

    }

    public int CalculatetotalTxs(String id, int startBlockHeight, int endBlockHeight) throws Exception {

        subLedger = id;
        int count = 0, total = 0;

        for (int i = startBlockHeight + 1; i <= endBlockHeight; i++) {
            //获取区块中的交易个数
            String[] txs = getTxsArray(i);
            count = txs.length;

            total = total + count;
        }


        log.info("交易总数：" + total);
        return total;
    }

}
