package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.gdConstructShareList;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class shareTransferInterfaceTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String contractAddr = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
    String platformKeyID = "bt45k19pgfltc7nnqn50";
    String companyId = "companyI1100001";
    String clientNo = "cI1100001";
    String equityCode = "ecI0800001";

    @Before
    public void beforeIssue()throws Exception{
        String eqCode = "ecI0800001";

        List<Map> shareList = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",5000,0);
        List<Map> shareList2 = gdConstructShareList("So6uaUagSbBcDEt935v8sdA52cQ2QFRnVx9nBoaNmzKxomxSRkn",5000,0, shareList);
        List<Map> shareList3 = gdConstructShareList("Sn6KRMf6heVv55V2AWzyE4mF9n8isgshAeZJVMhuW1bG2ARsd15",5000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList("SnnswixfQNaJd9v19LPEFY4UoAmxGtmEivHn6GBnYDD8aPtyjpY",5000,0, shareList3);

        String response= gd.GDShareIssue(contractAddr,platformKeyID,eqCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

    @Test
    public void shareTransferInterfaceMustParamTest() throws Exception {
        String eqCode = "ecI0800001";

        String keyId = "bt3hd3ppgfltc7nnqlt0";
        String fromAddr = "SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM";
        double amount = 10;
        String toAddr = "SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM";
        int shareProperty = 0;
        int txType = 0;
        String orderNo = "test202008280952";
        int orderWay = 0;
        int orderType = 0;
        String price = "10000";
        String time = "20200828";
        String remark = "转账";

        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);




        log.info(" ************************ test keyId must ************************ ");
        map.put("keyId", "");
        String response= gd.GDShareTransfer(map);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.KeyId' Error:Field validation for 'KeyId' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test fromAddress must ************************ ");
        map.put("keyId", keyId);
        map.put("fromAddress", "");
        response= gd.GDShareTransfer(map);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.FromAddress' Error:Field validation for 'FromAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test amount must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
//        map.put("amount", amount); //不传入参数
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test toAddress must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", "");
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.ToAddress' Error:Field validation for 'ToAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareProperty must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);//不传入
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test equityCode must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);//不传入
        map.put("equityCode", "");
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test txType must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
//        map.put("txType", txType);//不传入
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test orderNo must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", "");
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.OrderNo' Error:Field validation for 'OrderNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test orderWay must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);    //不传入
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test orderType must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);    //不传入
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test price must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", "");
        map.put("time", time);
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.Price' Error:Field validation for 'Price' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test tradeTime must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", "");
        map.put("remark", remark);

        response= gd.GDShareTransfer(map);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.Time' Error:Field validation for 'Time' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test remark must ************************ ");

        map.clear();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", eqCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", "");

        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test all must ************************ ");

        response = gd.GDShareTransfer(null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
    }
}
