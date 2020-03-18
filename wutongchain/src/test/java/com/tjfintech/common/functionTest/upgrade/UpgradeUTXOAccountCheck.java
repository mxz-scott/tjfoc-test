package com.tjfintech.common.functionTest.upgrade;


import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpgradeUTXOAccountCheck {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    //获取所有地址账户与私钥密码信息
    JSONObject jsonObjectAddrPri = commonFunc.mapAddrInfo();



    @BeforeClass
    public static void beforeClass() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        bf.createAddresses();
//        bf.collAddressTest();
//        Thread.sleep(SLEEPTIME);
    }

//    @Test



    @Test
    public void testAccoutAfterUpgrade() throws Exception{
        //获取当前所有UTXO账户及余额
        Map<String, Object> mapAccBalance = commonFunc.getUTXOAccountBalance();


        Iterator iter = mapAccBalance.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object val = mapAccBalance.get(key);

            //获取所有地址信息
            ArrayList<String> addrList = commonFunc.collAddrList();

            if(!val.toString().equals("[]")){
                log.info("Account: " + key.toString());
                log.info("Value: " + val.toString());
                log.info(jsonObjectAddrPri.getString(key.toString()));


                String operateTokenType = "";
                String operateFromAccount = key.toString();
                String operateAmount = "";

                addrList.remove(key); //转向地址刨除自己的地址
                String operateToAddr = addrList.get(0);//刨除自己地址后的第一个地址作为转账一个转向地址

                //将对应账户的Token及余额信息取出保存到jsonArray中
                JSONArray jsonArray = JSONArray.fromObject(val);

                //判断单签多签flag 单签sign 保存时设置为0 多签则 > 0的数字
                String sign = JSONObject.fromObject(jsonObjectAddrPri.getString(operateFromAccount)).getString("sign");


//                for(int j = 0; j < jsonArray.size();j++){

                    log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
                    operateTokenType = JSONObject.fromObject(jsonArray.getString(0)).getString("tokenType");
//                operateTokenType = JSONObject.fromObject(jsonArray.getString(j)).getString("tokenType");

                    //转账前查询From账户地址tokenType余额
                    log.info("转出账户余额查询 " + operateFromAccount);
//                    operateAmount = JSONObject.fromObject(jsonArray.getString(j)).getString("value");
                    //此处不能直接使用初始获取的账户余额信息，有可能之前有其他账户转给该账户余额
                    operateAmount = commonFunc.GetBalance(operateFromAccount,operateTokenType);


                    //转账前查询To账户地址tokenType余额
                    log.info("转向账户余额查询 " + operateToAddr);
                    String toAddrBalanceBeforeTrf = commonFunc.GetBalance(operateToAddr,operateTokenType);

                    //回收前查询回收账户token余额
                    String zeroTokenBalanceBefore = JSONObject.fromObject(
                            multiSign.QueryZero(operateTokenType)).getJSONObject("Data").getString("Total");


                    Double trfAmount =Double.parseDouble(operateAmount);


                    //根据多签or单签地址组装转账list
                    List<Map> list;
                    if(sign.equals("0"))
                        list = soloSign.constructToken(operateToAddr,operateTokenType,String.valueOf(trfAmount/2));//单签转账list
                    else
                        list = utilsClass.constructToken(operateToAddr,operateTokenType,String.valueOf(trfAmount/2));//多签转账list格式

                    log.info("记录转账: " + operateFromAccount + " 向 " + operateToAddr + " token: " + operateTokenType + " * " + trfAmount/2);
                    //一半执行转账
                    commonFunc.sdkTransfer(operateFromAccount,list);
                    sleepAndSaveInfo(SLEEPTIME); //需要加sleep时间 避免转账和回收使用同一个input（当前不支持）

                    assertEquals(trfAmount/2 + Double.parseDouble(toAddrBalanceBeforeTrf),
                            Double.parseDouble(commonFunc.GetBalance(operateToAddr,operateTokenType)),0.000002);

                    //后一半token测试回收 ToAddr回收
                    commonFunc.sdkRecycle(operateFromAccount,operateTokenType,String.valueOf(Double.parseDouble(operateAmount)/2));
                    commonFunc.sdkRecycle(operateToAddr,operateTokenType,String.valueOf(Double.parseDouble(operateAmount)/2));
                    sleepAndSaveInfo(SLEEPTIME);

                    assertEquals(Double.parseDouble(toAddrBalanceBeforeTrf),
                            Double.parseDouble(commonFunc.GetBalance(operateToAddr,operateTokenType)),0.000002);
                    assertEquals(trfAmount - trfAmount/2 - trfAmount/2 ,
                            Double.parseDouble(commonFunc.GetBalance(operateFromAccount,operateTokenType)),0.000002);

                    assertEquals(Double.parseDouble(zeroTokenBalanceBefore) + trfAmount/2 + trfAmount/2,
                            Double.parseDouble(JSONObject.fromObject(
                                    multiSign.QueryZero(operateTokenType)).getJSONObject("Data").getString("Total")),
                            0.000002 );
                }

//            }
        }

    }



}
