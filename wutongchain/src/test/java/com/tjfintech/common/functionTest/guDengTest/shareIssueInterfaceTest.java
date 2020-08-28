package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
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
public class shareIssueInterfaceTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String contractAddr = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
    String platformKeyID = "bt45k19pgfltc7nnqn50";
    String companyId = "companyI1100001";
    String clientNo = "cI1100001";
    String equityCode = "SZI1100001";



    @Test
    public void shareIssueInterfaceMustParamTest() throws Exception {
        String eqCode = "ecI1600001";

        List<Map> shareList = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",5000,0);
        List<Map> shareList2 = gdConstructShareList("So6uaUagSbBcDEt935v8sdA52cQ2QFRnVx9nBoaNmzKxomxSRkn",5000,0, shareList);

        String response= gd.GDShareIssue(contractAddr,platformKeyID,eqCode,shareList2);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        log.info(" ************************ test platformKeyId must ************************ ");

        response = gd.GDShareIssue(contractAddr,"",eqCode,shareList2);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test equityCode must ************************ ");

        response = gd.GDShareIssue(contractAddr,platformKeyID,"",shareList2);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test contractAddress must ************************ ");

        response = gd.GDShareIssue("",platformKeyID,eqCode,shareList2);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test shareList must ************************ ");

        response = gd.GDShareIssue(contractAddr,platformKeyID,eqCode,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("至少传入一个股权账号信息",JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareList.address must ************************ ");
        List<Map> shareListErr1 = gdConstructShareList("",5000,0);
        response = gd.GDShareIssue(contractAddr,platformKeyID,eqCode,shareListErr1);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareList.amount 0 ************************ ");
        List<Map> shareListErr2 = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",0,0);
        response = gd.GDShareIssue(contractAddr,platformKeyID,eqCode,shareListErr2);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareList.amount must ************************ ");
        Map<String,Object> shares2 = new HashMap<>();
        shares2.put("address","SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM");
        shares2.put("shareProperty",0);

        List<Map> shareList22 = new ArrayList<>();
        shareList22.add(shares2);
        response = gd.GDShareIssue(contractAddr,platformKeyID,eqCode,shareList22);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareList.shareProperty must ************************ ");
        Map<String,Object> shares3 = new HashMap<>();
        shares3.put("address","SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM");
        shares3.put("amount",500);

        List<Map> shareList3 = new ArrayList<>();
        shareList3.add(shares3);

        response = gd.GDShareIssue(contractAddr,platformKeyID,eqCode,shareList3);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test all must ************************ ");

        response = gd.GDShareIssue("","","",null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
    }
}
