package com.tjfintech.common.functionTest.CreditTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.CreditBeforeCondition;
import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassCredit;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.internal.matchers.Null;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassCredit.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CreditInterfaceTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Credit credit = testBuilder.getCredit();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    UtilsClassCredit utilsClassCredit = new UtilsClassCredit();
    WVMContractTest wvm = new WVMContractTest();

    String enterprisecode = "enterprise" + utilsClass.Random(8);

    @BeforeClass
    public static void init() throws Exception {
        UtilsClassCredit utilsClassCredit = new UtilsClassCredit();
        UtilsClass utilsClass = new UtilsClass();
        if (authContractName.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            CreditBeforeCondition creditBeforeCondition = new CreditBeforeCondition();
            beforeCondition.updatePubPriKey();
            creditBeforeCondition.installZXContract();
        }
        //更新zxconfig配置文件Code和CreditdataPath
        utilsClassCredit.setZXConfig(utilsClass.getIPFromStr(SDKADD), "Common", "Code", zxCode);
        utilsClassCredit.setZXConfig(utilsClass.getIPFromStr(SDKADD), "SmartContract", "CreditdataPath", creditContractName);
        shellExeCmd(utilsClass.getIPFromStr(SDKADD), killSDKCmd, startSDKCmd); //重启sdk api
        sleepAndSaveInfo(SLEEPTIME, "等待SDK重启");
    }

    @Test
    public void creditIdentityAddTest() throws Exception {

        //公司名称为空
        String response = credit.creditIdentityAdd
                ("", zxCode, "征信机构", creditContractName, PUBKEY1, "aa", "bb");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //公司Code为空
        response = credit.creditIdentityAdd
                (zxCode, "", "征信机构", creditContractName, PUBKEY1, "aa", "bb");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //类型为空
        response = credit.creditIdentityAdd
                (zxCode, zxCode, "", creditContractName, PUBKEY1, "aa", "bb");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信数据合约地址为空
        response = credit.creditIdentityAdd
                (zxCode, zxCode, "征信机构", "", PUBKEY1, "aa", "bb");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //身份公钥为空
        response = credit.creditIdentityAdd
                (zxCode, zxCode, "征信机构", creditContractName, "", "aa", "bb");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //地址为空
        response = credit.creditIdentityAdd
                (zxCode, zxCode, "征信机构", creditContractName, PUBKEY1, "", "bb");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //额外备注或者描述
        response = credit.creditIdentityAdd
                (zxCode, zxCode, "征信机构", creditContractName, PUBKEY1, "aa", "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void creditIdentityQueryTest() throws Exception {

        //公司code为空
        String response = credit.creditIdentityQuery("");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void creditCreditdataAddTest() throws Exception {

        //征信数据列表为空
        List<Map> creditlist = null;
        String response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权公司名称为空
        creditlist = utilsClassCredit.constructCreditData("", enterprisecode, zxCode, zxCode, "hash1",
                "a", "2020-09-24-15-00-00", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权公司Code为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, "", zxCode, zxCode, "hash1",
                "a", "2020-09-24-15-00-00", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信公司名称为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, "", zxCode, "hash1",
                "a", "2020-09-24-15-00-00", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信公司ID为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, zxCode, "", "hash1",
                "a", "2020-09-24-15-00-00", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //原始征信数据摘要为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, zxCode, zxCode, "",
                "a", "2020-09-24-15-00-00", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信数据大分类信息为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, zxCode, zxCode, "hash1",
                "", "2020-09-24-15-00-00", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权开始时间为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, zxCode, zxCode, "hash1",
                "a", "", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //原始征信数据URL为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, zxCode, zxCode, "hash1",
                "a", "2020-09-24-15-00-00", "", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //额外备注或者描述为空
        creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, zxCode, zxCode, "hash1",
                "a", "2020-09-24-15-00-00", "a", "");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));


    }

    @Test
    public void creditCreditdataQueryTest() throws Exception {

        //授权公司Code
        String response = credit.creditCreditdataQuery("");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void creditAuthorizationAddTest() throws Exception {

        //节点机构列表为空
        ArrayList<String> orgid = new ArrayList<>();
        List<Map> authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        String response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权记录列表为空
        orgid.add(zxCode);
        authlist.clear();
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权公司名称为空
        authlist = utilsClassCredit.constructAuthorizationData("", enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权公司code为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, "", zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //被授权银行/机构名称为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //被授权银行/机构code为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信公司名称为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, "", zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信公司ID为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, "", "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //异地征信公司名称为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                "", zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //异地征信公司ID为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, "", "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //本地/异地授权区分为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权证照url为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权证照摘要为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权类型为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权开始时间为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权有效天数为空
        authlist.clear();
        authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 0);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));


    }

    @Test
    public void creditAuthorizationQueryTest() throws Exception {

        //添加授权返回的唯一标识符key为空
        String response = credit.creditAuthorizationQuery("");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void creditViewhistoryAddTest() throws Exception {

        //节点机构列表为空
        ArrayList<String> orgid = new ArrayList<>();
        List<Map> viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        String response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //查询记录列表为空
        orgid.add(zxCode);
        viewlist.clear();
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权公司名称为空
        viewlist = utilsClassCredit.constructViewData("", enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //授权公司code为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, "", zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信机构名称为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, "", zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //征信机构code为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, "", "ICBC", "ICBC",
                zxCode, zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //银行机构名称为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "", "ICBC",
                zxCode, zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //银行机构code为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "",
                zxCode, zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //异地征信公司名称为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                "", zxCode, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //异地征信公司code为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, "", "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //本地/异地授权区分信息为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //查询操作人员为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //查询原因为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "aa", "", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //查询时间为空
        viewlist.clear();
        viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                zxCode, zxCode, "aa", "aa", "aa", "");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));


    }

    @Test
    public void creditViewhistoryQueryTest() throws Exception {

        //添加查询记录返回的唯一标识符key为空
        String response = credit.creditViewhistoryQuery("");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

    }

}