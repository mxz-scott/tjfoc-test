package com.tjfintech.common;

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
import org.hamcrest.CoreMatchers;
import sun.misc.BASE64Encoder;

import java.io.File;
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

        amountMap.put("\"from\"","\"" + from + "\"");
        amountMap.put("\"to\"","\"" + to + "\"");
        amountMap.put("\"tokenType\"","\"" + tokenType + "\"");
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
     * @param list
     * @return
     */
    public static List<Map> constructUTXOTxDetailList(String from, String to, String tokenType, String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new LinkedHashMap<>();

        amountMap.put("\"from\"","\"" + from + "\"");
        amountMap.put("\"to\"","\"" + to + "\"");
        amountMap.put("\"tokenType\"","\"" + tokenType + "\"");
        amountMap.put("\"amount\"","\"" + amount + "\"");

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

    public void addSDKPeerCluster(String SDKIP,String PeerIPPort,String TLSEnabled,String PeerTLSServerName){
        //获取第一个Rpc所在行号 前一行插入节点集群信息
        String lineNo = shExeAndReturn(SDKIP,"grep -n Rpc "+ SDKConfigPath + " | cut -d \":\" -f 1 |sed -n '$p'");

        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)-1) + "i[[Peer]]' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo))  + "iAddress = \"" + PeerIPPort + "\"' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "iTLSEnabled = " + TLSEnabled + "' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "iTLSServerName = \"" + PeerTLSServerName + "\"' " + SDKConfigPath);
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
        Boolean bWallet = true;
        if(type.equals(utilsClass.sdkGetTxDetailType)){
            bWallet = getSDKWalletEnabled();//获取sdk 钱包是否开启
        }
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
        //如果确认交易上链 则去数据库查询或者确认节点高度是否一致，交易在指定时间内未查询到上链则不进行更多的检查
//        if(bOK) {
//            //需要排除sdk 钱包关闭场景
//            //钱包开启则查询数据库中是否已经同步区块
//            long nowTimeDB = (new Date()).getTime();
//            if(bWallet) {
//                while ((new Date()).getTime() - nowTimeDB < DBSyncTime && bInlocal == false) {
//                    bInlocal = checkDataInMysqlDB(rSDKADD, "tx_finish", "hash", hashData);
//                    Thread.sleep(500);
//                }
//                log.info("============================= 查询数据库更新 " + bInlocal + " 等待时间 "
//                        + hashData + " " + ((new Date()).getTime() - nowTimeDB));
////                assertEquals("数据库在未同步到已上链交易",true,bInlocal);
//
//                if(type.equals(utilsClass.tokenApiGetTxDetailTType)){
//                    sleepAndSaveInfo(1000,"============================= 等待 token api数据库同步数据");
//                }
//            }
//            if(!bWallet || type.equals(utilsClass.tokenApiGetTxDetailTType)){
            if(!bWallet){
                //检查节点高度是否一致
                //如果是钱包关闭场景 及 token api场景下不支持数据库是否同步到交易的查询
                Boolean bEqual = false;
                //确认所有节点均同步
                long nowTimeSync = (new Date()).getTime();
                bEqual = mgToolCmd.mgCheckHeightOrSleep(
                        PEER1IP + ":" + PEER1RPCPort,PEER2IP + ":" + PEER2RPCPort,
                        30*1000,300);
//                if(!bEqual) log.info("============================= 等待节点同步区块时间 " + ((new Date()).getTime() - nowTimeSync));
//                assertEquals("高度检查不一致",true,bEqual);
                bEqual = mgToolCmd.mgCheckHeightOrSleep(
                        PEER1IP + ":" + PEER1RPCPort,PEER4IP + ":" + PEER4RPCPort,
                        30*1000,300);
                log.info("============================= 等待节点同步区块时间 " + ((new Date()).getTime() - nowTimeSync));
//                assertEquals("高度检查不一致",true,bEqual);
            }
//        }

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
                sleepAndSaveInfo(100,"等待再次检查交易是否上链时间");
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


    public void compareHashMap(Map<String,String> before ,Map<String,String> after)throws Exception{
        ArrayList<String> diffRespList = new ArrayList<>();

        log.info("升级前检查数据长度： " + before.size() + "\n升级后检查数据长度： " +  after.size());
        assertEquals("升级前后存储信息个数不等请确认",before.size(),after.size());

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
        String diffSaveFile = resourcePath + nowTime + "-diff.txt";
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static List<Map> gdConstructShareListV1(String address, double amount, int shareProperty){

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }


    public static List<Map> gdConstructShareListV1(String address, double amount, int shareProperty, List<Map> list){
        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);

        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareList(String address, double amount, int shareProperty){

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",registerInfo);
        shares.put("transactionReport",txInformation);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareList(String address, double amount, int shareProperty,List<Map> list){
        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",registerInfo);
        shares.put("transactionReport",txInformation);

        shareList.add(shares);
        return shareList;
    }

    public static List<Map> getShareListFromQueryNoZeroAcc(JSONArray dataShareList)throws Exception {
        List<Map> getShareList = new ArrayList<>();
        for (int i = 0; i < dataShareList.size(); i++) {

            if (dataShareList.get(i).toString().contains(zeroAccount))
                continue;
            else {
                double amount = JSONObject.fromObject(dataShareList.get(i)).getDouble("amount");
                double lockAmount = JSONObject.fromObject(dataShareList.get(i)).getDouble("lockAmount");
                String address = JSONObject.fromObject(dataShareList.get(i)).getString("address");
                int shareProperty = JSONObject.fromObject(dataShareList.get(i)).getInt("shareProperty");
                String sharePropertyCN = JSONObject.fromObject(dataShareList.get(i)).getString("sharePropertyCN");
                getShareList = gdConstructQueryShareList(address, amount, shareProperty, lockAmount,sharePropertyCN,getShareList);
            }
        }
        return getShareList;
    }

    public static List<Map>   gdConstructQueryShareList(String address, double amount, int shareProperty,double lockAmount,String sharePropertyCN, List<Map> list){
        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("lockAmount",lockAmount);
        shares.put("shareProperty",shareProperty);
        shares.put("sharePropertyCN",sharePropertyCN);

        shareList.add(shares);
        return shareList;
    }


    public static double getTotalAmountFromShareList(JSONArray dataShareList)throws Exception {
        List<Map> getShareList = new ArrayList<>();
        double total = 0;
        for (int i = 0; i < dataShareList.size(); i++) {
            total = JSONObject.fromObject(dataShareList.get(i)).getDouble("amount") + total;
        }
        return total;
    }

    public static Map<String,String> mapShareENCN()throws Exception{
        Map<String,String> mapShareTypeCN = new HashMap<>();
        mapShareTypeCN.put("0","流通股");
        mapShareTypeCN.put("1","优先股");
        mapShareTypeCN.put("11","公众已托管-社会公众股");
        mapShareTypeCN.put("12","公众已托管-高管买入股份");
        mapShareTypeCN.put("13","公众已托管-限售流通股(个人)");
        mapShareTypeCN.put("14","公众已托管-限售流通股(机构)");
        mapShareTypeCN.put("2","资格股");
        mapShareTypeCN.put("21","发起人股-境内法人股");
        mapShareTypeCN.put("22","发起人股-国有股");
        mapShareTypeCN.put("23","发起人股-境外法人股");
        mapShareTypeCN.put("24","发起人股-境内自然人股");
        mapShareTypeCN.put("25","发起人股-境外自然人股");
        mapShareTypeCN.put("26","发起人股-境内其它机构");
        mapShareTypeCN.put("27","发起人股-境外其它机构");
        mapShareTypeCN.put("28","发起人股-认缴未出资");
        mapShareTypeCN.put("29","发起人股-其它");
        mapShareTypeCN.put("3","定增股");
        mapShareTypeCN.put("31","定增股-国有股");
        mapShareTypeCN.put("32","定增股-境内法人股");
        mapShareTypeCN.put("33","定增股-境外法人股");
        mapShareTypeCN.put("34","定增股-境内自然人股");
        mapShareTypeCN.put("35","定增股-境外自然人股");
        mapShareTypeCN.put("36","定增股-境内其它机构");
        mapShareTypeCN.put("37","定增股-境外其它机构");
        mapShareTypeCN.put("39","定增股-其它");
        mapShareTypeCN.put("4","特限股");
        mapShareTypeCN.put("41","控股股东、实控人、一致行动人股");
        mapShareTypeCN.put("42","承诺人股");
        mapShareTypeCN.put("43","高管分红股、转增股");
        mapShareTypeCN.put("44","高管定增股");
        mapShareTypeCN.put("45","高管限售股");
        mapShareTypeCN.put("46","股权激励股");
        mapShareTypeCN.put("49","特限股-其它");
        mapShareTypeCN.put("5","托管股");
        mapShareTypeCN.put("51","托管股-国有股");
        mapShareTypeCN.put("52","托管股-境内法人股");
        mapShareTypeCN.put("53","托管股-境外法人股");
        mapShareTypeCN.put("54","托管股-境内自然人股");
        mapShareTypeCN.put("55","托管股-境外自然人股");
        mapShareTypeCN.put("56","托管股-境内其它机构");
        mapShareTypeCN.put("57","托管股-境外其它机构");
        mapShareTypeCN.put("58","托管股-高管股");
        mapShareTypeCN.put("59","托管股-其它");
        mapShareTypeCN.put("6","有限公司股");
        mapShareTypeCN.put("61","有限公司股-国有股");
        mapShareTypeCN.put("62","有限公司股-境内法人股");
        mapShareTypeCN.put("63","有限公司股-境外法人股");
        mapShareTypeCN.put("64","有限公司股-境内自然人股");
        mapShareTypeCN.put("65","有限公司股-境外自然人股");
        mapShareTypeCN.put("66","有限公司股-境内其它机构");
        mapShareTypeCN.put("67","有限公司股-境外其它机构");
        mapShareTypeCN.put("68","有限公司股-高管股");
        mapShareTypeCN.put("69","有限公司股-其它");
        mapShareTypeCN.put("9","其他类型");
        mapShareTypeCN.put("91","股份合作制");
        mapShareTypeCN.put("92","退托管股-记录信息无法律效力");
        mapShareTypeCN.put("93","退托管股-记录信息无法律效力-未缴");
        mapShareTypeCN.put("94","退托管股-记录信息无法律效力-承诺人");
        mapShareTypeCN.put("99","其他");


        return mapShareTypeCN;
    }
    public boolean mapCompare(Map<String, Object> map1,Map<String, Object> map2) {
        boolean isChange = false;
        for (Map.Entry<String, Object> entry1 : map1.entrySet()) {
            Object m1value = entry1.getValue() == null ? "" : entry1.getValue();
            Object m2value = map2.get(entry1.getKey()) == null ? "" : map2.get(entry1.getKey());
            if (!m1value.equals(m2value)) {
                isChange = true;
            }
        }
        return isChange;
    }


    //构造检查数据
    public Map contructRegisterInfo(String TxId,int checkSize){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        com.alibaba.fastjson.JSONObject jobjOK = null;

        //检查交易及登记array size
        assertEquals(checkSize,jsonArray2.size());
        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("登记")){
                jobjOK = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objRefList = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("名册登记");
        com.alibaba.fastjson.JSONObject objRegRig = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("权利登记");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("登记对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记对象标识"));
        getSubjectInfo.put("登记类型",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记类型"));

        getSubjectInfo.put("登记流水号",objRegRig.getJSONObject("权利基本信息").getString("登记流水号"));
        getSubjectInfo.put("登记时间",objRegRig.getJSONObject("权利基本信息").getString("登记时间"));
        getSubjectInfo.put("登记主体引用",objRegRig.getJSONObject("权利基本信息").getString("登记主体引用"));
        getSubjectInfo.put("登记主体类型",objRegRig.getJSONObject("权利基本信息").getString("登记主体类型"));
        getSubjectInfo.put("权利登记单位",objRegRig.getJSONObject("权利基本信息").getString("权利登记单位"));
        getSubjectInfo.put("登记币种",objRegRig.getJSONObject("权利基本信息").getString("登记币种"));
        getSubjectInfo.put("变动额",objRegRig.getJSONObject("权利基本信息").getString("变动额"));
        getSubjectInfo.put("当前可用余额",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额"));
        getSubjectInfo.put("当前可用余额占比",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额占比"));
        getSubjectInfo.put("质押变动额",objRegRig.getJSONObject("权利基本信息").getString("质押变动额"));
        getSubjectInfo.put("当前质押余额",objRegRig.getJSONObject("权利基本信息").getString("当前质押余额"));
        getSubjectInfo.put("冻结变动额",objRegRig.getJSONObject("权利基本信息").getString("冻结变动额"));
        getSubjectInfo.put("当前冻结余额",objRegRig.getJSONObject("权利基本信息").getString("当前冻结余额"));
        getSubjectInfo.put("持有状态",objRegRig.getJSONObject("权利基本信息").getString("持有状态"));
        getSubjectInfo.put("持有属性",objRegRig.getJSONObject("权利基本信息").getString("持有属性"));
        getSubjectInfo.put("来源类型",objRegRig.getJSONObject("权利基本信息").getString("来源类型"));
        getSubjectInfo.put("登记说明",objRegRig.getJSONObject("权利基本信息").getString("登记说明"));
        getSubjectInfo.put("登记核验凭证",com.alibaba.fastjson.JSONObject.parseArray(
                objRegRig.getJSONObject("权利基本信息").getJSONArray("登记核验凭证").toJSONString(), String.class));
        getSubjectInfo.put("登记产品类型",objRegRig.getJSONObject("产品登记").getString("登记产品类型"));
        getSubjectInfo.put("登记产品引用",objRegRig.getJSONObject("产品登记").getString("登记产品引用"));
        getSubjectInfo.put("权利人账户引用",objRegRig.getJSONObject("产品登记").getString("权利人账户引用"));
        getSubjectInfo.put("交易报告引用",objRegRig.getJSONObject("产品登记").getString("交易报告引用"));

        getSubjectInfo.put("名册主体引用",objRefList.getJSONObject("名册基本信息").getString("名册主体引用"));
        getSubjectInfo.put("权利类型",objRefList.getJSONObject("名册基本信息").getString("权利类型"));
        getSubjectInfo.put("登记日期",objRefList.getJSONObject("名册基本信息").getString("登记日期"));

        getSubjectInfo.put("股东主体引用",objRefList.getJSONObject("股东名册").getString("股东主体引用"));
        getSubjectInfo.put("股东主体类型",objRefList.getJSONObject("股东名册").getString("股东主体类型"));
        getSubjectInfo.put("股份性质",objRefList.getJSONObject("股东名册").getString("股份性质"));
        getSubjectInfo.put("认缴金额",objRefList.getJSONObject("股东名册").getString("认缴金额"));
        getSubjectInfo.put("实缴金额",objRefList.getJSONObject("股东名册").getString("实缴金额"));
        getSubjectInfo.put("持股比例",objRefList.getJSONObject("股东名册").getString("持股比例"));

        getSubjectInfo.put("债权人主体引用",objRefList.getJSONObject("债权人名册").getString("债权人主体引用"));
        getSubjectInfo.put("债权人类型",objRefList.getJSONObject("债权人名册").getString("债权人类型"));
        getSubjectInfo.put("认购数量",objRefList.getJSONObject("债权人名册").getString("认购数量"));
        getSubjectInfo.put("认购金额",objRefList.getJSONObject("债权人名册").getString("认购金额"));
        getSubjectInfo.put("债权人联系方式",objRefList.getJSONObject("债权人名册").getString("债权人联系方式"));

        return getSubjectInfo;
    }

    //构造检查数据
    public Map contructOneRegisterInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONObject jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);

        com.alibaba.fastjson.JSONObject objRefList = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("名册登记");
        com.alibaba.fastjson.JSONObject objRegRig = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("权利登记");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("登记对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记对象标识"));
        getSubjectInfo.put("登记类型",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记类型"));

        getSubjectInfo.put("登记流水号",objRegRig.getJSONObject("权利基本信息").getString("登记流水号"));
        getSubjectInfo.put("登记时间",objRegRig.getJSONObject("权利基本信息").getString("登记时间"));
        getSubjectInfo.put("登记主体引用",objRegRig.getJSONObject("权利基本信息").getString("登记主体引用"));
        getSubjectInfo.put("登记主体类型",objRegRig.getJSONObject("权利基本信息").getString("登记主体类型"));
        getSubjectInfo.put("权利登记单位",objRegRig.getJSONObject("权利基本信息").getString("权利登记单位"));
        getSubjectInfo.put("登记币种",objRegRig.getJSONObject("权利基本信息").getString("登记币种"));
        getSubjectInfo.put("变动额",objRegRig.getJSONObject("权利基本信息").getString("变动额"));
        getSubjectInfo.put("当前可用余额",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额"));
        getSubjectInfo.put("当前可用余额占比",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额占比"));
        getSubjectInfo.put("质押变动额",objRegRig.getJSONObject("权利基本信息").getString("质押变动额"));
        getSubjectInfo.put("当前质押余额",objRegRig.getJSONObject("权利基本信息").getString("当前质押余额"));
        getSubjectInfo.put("冻结变动额",objRegRig.getJSONObject("权利基本信息").getString("冻结变动额"));
        getSubjectInfo.put("当前冻结余额",objRegRig.getJSONObject("权利基本信息").getString("当前冻结余额"));
        getSubjectInfo.put("持有状态",objRegRig.getJSONObject("权利基本信息").getString("持有状态"));
        getSubjectInfo.put("持有属性",objRegRig.getJSONObject("权利基本信息").getString("持有属性"));
        getSubjectInfo.put("来源类型",objRegRig.getJSONObject("权利基本信息").getString("来源类型"));
        getSubjectInfo.put("登记说明",objRegRig.getJSONObject("权利基本信息").getString("登记说明"));
        getSubjectInfo.put("登记核验凭证",com.alibaba.fastjson.JSONObject.parseArray(
                objRegRig.getJSONObject("权利基本信息").getJSONArray("登记核验凭证").toJSONString(), String.class));
        getSubjectInfo.put("登记产品类型",objRegRig.getJSONObject("产品登记").getString("登记产品类型"));
        getSubjectInfo.put("登记产品引用",objRegRig.getJSONObject("产品登记").getString("登记产品引用"));
        getSubjectInfo.put("权利人账户引用",objRegRig.getJSONObject("产品登记").getString("权利人账户引用"));
        getSubjectInfo.put("交易报告引用",objRegRig.getJSONObject("产品登记").getString("交易报告引用"));

        getSubjectInfo.put("名册主体引用",objRefList.getJSONObject("名册基本信息").getString("名册主体引用"));
        getSubjectInfo.put("权利类型",objRefList.getJSONObject("名册基本信息").getString("权利类型"));
        getSubjectInfo.put("登记日期",objRefList.getJSONObject("名册基本信息").getString("登记日期"));

        getSubjectInfo.put("股东主体引用",objRefList.getJSONObject("股东名册").getString("股东主体引用"));
        getSubjectInfo.put("股东主体类型",objRefList.getJSONObject("股东名册").getString("股东主体类型"));
        getSubjectInfo.put("股份性质",objRefList.getJSONObject("股东名册").getString("股份性质"));
        getSubjectInfo.put("认缴金额",objRefList.getJSONObject("股东名册").getString("认缴金额"));
        getSubjectInfo.put("实缴金额",objRefList.getJSONObject("股东名册").getString("实缴金额"));
        getSubjectInfo.put("持股比例",objRefList.getJSONObject("股东名册").getString("持股比例"));

        getSubjectInfo.put("债权人主体引用",objRefList.getJSONObject("债权人名册").getString("债权人主体引用"));
        getSubjectInfo.put("债权人类型",objRefList.getJSONObject("债权人名册").getString("债权人类型"));
        getSubjectInfo.put("认购数量",objRefList.getJSONObject("债权人名册").getString("认购数量"));
        getSubjectInfo.put("认购金额",objRefList.getJSONObject("债权人名册").getString("认购金额"));
        getSubjectInfo.put("债权人联系方式",objRefList.getJSONObject("债权人名册").getString("债权人联系方式"));

        return getSubjectInfo;
    }


    public Map contructTxInfo(String TxId,int checkSize){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        com.alibaba.fastjson.JSONObject jobjOK = null;

        //检查交易及登记array size
        assertEquals(checkSize,jsonArray2.size());
        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("交易报告")){
                jobjOK = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("交易报告信息").getJSONObject("交易基本信息");
        com.alibaba.fastjson.JSONObject objDeal = jobjOK.getJSONObject("body").getJSONObject("交易报告信息").getJSONObject("交易成交信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("交易对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("交易对象标识"));

        getSubjectInfo.put("交易产品引用",objBase.getString("交易产品引用"));
        getSubjectInfo.put("产品名称",objBase.getString("产品名称"));
        getSubjectInfo.put("交易类型",objBase.getString("交易类型"));
        getSubjectInfo.put("交易场所",objBase.getString("交易场所"));
        getSubjectInfo.put("交易描述信息",objBase.getString("交易描述信息"));

        getSubjectInfo.put("交易成交流水号",objDeal.getJSONObject("成交内容信息").getString("交易成交流水号"));
        getSubjectInfo.put("成交方式",objDeal.getJSONObject("成交内容信息").getString("成交方式"));
        getSubjectInfo.put("成交币种",objDeal.getJSONObject("成交内容信息").getString("成交币种"));
        getSubjectInfo.put("成交价格",objDeal.getJSONObject("成交内容信息").getString("成交价格"));
        getSubjectInfo.put("成交数量",objDeal.getJSONObject("成交内容信息").getString("成交数量"));
        getSubjectInfo.put("成交时间",objDeal.getJSONObject("成交内容信息").getString("成交时间"));
        getSubjectInfo.put("交易成交描述信息",objDeal.getJSONObject("成交内容信息").getString("交易成交描述信息"));

        getSubjectInfo.put("发行方主体引用",objDeal.getJSONObject("融资类交易成交方信息").getString("发行方主体引用"));
        getSubjectInfo.put("发行方名称",objDeal.getJSONObject("融资类交易成交方信息").getString("发行方名称"));
        getSubjectInfo.put("投资方主体引用",objDeal.getJSONObject("融资类交易成交方信息").getString("投资方主体引用"));
        getSubjectInfo.put("投资方名称",objDeal.getJSONObject("融资类交易成交方信息").getString("投资方名称"));

        getSubjectInfo.put("原持有方主体引用",objDeal.getJSONObject("交易成交方信息").getString("原持有方主体引用"));
        getSubjectInfo.put("原持有方名称",objDeal.getJSONObject("交易成交方信息").getString("原持有方名称"));
        getSubjectInfo.put("对手方主体引用",objDeal.getJSONObject("交易成交方信息").getString("对手方主体引用"));
        getSubjectInfo.put("对手方名称",objDeal.getJSONObject("交易成交方信息").getString("对手方名称"));

        getSubjectInfo.put("委托核验凭证",objDeal.getJSONObject("成交核验信息").getString("委托核验凭证"));
        getSubjectInfo.put("成交核验凭证",objDeal.getJSONObject("成交核验信息").getString("成交核验凭证"));

        getSubjectInfo.put("交易中介信息",com.alibaba.fastjson.JSONObject.parseArray(
                jobjOK.getJSONObject("body").getJSONObject("交易报告信息").getJSONArray("交易中介信息").toJSONString(), Map.class));

        return getSubjectInfo;
    }

    public Map contructSettleInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);

        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("资金结算信息").getJSONObject("资金结算基本信息");
        com.alibaba.fastjson.JSONObject objIn = jobjOK.getJSONObject("body").getJSONObject("资金结算信息").getJSONObject("转入方信息");
        com.alibaba.fastjson.JSONObject objOut = jobjOK.getJSONObject("body").getJSONObject("资金结算信息").getJSONObject("转出方信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("资金结算对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("资金结算对象标识"));

        getSubjectInfo.put("结算机构主体引用",objBase.getString("结算机构主体引用"));
        getSubjectInfo.put("结算类型",objBase.getString("结算类型"));
        getSubjectInfo.put("结算流水号",objBase.getString("结算流水号"));
        getSubjectInfo.put("结算时间",objBase.getString("结算时间"));
        getSubjectInfo.put("交易报告引用",objBase.getString("交易报告引用"));
        getSubjectInfo.put("结算币种",objBase.getString("结算币种"));
        getSubjectInfo.put("结算金额",objBase.getString("结算金额"));
        getSubjectInfo.put("结算说明",objBase.getString("结算说明"));
        getSubjectInfo.put("结算操作凭证",objBase.getString("结算操作凭证"));

        getSubjectInfo.put("转出方银行代号",objOut.getString("转出方银行代号"));
        getSubjectInfo.put("转出方银行名称",objOut.getString("转出方银行名称"));
        getSubjectInfo.put("转出方银行账号",objOut.getString("转出方银行账号"));
        getSubjectInfo.put("转出方账户引用",objOut.getString("转出方账户引用"));
        getSubjectInfo.put("转出方账户名称",objOut.getString("转出方账户名称"));
        getSubjectInfo.put("转出方发生前金额",objOut.getString("转出方发生前金额"));
        getSubjectInfo.put("转出方发生后余额",objOut.getString("转出方发生后余额"));

        getSubjectInfo.put("转入方银行代号",objIn.getString("转入方银行代号"));
        getSubjectInfo.put("转入方银行名称",objIn.getString("转入方银行名称"));
        getSubjectInfo.put("转入方银行账号",objIn.getString("转入方银行账号"));
        getSubjectInfo.put("转入方账户引用",objIn.getString("转入方账户引用"));
        getSubjectInfo.put("转入方账户名称",objIn.getString("转入方账户名称"));
        getSubjectInfo.put("转入方资金账号",objIn.getString("转入方资金账号"));
        getSubjectInfo.put("转入方发生前金额",objIn.getString("转入方发生前金额"));
        getSubjectInfo.put("转入方发生后余额",objIn.getString("转入方发生后余额"));

        return getSubjectInfo;
    }

    public Map contructPublishInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONObject jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);

        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("信批信息").getJSONObject("信批基本信息");
        com.alibaba.fastjson.JSONObject objAccount = jobjOK.getJSONObject("body").getJSONObject("信批信息").getJSONObject("财务信息");
        com.alibaba.fastjson.JSONObject objKeyEvent = jobjOK.getJSONObject("body").getJSONObject("信批信息").getJSONObject("重大时间信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("信批对象标识",jobjOK.getJSONObject("body").getJSONObject("对象标识").getString("信批对象标识"));

        getSubjectInfo.put("信批主体引用", objBase.getString("信批主体引用"));

        getSubjectInfo.put("期间起始日期", objAccount.getJSONObject("基本财务信息").getString("期间起始日期"));
        getSubjectInfo.put("截止日期", objAccount.getJSONObject("基本财务信息").getString("截止日期"));
        getSubjectInfo.put("报表类型", objAccount.getJSONObject("基本财务信息").getString("报表类型"));
        getSubjectInfo.put("期末总资产(元)", objAccount.getJSONObject("基本财务信息").getString("期末总资产(元)"));
        getSubjectInfo.put("期末净资产(元)", objAccount.getJSONObject("基本财务信息").getString("期末净资产(元)"));
        getSubjectInfo.put("总负债(元)", objAccount.getJSONObject("基本财务信息").getString("总负债(元)"));
        getSubjectInfo.put("本期营业收入(元)", objAccount.getJSONObject("基本财务信息").getString("本期营业收入(元)"));
        getSubjectInfo.put("本期利润总额（元）", objAccount.getJSONObject("基本财务信息").getString("本期利润总额（元）"));
        getSubjectInfo.put("本期净利润（元）",objAccount.getJSONObject("基本财务信息").getString("本期净利润（元）"));
        getSubjectInfo.put("现金流量（元）",objAccount.getJSONObject("基本财务信息").getString("现金流量（元）"));
        getSubjectInfo.put("是否有研发费用",objAccount.getJSONObject("基本财务信息").getString("是否有研发费用"));
        getSubjectInfo.put("研发费用（元）",objAccount.getJSONObject("基本财务信息").getString("研发费用（元）"));

        getSubjectInfo.put("资产负债表(PDF)", objAccount.getJSONObject("财务报表文件").getString("资产负债表(PDF)"));
        getSubjectInfo.put("现金流量表(PDF)", objAccount.getJSONObject("财务报表文件").getString("现金流量表(PDF)"));
        getSubjectInfo.put("利润表(PDF)", objAccount.getJSONObject("财务报表文件").getString("利润表(PDF)"));

        getSubjectInfo.put("重大事件类型", objKeyEvent.getJSONObject("事件类型").getString("重大事件类型"));
        getSubjectInfo.put("文件列表", com.alibaba.fastjson.JSONObject.parseArray(
                objKeyEvent.getJSONObject("文件").getJSONArray("文件列表").toJSONString(), String.class));
        getSubjectInfo.put("提报时间", objKeyEvent.getJSONObject("提报时间").getString("提报时间"));

        return getSubjectInfo;
    }

    public Map contructEnterpriseSubInfo(String subTxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(subTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);
        com.alibaba.fastjson.JSONObject jobj2 = null;

        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("主体")){
                jobj2 = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objSubBase = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        com.alibaba.fastjson.JSONObject objEnterpriseSub = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("机构主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",jobj2.getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));
        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));
        getSubjectInfo.put("主体资质信息", com.alibaba.fastjson.JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class));

        getSubjectInfo.put("机构类型",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构类型"));
        getSubjectInfo.put("机构性质",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构性质"));

        getSubjectInfo.put("公司全称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司全称"));
        getSubjectInfo.put("英文名称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文名称"));
        getSubjectInfo.put("公司简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简称"));
        getSubjectInfo.put("英文简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文简称"));
        getSubjectInfo.put("企业类型",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业类型"));
        getSubjectInfo.put("企业成分",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业成分"));
        getSubjectInfo.put("统一社会信用代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("统一社会信用代码"));
        getSubjectInfo.put("组织机构代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("组织机构代码"));
        getSubjectInfo.put("设立日期",objEnterpriseSub.getJSONObject("企业基本信息").getString("设立日期"));
        getSubjectInfo.put("营业执照",objEnterpriseSub.getJSONObject("企业基本信息").getString("营业执照"));
        getSubjectInfo.put("经营范围",objEnterpriseSub.getJSONObject("企业基本信息").getString("经营范围"));
        getSubjectInfo.put("企业所属行业",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业所属行业"));
        getSubjectInfo.put("主营业务",objEnterpriseSub.getJSONObject("企业基本信息").getString("主营业务"));
        getSubjectInfo.put("公司简介",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简介"));
        getSubjectInfo.put("注册资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册资本"));
        getSubjectInfo.put("注册资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("注册资本币种"));
        getSubjectInfo.put("实收资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("实收资本"));
        getSubjectInfo.put("实收资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("实收资本币种"));
        getSubjectInfo.put("注册地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册地址"));
        getSubjectInfo.put("办公地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("办公地址"));
        getSubjectInfo.put("联系地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系地址"));
        getSubjectInfo.put("联系电话",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系电话"));
        getSubjectInfo.put("传真",objEnterpriseSub.getJSONObject("企业基本信息").getString("传真"));
        getSubjectInfo.put("邮政编码",objEnterpriseSub.getJSONObject("企业基本信息").getString("邮政编码"));
        getSubjectInfo.put("互联网地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("互联网地址"));
        getSubjectInfo.put("电子邮箱",objEnterpriseSub.getJSONObject("企业基本信息").getString("电子邮箱"));
        getSubjectInfo.put("公司章程",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司章程"));
        getSubjectInfo.put("主管单位",objEnterpriseSub.getJSONObject("企业基本信息").getString("主管单位"));
        getSubjectInfo.put("股东总数（个）",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("股东总数（个）"));
        getSubjectInfo.put("股本总数(股)",objEnterpriseSub.getJSONObject("企业基本信息").getString("股本总数(股)"));
        getSubjectInfo.put("法定代表人姓名",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人姓名"));
        getSubjectInfo.put("法人性质",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法人性质"));
        getSubjectInfo.put("法定代表人身份证件类型",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人身份证件类型"));
        getSubjectInfo.put("法定代表人身份证件号码",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人身份证件号码"));
        getSubjectInfo.put("法定代表人职务",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人职务"));
        getSubjectInfo.put("法定代表人手机号",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人手机号"));

        return getSubjectInfo;
    }


    public Map contructPersonalSubInfo(String personalTxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(personalTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        com.alibaba.fastjson.JSONObject jobj2 = null;

        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("主体")){
                jobj2 = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objSubBase = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        com.alibaba.fastjson.JSONObject objPersonalSub = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("个人主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",jobj2.getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));

        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));

        getSubjectInfo.put("主体资质信息", com.alibaba.fastjson.JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class));

        getSubjectInfo.put("个人姓名",objPersonalSub.getJSONObject("个人主体基本信息").get("个人姓名"));
        getSubjectInfo.put("个人身份证类型",objPersonalSub.getJSONObject("个人主体基本信息").get("个人身份证类型"));
        getSubjectInfo.put("个人身份证件号",objPersonalSub.getJSONObject("个人主体基本信息").get("个人身份证件号"));
        getSubjectInfo.put("个人联系地址",objPersonalSub.getJSONObject("个人主体基本信息").get("个人联系地址"));
        getSubjectInfo.put("个人联系电话",objPersonalSub.getJSONObject("个人主体基本信息").get("个人联系电话"));
        getSubjectInfo.put("个人手机号",objPersonalSub.getJSONObject("个人主体基本信息").get("个人手机号"));
        getSubjectInfo.put("学历",objPersonalSub.getJSONObject("个人主体基本信息").get("学历"));
        getSubjectInfo.put("个人所属行业",objPersonalSub.getJSONObject("个人主体基本信息").get("个人所属行业"));
        getSubjectInfo.put("出生日期",objPersonalSub.getJSONObject("个人主体基本信息").get("出生日期"));
        getSubjectInfo.put("性别",objPersonalSub.getJSONObject("个人主体基本信息").get("性别"));

        getSubjectInfo.put("评级结果",objPersonalSub.getJSONObject("个人主体风险评级").get("评级结果"));
        getSubjectInfo.put("评级时间",objPersonalSub.getJSONObject("个人主体风险评级").get("评级时间"));
        getSubjectInfo.put("评级原始记录",objPersonalSub.getJSONObject("个人主体风险评级").get("评级原始记录"));

        return getSubjectInfo;
    }


    public Map contructFundAccountInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        com.alibaba.fastjson.JSONObject jobjOK = null;

        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("账户") &&
                    (objTemp.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息").getIntValue("账户类型") == 1)){
                jobjOK = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objAccbase = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息");
        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户关联信息");
        com.alibaba.fastjson.JSONObject objAccLife = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户生命周期信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("账户对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("账户对象标识"));

        getSubjectInfo.put("账户所属主体引用",objAccbase.getString("账户所属主体引用"));
        getSubjectInfo.put("开户机构主体引用",objAccbase.getString("开户机构主体引用"));
        getSubjectInfo.put("账号",objAccbase.getString("账号"));
        getSubjectInfo.put("账户类型",objAccbase.getString("账户类型"));  //默认股权账户
        getSubjectInfo.put("账户用途",objAccbase.getString("账户用途"));
        getSubjectInfo.put("账号状态",objAccbase.getString("账号状态"));

        getSubjectInfo.put("账户开户时间",objAccLife.getJSONObject("开户信息").getString("账户开户时间"));
        getSubjectInfo.put("账户开户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("开户信息").getJSONArray("账户开户核验凭证").toJSONString(), String.class));

        getSubjectInfo.put("账户销户时间",objAccLife.getJSONObject("销户信息").getString("账户销户时间"));
        getSubjectInfo.put("账户销户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("销户信息").getJSONArray("账户销户核验凭证").toJSONString(), String.class));

        getSubjectInfo.put("账户冻结时间",objAccLife.getJSONObject("冻结信息").getString("账户冻结时间"));
        getSubjectInfo.put("账户冻结核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("冻结信息").getJSONArray("账户冻结核验凭证").toJSONString(), String.class));

        getSubjectInfo.put("账户解冻时间",objAccLife.getJSONObject("解冻信息").getString("账户解冻时间"));
        getSubjectInfo.put("账户解冻核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("解冻信息").getJSONArray("账户解冻核验凭证").toJSONString(), String.class));


        getSubjectInfo.put("关联关系",objAccRela.getString("关联关系"));
        getSubjectInfo.put("关联账户对象引用",objAccRela.getString("关联账户对象引用"));
        getSubjectInfo.put("关联账户开户文件", com.alibaba.fastjson.JSONObject.parseArray(
                objAccRela.getJSONArray("关联账户开户文件").toJSONString(), String.class));

        return getSubjectInfo;
    }

    public Map contructEquityAccountInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        com.alibaba.fastjson.JSONObject jobjOK = null;

        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("账户") &&
                    (objTemp.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息").getIntValue("账户类型") == 0)){
                jobjOK = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objAccbase = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息");
        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户关联信息");
        com.alibaba.fastjson.JSONObject objAccLife = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户生命周期信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("账户对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("账户对象标识"));

        getSubjectInfo.put("账户所属主体引用",objAccbase.getString("账户所属主体引用"));
        getSubjectInfo.put("开户机构主体引用",objAccbase.getString("开户机构主体引用"));
        getSubjectInfo.put("账号",objAccbase.getString("账号"));
        getSubjectInfo.put("账户类型",objAccbase.getString("账户类型"));  //默认股权账户
        getSubjectInfo.put("账户用途",objAccbase.getString("账户用途"));
        getSubjectInfo.put("账号状态",objAccbase.getString("账号状态"));

        getSubjectInfo.put("账户开户时间",objAccLife.getJSONObject("开户信息").getString("账户开户时间"));
        getSubjectInfo.put("账户开户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("开户信息").getJSONArray("账户开户核验凭证").toJSONString(), String.class));

        getSubjectInfo.put("账户销户时间",objAccLife.getJSONObject("销户信息").getString("账户销户时间"));
        getSubjectInfo.put("账户销户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("销户信息").getJSONArray("账户销户核验凭证").toJSONString(), String.class));

        getSubjectInfo.put("账户冻结时间",objAccLife.getJSONObject("冻结信息").getString("账户冻结时间"));
        getSubjectInfo.put("账户冻结核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("冻结信息").getJSONArray("账户冻结核验凭证").toJSONString(), String.class));

        getSubjectInfo.put("账户解冻时间",objAccLife.getJSONObject("解冻信息").getString("账户解冻时间"));
        getSubjectInfo.put("账户解冻核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
                objAccLife.getJSONObject("解冻信息").getJSONArray("账户解冻核验凭证").toJSONString(), String.class));


        getSubjectInfo.put("关联关系",objAccRela.getString("关联关系"));
        getSubjectInfo.put("关联账户对象引用",objAccRela.getString("关联账户对象引用"));
        getSubjectInfo.put("关联账户开户文件", com.alibaba.fastjson.JSONObject.parseArray(
                objAccRela.getJSONArray("关联账户开户文件").toJSONString(), String.class));

        return getSubjectInfo;
    }


    public Map contructEquityProdInfo(String prodTxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        com.alibaba.fastjson.JSONObject jobj2 = null;
        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("产品")){
                jobj2 = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objProdBase = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品基本信息");
        com.alibaba.fastjson.JSONObject objProdIssue = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品发行信息");

        Map getSubjectInfo = new HashMap();

        getSubjectInfo.put("产品对象标识",jobj2.getJSONObject("body").getJSONObject("对象标识").getString("产品对象标识"));
        getSubjectInfo.put("发行主体引用",objProdBase.getString("发行主体引用"));
        getSubjectInfo.put("发行主体名称",objProdBase.getString("发行主体名称"));
        getSubjectInfo.put("登记机构主体引用",objProdBase.getString("登记机构主体引用"));
        getSubjectInfo.put("托管机构主体引用",objProdBase.getString("托管机构主体引用"));
        getSubjectInfo.put("产品代码",objProdBase.getString("产品代码"));
        getSubjectInfo.put("产品全称",objProdBase.getString("产品全称"));
        getSubjectInfo.put("产品简称",objProdBase.getString("产品简称"));
        getSubjectInfo.put("产品类型",objProdBase.getString("产品类型"));
        getSubjectInfo.put("最大账户数量",objProdBase.getString("最大账户数量"));
        getSubjectInfo.put("信息披露方式",objProdBase.getString("信息披露方式"));
        getSubjectInfo.put("产品规模单位",objProdBase.getString("产品规模单位"));
        getSubjectInfo.put("产品规模币种",objProdBase.getString("产品规模币种"));
        getSubjectInfo.put("产品规模总额",objProdBase.getString("产品规模总额"));
        getSubjectInfo.put("浏览范围",objProdBase.getString("浏览范围"));
        getSubjectInfo.put("交易范围",objProdBase.getString("交易范围"));

        getSubjectInfo.put("承销机构主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构主体引用"));
        getSubjectInfo.put("承销机构名称",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构名称"));
        getSubjectInfo.put("律师事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所主体引用"));
        getSubjectInfo.put("律师事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所名称"));
        getSubjectInfo.put("会计事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所主体引用"));
        getSubjectInfo.put("会计事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所名称"));

        getSubjectInfo.put("发行方联系人",objProdIssue.getJSONObject("联系信息").getString("发行方联系人"));
        getSubjectInfo.put("发行方联系信息",objProdIssue.getJSONObject("联系信息").getString("发行方联系信息"));

        getSubjectInfo.put("股权类-发行增资信息", com.alibaba.fastjson.JSONObject.parseArray(objProdIssue.getJSONArray("股权类-发行增资信息").toJSONString(), Map.class));

        return getSubjectInfo;
    }

    public Map contructBondProdInfo(String prodTxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        com.alibaba.fastjson.JSONObject jobj2 = null;
        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("产品")){
                jobj2 = objTemp;
                break;
            }
        }
        com.alibaba.fastjson.JSONObject objProdBase = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品基本信息");
        com.alibaba.fastjson.JSONObject objProdIssue = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品发行信息");

        Map getSubjectInfo = new HashMap();

        getSubjectInfo.put("产品对象标识",jobj2.getJSONObject("body").getJSONObject("对象标识").getString("产品对象标识"));

        getSubjectInfo.put("发行主体引用",objProdBase.getString("发行主体引用"));
        getSubjectInfo.put("发行主体名称",objProdBase.getString("发行主体名称"));
        getSubjectInfo.put("登记机构主体引用",objProdBase.getString("登记机构主体引用"));
        getSubjectInfo.put("托管机构主体引用",objProdBase.getString("托管机构主体引用"));
        getSubjectInfo.put("产品代码",objProdBase.getString("产品代码"));
        getSubjectInfo.put("产品全称",objProdBase.getString("产品全称"));
        getSubjectInfo.put("产品简称",objProdBase.getString("产品简称"));
        getSubjectInfo.put("产品类型",objProdBase.getString("产品类型"));
        getSubjectInfo.put("最大账户数量",objProdBase.getString("最大账户数量"));
        getSubjectInfo.put("信息披露方式",objProdBase.getString("信息披露方式"));
        getSubjectInfo.put("产品规模单位",objProdBase.getString("产品规模单位"));
        getSubjectInfo.put("产品规模币种",objProdBase.getString("产品规模币种"));
        getSubjectInfo.put("产品规模总额",objProdBase.getString("产品规模总额"));
        getSubjectInfo.put("浏览范围",objProdBase.getString("浏览范围"));
        getSubjectInfo.put("交易范围",objProdBase.getString("交易范围"));

        getSubjectInfo.put("承销机构主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构主体引用"));
        getSubjectInfo.put("承销机构名称",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构名称"));
        getSubjectInfo.put("律师事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所主体引用"));
        getSubjectInfo.put("律师事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所名称"));
        getSubjectInfo.put("会计事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所主体引用"));
        getSubjectInfo.put("会计事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所名称"));

        getSubjectInfo.put("发行方联系人",objProdIssue.getJSONObject("联系信息").getString("发行方联系人"));
        getSubjectInfo.put("发行方联系信息",objProdIssue.getJSONObject("联系信息").getString("发行方联系信息"));

        getSubjectInfo.put("发行代码",objProdIssue.getJSONObject("债券类-发行信息").getString("发行代码"));
        getSubjectInfo.put("存续期限",objProdIssue.getJSONObject("债券类-发行信息").getString("存续期限"));
        getSubjectInfo.put("最小账户数量",objProdIssue.getJSONObject("债券类-发行信息").getString("最小账户数量"));
        getSubjectInfo.put("产品面值",objProdIssue.getJSONObject("债券类-发行信息").getString("产品面值"));
        getSubjectInfo.put("票面利率",objProdIssue.getJSONObject("债券类-发行信息").getString("票面利率"));
        getSubjectInfo.put("利率形式",objProdIssue.getJSONObject("债券类-发行信息").getString("利率形式"));
        getSubjectInfo.put("付息频率",objProdIssue.getJSONObject("债券类-发行信息").getString("付息频率"));
        getSubjectInfo.put("非闰年计息天数",objProdIssue.getJSONObject("债券类-发行信息").getString("非闰年计息天数"));
        getSubjectInfo.put("闰年计息天数",objProdIssue.getJSONObject("债券类-发行信息").getString("闰年计息天数"));
        getSubjectInfo.put("发行价格",objProdIssue.getJSONObject("债券类-发行信息").getString("发行价格"));
        getSubjectInfo.put("选择权条款",objProdIssue.getJSONObject("债券类-发行信息").getString("选择权条款"));
        getSubjectInfo.put("（本期）发行规模上限",objProdIssue.getJSONObject("债券类-发行信息").getString("（本期）发行规模上限"));
        getSubjectInfo.put("（本期）发行规模下限",objProdIssue.getJSONObject("债券类-发行信息").getString("（本期）发行规模下限"));
        getSubjectInfo.put("发行开始日期",objProdIssue.getJSONObject("债券类-发行信息").getString("发行开始日期"));
        getSubjectInfo.put("发行结束日期",objProdIssue.getJSONObject("债券类-发行信息").getString("发行结束日期"));
        getSubjectInfo.put("登记日期",objProdIssue.getJSONObject("债券类-发行信息").getString("登记日期"));
        getSubjectInfo.put("起息日期",objProdIssue.getJSONObject("债券类-发行信息").getString("起息日期"));
        getSubjectInfo.put("到期日期",objProdIssue.getJSONObject("债券类-发行信息").getString("到期日期"));
        getSubjectInfo.put("首次付息日期",objProdIssue.getJSONObject("债券类-发行信息").getString("首次付息日期"));
        getSubjectInfo.put("发行文件编号",objProdIssue.getJSONObject("债券类-发行信息").getString("发行文件编号"));
        getSubjectInfo.put("发行文件列表", com.alibaba.fastjson.JSONObject.parseArray(
                objProdIssue.getJSONObject("债券类-发行信息").getJSONArray("发行文件列表").toJSONString(), String.class));
        getSubjectInfo.put("发行方主体信用评级",objProdIssue.getJSONObject("债券类-发行信息").getString("发行方主体信用评级"));
        getSubjectInfo.put("增信机构主体引用",objProdIssue.getJSONObject("债券类-发行信息").getString("增信机构主体引用"));
        getSubjectInfo.put("增信机构名称",objProdIssue.getJSONObject("债券类-发行信息").getString("增信机构名称"));
        getSubjectInfo.put("增信机构主体评级",objProdIssue.getJSONObject("债券类-发行信息").getString("增信机构主体评级"));
        getSubjectInfo.put("信用评级机构主体引用",objProdIssue.getJSONObject("债券类-发行信息").getString("信用评级机构主体引用"));
        getSubjectInfo.put("信用评级机构名称",objProdIssue.getJSONObject("债券类-发行信息").getString("信用评级机构名称"));
        getSubjectInfo.put("担保机构主体引用",objProdIssue.getJSONObject("债券类-发行信息").getString("担保机构主体引用"));
        getSubjectInfo.put("担保机构名称",objProdIssue.getJSONObject("债券类-发行信息").getString("担保机构名称"));
        getSubjectInfo.put("担保安排",objProdIssue.getJSONObject("债券类-发行信息").getString("担保安排"));
        getSubjectInfo.put("产品终止条件",objProdIssue.getJSONObject("债券类-发行信息").getString("产品终止条件"));

        return getSubjectInfo;
    }
}
