package com.tjfintech.common.functionTest.guDengTest;

import com.alibaba.fastjson.JSON;
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

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class enterpriseRegisterTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String contractAddr = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
    String platformKeyID = "bt3hichpgfltc7nnqlvg";


    @Test
    public void TC01_enterpriseRegister() throws Exception {

        Map mapBaseInfo = new HashMap();
        mapBaseInfo.put("equityCode","SZ100001");
        mapBaseInfo.put("equitySimpleName","苏州股权代码");
        mapBaseInfo.put("equityType",0);
        mapBaseInfo.put("totalShares",1000000);
        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
        mapBaseInfo.put("certificateType",0);
        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId","1598222555555");
        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
        mapBaseInfo.put("companySimpleName","苏同院");
        mapBaseInfo.put("companyENName","tjfoc");
        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
        mapBaseInfo.put("txStatus",0);
        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("industry","互联网");
        mapBaseInfo.put("enterpriseStatus",0);

        Map mapBzInfo = new HashMap();
        mapBzInfo.put("province","江苏省");
        mapBzInfo.put("city","苏州");
        mapBzInfo.put("registeredAddress","相城区");
        mapBzInfo.put("officeAddress","领寓商务");
        mapBzInfo.put("contactAddress","相城区领寓商务广场12楼");
        mapBzInfo.put("phone","051261116444");
        mapBzInfo.put("fax","051261116444");
        mapBzInfo.put("postalCode","621552");
        mapBzInfo.put("internetAddress","www.wutongchain.com");
        mapBzInfo.put("mailBox","test@wutongchain.com");
        mapBzInfo.put("registeredCapital","1000000");

        Map maplegalPersonInfo = new HashMap();

        maplegalPersonInfo.put("name","苏同院");
        maplegalPersonInfo.put("certificateType",0);
        maplegalPersonInfo.put("certificateNo","123456789684984");
        maplegalPersonInfo.put("job","test");
        maplegalPersonInfo.put("legalPersonPhone","15685297828");


        String extend = "{\"city\":\"苏州\"}";

        String response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

    }


    @Test
    public void TC01_createAccout() throws Exception {

        String clientNo = "c0000001";
        String shareHolderNo = "s0000001";

        Map mapPersonInfo = new HashMap();
        mapPersonInfo.put("clientFullName","SZ100001");
        mapPersonInfo.put("organizationType","苏州股权代码");
        mapPersonInfo.put("certificateType",0);
        mapPersonInfo.put("certificateNo","123456468123153");
        mapPersonInfo.put("certificateAddress","certificateAddress");
        mapPersonInfo.put("gender","苏州同济区块链研究");
        mapPersonInfo.put("telephone","1598222555555");
        mapPersonInfo.put("phone","1598222555555");
        mapPersonInfo.put("postalCode","1585685245666821236");
        mapPersonInfo.put("contactAddress","人民币");
        mapPersonInfo.put("mailBox","1598222555555");
        mapPersonInfo.put("fax","苏州同济区块链研究");
        mapPersonInfo.put("equityCode","苏同院");
        mapPersonInfo.put("equityAmount",5000);
        mapPersonInfo.put("shareProperty",0);

        Map mapinvestor = new HashMap();
        mapinvestor.put("salesDepartment","业务一部");
        mapinvestor.put("clientGroups","群组");
        mapinvestor.put("equityAccountNo","111111");
        mapinvestor.put("currency","人民币");
        mapinvestor.put("board","E板");
        mapinvestor.put("accountType",0);
        mapinvestor.put("accountStatus",0);
        mapinvestor.put("registrationDate","621552");
        mapinvestor.put("lastTradingDate","www.wutongchain.com");
        mapinvestor.put("closingDate","test@wutongchain.com");
        mapinvestor.put("shareholderAmount",3);

        String extend = "";

        Map mapInvestorInfo = new HashMap();

        mapInvestorInfo.put("clientName","034654");
        mapInvestorInfo.put("shareholderNo",shareHolderNo);
        mapInvestorInfo.put("fundNo","f0000001");
        mapInvestorInfo.put("clientNo",clientNo);
        mapInvestorInfo.put("extend",extend);
        mapInvestorInfo.put("personalInfo",mapPersonInfo);
        mapInvestorInfo.put("investor",mapinvestor);


        String response= gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("data").getString("TxId");
        assertEquals(clientNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("AccountList").getString("ClientNo"));
        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("AccountList").getString("ShareholderNo"));
        String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("AccountList").getString("KeyId");
        String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("AccountList").getString("Address");

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

    }




}
