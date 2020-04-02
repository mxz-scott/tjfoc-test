package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;

import java.util.*;

//import static com.java.tar.gz.FileUtil.log;
import static com.tjfintech.common.utils.FileOperation.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
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
    //获取所有地址账户与私钥密码信息
    JSONObject jsonObjectAddrPri;


    //-----------------------------------------------------------------------------------------------------------
    //获取交易hash函数 此处兼容


    //-----------------------------------------------------------------------------------------------------------
    public void setPermAndCheckResp(String peerIP,String peerPort,String remoteId,String permList)throws Exception{
        String toolPath = "cd " + ToolPATH + ";";
        String ledger = (subLedger != "") ? " -z " + subLedger:"";
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
        jsonObjectAddrPri = mapAddrInfo();

        String addrInfo = jsonObjectAddrPri.getString(queryAccount);
        String prikey = JSONObject.fromObject(JSONObject.fromObject(addrInfo).getJSONArray("keyList").get(0)).getString("priKey");
        String pwd = JSONObject.fromObject(JSONObject.fromObject(addrInfo).getJSONArray("keyList").get(0)).getString("pwd");
        String sign = JSONObject.fromObject(addrInfo).getString("sign");

        //判断单签还是多签
        if(sign.equals("0")){
            //单签账户查询
            if(pwd.isEmpty()) {
                //不带密码的私钥查询余额
                response = soloSign.Balance(prikey,queryTokenType);
            }
            else
            {
                //带密码的私钥查询余额
                response = soloSign.Balance(prikey,pwd,queryTokenType);
            }
        }
        else{
            //多签账户余额查询
            if(pwd.isEmpty()) {
                //不带密码查询
                response = multiSign.Balance(queryAccount, prikey, queryTokenType);
            }
            else
            {
                //带密码查询
                response = multiSign.Balance(queryAccount, prikey, pwd, queryTokenType);
            }
        }
        assertEquals("200",JSONObject.fromObject(response).getString("State"));
        balanceAmount = JSONObject.fromObject(response).getJSONObject("Data").getString("Total");
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
            tx = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
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
                assertEquals("200", JSONObject.fromObject(response2).getString("State"));
                if (JSONObject.fromObject(response2).getJSONObject("Data").getString("IsCompleted").contains("true"))
                    break;
                else
                    tx = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
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
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        System.out.print("第一次签名");
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




    /**
     * 查询各个单签多签账户余额
     * @throws Exception
     */

    public Map<String, Object>  getUTXOAccountBalance() {
        CommonFunc commonFunc = new CommonFunc();
        String tokenColl = multiSign.tokenstate("");
        JSONArray tokenArr = JSONObject.fromObject(tokenColl).getJSONArray("Data");
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
            if(account.equals("")) {
                if(pwd.equals("")) {
                    tokenTotal = JSONObject.fromObject(
                            soloSign.Balance(accountPrikey, token)).getJSONObject("Data").getString("Total");
                }else{
                    tokenTotal = JSONObject.fromObject(
                            soloSign.Balance(accountPrikey, pwd,token)).getJSONObject("Data").getString("Total");
                }
            }
            else{
                if(pwd.equals("")) {
                    tokenTotal = JSONObject.fromObject(
                            multiSign.Balance(account, accountPrikey, token)).getJSONObject("Data").getString("Total");
                }else {
                    tokenTotal = JSONObject.fromObject(
                            multiSign.Balance(account, accountPrikey,pwd, token)).getJSONObject("Data").getString("Total");
                }
            }
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

    public void setSDKWalletAddrDBMysql(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mysqlDBAddr);
//        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mysql\"\\\"");
//        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mysqlDBAddr);

        String checkMyqlDBAddr = mysqlDBAddr.replaceAll("\"","").replaceAll("\\\\","");
//        System.out.print(checkMyqlDBAddr);

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMyqlDBAddr));
//        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mysql"));
//        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMyqlDBAddr));
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


    public void setSDKOnePeer(String SDKIP,String PeerIPPort,String TLSEnabled){
        //获取第一个[[Peer]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(SDKIP,"grep -n Peer "+ SDKConfigPath + " | cut -d \":\" -f 1 |sed -n '1p'");
        //先删除conf/config.toml文件中的所有Peers
        shExeAndReturn(SDKIP,"sed -i '/Peer/d' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '/Address/d' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '/TLSEnabled/d' " + SDKConfigPath);

        shExeAndReturn(SDKIP,"sed -i '" + Integer.parseInt(lineNo) + "i[[Peer]]' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "iAddress = \"" + PeerIPPort + "\"' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "iTLSEnabled = " + TLSEnabled + "' " + SDKConfigPath);
    }

    public void addSDKPeerCluster(String SDKIP,String PeerIPPort,String TLSEnabled){
        //获取第一个[[Peer]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(SDKIP,"grep -n TLSEnabled "+ SDKConfigPath + " | cut -d \":\" -f 1 |sed -n '$p'");

        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "i[[Peer]]' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2)  + "iAddress = \"" + PeerIPPort + "\"' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+3) + "iTLSEnabled = " + TLSEnabled + "' " + SDKConfigPath);
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
        String addr = "\\/" + IPformat + "\\/" + addIP +"\\/" + TcpType + "\\/" + Port;

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

        String peerID = getPeerId(addIP,USERNAME,PASSWD);
        String showName = "peer" + addIP.substring(addIP.lastIndexOf(".")+1);
        String addr = "\\/" + IPformat + "\\/" + addIP +"\\/" + TcpType + "\\/" + Port;
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
        ledger = (subLedger!="") ? " -z " + subLedger : "";
        String preCmd = toolPath + exeCmd + "-p " + PEER1RPCPort + " -s tokenapi " + ledger + " -d " + id + " -m ";
        String getCertainPerm = toolPath + "./" + ToolTPName + " getpermission -p " + PEER1RPCPort + " -d " + id + ledger;

        //如果没有权限 则设置权限
        if(!shExeAndReturn(PEER1IP,getCertainPerm).contains(fullPerm)){
            assertEquals(true,shExeAndReturn(PEER1IP,preCmd + "999").contains("success"));
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals(true,shExeAndReturn(PEER1IP,getCertainPerm).contains(fullPerm));
        }
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
        String resultDesp = "";
        long internal = 0;
        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        log.info("开始时间 " + nowTime);
        Boolean bOK = false;
        String state ="";
        while((new Date()).getTime() - nowTime < sleeptime && bOK == false){
            //当前支持旧版本SDK gettxdetail接口 tokenapi gettxdetail接口
            if(type.equals("0")) state = JSONObject.fromObject(store.GetTxDetail(hashData)).getString("State");
            else if(type.equals("1")) state = JSONObject.fromObject(tokenModule.tokenGetTxDetail(hashData)).getString("state");
            if(state.equals("200"))
                bOK = true;
            else
                sleepAndSaveInfo(1000,"等待再次检查交易是否上链时间");
        }
        //计算查询时间
        log.info("当前时间 " + (new Date()).getTime());
        internal = (new Date()).getTime() - nowTime;

        log.info("查询交易上链 " + bOK + " 等待时间 " + hashData + " " + internal);

        return hashData + " " + internal;
    }

    /***
     * 次函数用于从交易返回结果response中获取sdk或者token api交易hash
     * 不适用管理工具hash获取
     * @param response 交易返回信息 目前应该是一个json字符串
     * @param type 根据特定类型获取 因sdk不同交易或版本不同解析json所需要获取的json字段不同
     * @return
     */
    public String getTxHash(String response, String type){
        String hash = "";
        //0* 为SDK旧版本接口获取type类型起始 *为交易类型 以所有交易类型小序号为主 0表示存证 1表示 utxo
        //1* 为token api接口获取type类型起始
        //2* 为SDK新版本接口获取type类型起始
        switch (type){
            case "00" :
                //旧版本sdk接口 存证/隐私存证/docker创建合约/docker合约交易/docker合约销毁/utxo单签回收/WVM合约交易
                hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
                break;
            case "01" :
                //旧版本sdk接口 utxo hash 单签发行/转账 除单签回收
                hash = JSONObject.fromObject(response).getString("Data");
                break;
            case "02" :
                //旧版本sdk接口 utxo hash 多签发行/转账/回收
                hash = JSONObject.fromObject(response).getJSONObject("Data").getString("TxId");
                break;
            case "10" :
                //token api v1 接口 hash
                hash = JSONObject.fromObject(response).getString("data");
                break;
            case "20" :
                //新版本SDK v2接口
                hash = JSONObject.fromObject(response).getString("data");
                break;
            case "mg" :
                //新版本SDK v2接口
                assertEquals(true,response.contains("success:"));
                hash = response.substring(response.lastIndexOf("success:") + 8).trim();
                break;
            default:
                log.info("Can not resolve tx hash,please check type 01 02 10 20!");
        }
        return hash;
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

}
