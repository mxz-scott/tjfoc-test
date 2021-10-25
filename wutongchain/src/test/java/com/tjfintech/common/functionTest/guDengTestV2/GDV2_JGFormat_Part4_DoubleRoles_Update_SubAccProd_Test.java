package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.conJGFileName;
import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
/**
 * 多角色场景用例补充
 * 主体既可以挂牌登记 又可以开户 挂牌开户后 进行主体 账户 产品更新
 */

public class GDV2_JGFormat_Part4_DoubleRoles_Update_SubAccProd_Test {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);


    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        TestBuilder tbTemp = TestBuilder.getInstance();
        Store storeTemp =tbTemp.getStore();
        beginHeigh = Integer.parseInt(JSONObject.fromObject(storeTemp.GetHeight()).getString("data"));
        start = (new Date()).getTime();

        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
        gdEquityCode = "updateTest" + Random(12);
        bMultiRole = true;
    }

    @AfterClass
    public static void resetRole(){
        bMultiRole = false;
    }

    @Before
    public void reset()throws Exception{
        gdCompanyID = "P1Re" + Random(8);
        gdEquityCode = "updateTest" + Random(12);
    }


    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
    }

    @Test
    public void TC20_allUpdateSubjectInfo_Enterprise()throws Exception {
        gdEquityCode = "2update" + Random(12);
        //挂牌企业登记
        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        uf.regTestUnit_ThenCreateAcc("1", false);


        //更新主体信息数据 全部数据
        Map mapTemp = new HashMap();
        //-----------------主体资质信息---------------//
        List<Map> listSQI = new ArrayList<>();
        List<Map> listQAI = new ArrayList<>();
        List<Map> listISI = new ArrayList<>();
        Map mapSQI = new HashMap();

        Map mapQAI = new HashMap();
        Map mapISI = new HashMap();

        //主体基本信息 主体资质信息 资质信息
        mapSQI.put("subject_qualification_category", 1);
        mapSQI.put("subject_market_roles_type", 1);
        List<Integer> type = new ArrayList<>();
        type.add(0);
        type.add(1);
        mapSQI.put("subject_intermediary_qualification", type);
        mapSQI.put("subject_financial_qualification_type", 1);


        //主体基本信息 主体资质信息 资质认证信息
        mapQAI.put("subject_qualification_code", "资质代码2");
        mapQAI.put("subject_role_qualification_certification_doc", getListFileObj());
        mapQAI.put("subject_qualification_authenticator", "认证方2");
        mapQAI.put("subject_certification_time", time1);
        mapQAI.put("subject_qualification_reviewer", "审核方2");
        mapQAI.put("subject_review_time", time1);
        mapQAI.put("subject_qualification_status", true);
        listQAI.add(mapQAI);

        //主体基本信息 主体资质信息 投资者适当性信息
        mapISI.put("subject_investor_qualification", 1);
        mapISI.put("subject_investor_qualification_sub", "适当性认证子类2");
        mapISI.put("subject_investor_qualification_description", "适当性认证描述2");
        mapISI.put("subject_investor_qualification_certificate", getListFileObj());
        mapISI.put("subject_investor_qualification_certifier_ref", subject_investor_qualification_certifier_ref);
        mapISI.put("subject_investor_qualification_certifier_name", "适当性认证方主体名称2");
        mapISI.put("subject_investor_qualification_certificate_time", time2);
        mapISI.put("subject_investor_qualification_status", true);
        listISI.add(mapISI);

        mapSQI.put("investor_suitability_information", listISI);
        mapSQI.put("qualification_authentication_information", listQAI);


        listSQI.add(mapSQI);
        //-----------------主体资质信息---------------//


        //-----------------主体基本信息---------------//
        //对象标识
        mapTemp.put("subject_object_id", gdCompanyID);
//        mapTemp.put("subject_object_information_type",0);
//        mapTemp.put("subject_type",1);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_type", 1);
        mapTemp.put("subject_main_administrative_region", 0);
        mapTemp.put("subject_create_time", time3);

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_information", listSQI);
        //-----------------主体基本信息---------------//


        //-----------------机构主体信息---------------//
        //主体信息 机构主体信息 企业基本信息 基本信息描述
        mapTemp.put("subject_company_name", "公司全称CHARACTER2");
        mapTemp.put("subject_company_english_name", "英文名称CHARACTER2");
        mapTemp.put("subject_company_short_name", "公司简称CHARACTER2");
        mapTemp.put("subject_company_short_english_name", "英文简称CHARACTER2");
        mapTemp.put("subject_organization_nature", 0);
        mapTemp.put("subject_legal_type", 0);
        mapTemp.put("subject_economic_type", 0);
        mapTemp.put("subject_company_type", 0);
        mapTemp.put("subject_scale_type", 0);
        mapTemp.put("subject_high_technology_enterprise", 0);

        Map mapCertDoc = new HashMap();
        List<Map> listCertDoc = new ArrayList<>();
        mapCertDoc.put("type", 1);
        mapCertDoc.put("code", "1233333333");
        listCertDoc.add(mapCertDoc);

        mapTemp.put("subject_document_information", listCertDoc);
        mapTemp.put("subject_registry_date", date1);
        mapTemp.put("subject_business_license", getListFileObj());
        mapTemp.put("subject_business_scope", "经营范围CHARACTER2");
        mapTemp.put("subject_industry", 1);
        mapTemp.put("subject_company_business", "主营业务CHARACTER2");
        mapTemp.put("subject_company_profile", "公司简介TEXT2");
        mapTemp.put("subject_registered_capital", 7000000);
        mapTemp.put("subject_registered_capital_currency", "156");
        mapTemp.put("subject_paid_in_capital", 7000000);
        mapTemp.put("subject_paid_in_capital_currency", "156");
        mapTemp.put("subject_registered_address", "注册地址CHARACTER2");
        mapTemp.put("subject_province", "注册地所在省份CHARACTER2");
        mapTemp.put("subject_city", "注册地所在市CHARACTER2");
        mapTemp.put("subject_district", "注册地所在区CHARACTER2");
        mapTemp.put("subject_office_address", "办公地址CHARACTER2");
        mapTemp.put("subject_contact_address", "联系地址CHARACTER2");
        mapTemp.put("subject_contact_number", "联系电话CHARACTER2");
        mapTemp.put("subject_fax", "企业传真CHARACTER2");
        mapTemp.put("subject_postal_code", "邮政编码CHARACTER2");
        mapTemp.put("subject_internet_address", "互联网地址CHARACTER2");
        mapTemp.put("subject_mail_box", "电子邮箱CHARACTER2");
        mapTemp.put("subject_association_articles", getListFileObj());
        mapTemp.put("subject_regulator", "主管单位CHARACTER2");
        mapTemp.put("subject_shareholders_number", 15);
        mapTemp.put("subject_taxpayer_id_number", "纳税人识别号CHARACTER2");
        mapTemp.put("subject_invoice_bank", "发票开户行CHARACTER2");
        mapTemp.put("subject_invoice_account_number", "发票账号CHARACTER2");
        mapTemp.put("subject_invoice_address", "发票地址CHARACTER2");
        mapTemp.put("subject_invoice_telephone_number", "发票电话CHARACTER2");
        mapTemp.put("subject_approval_time", time3);
        mapTemp.put("subject_insured_number", 52);
        mapTemp.put("subject_company_status", 1);
        mapTemp.put("subject_company_status_deregistration", "注销原因CHARACTER2");
        mapTemp.put("subject_company_status_deregistration_date", date3);
        mapTemp.put("subject_company_status_windingup", "吊销原因CHARACTER3");
        mapTemp.put("subject_company_status_windingup_date", date3);
        List<String> name = new ArrayList<>();
        name.add("曾用名a2");
        name.add("曾用名b2");
        name.add("曾用名c2");
        mapTemp.put("subject_name_used_before", name);
        mapTemp.put("subject_personnel_size", "人员规模CHARACTER2");
        //-----------------机构主体信息---------------//


        //主体信息 机构主体信息 企业基本信息 主要人员信息
        //-----------------主要人员信息---------------//
        Map mapLMI = new HashMap();
        List<Map> listLMI = new ArrayList<>();
        mapLMI.put("subject_key_personnel_appointment_end", date1);
        mapLMI.put("subject_key_personnel_id", "证件号码CHARACTER2");

        mapLMI.put("subject_key_personnel_position", 1);
        mapLMI.put("subject_key_personnel_appointment_start", date3);
        mapLMI.put("subject_key_personnel_name", "姓名CHARACTER2");
        mapLMI.put("subject_key_personnel_nationality", "国籍CHARACTER2");
        mapLMI.put("subject_document_type", 0);
        mapLMI.put("subject_key_personnel_type", 0);
        mapLMI.put("subject_key_personnel_address", "证件地址CHARACTER2");
        mapLMI.put("subject_key_personnel_shareholding_ratio", 30);
        mapLMI.put("subject_key_personnel_shareholding", 5000);
        mapLMI.put("subject_key_personnel_contact", "联系方式CHARACTER2");
        listLMI.add(mapLMI);

        mapTemp.put("leading_member_information", listLMI);
        //-----------------主要人员信息---------------//

        //执行update操作
        String resp2 = gd.GDUpdateSubjectInfo(gdContractAddress, 0, mapTemp);
        String txId = JSONObject.fromObject(resp2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String response = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //检查uri存证交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("0", jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(false, response.contains(gdEquityCode));
        assertEquals(true, response.contains(gdCompanyID));


        //设置各个主体版本变量
        String newSubVer = gdCF.getObjectLatestVer(gdCompanyID);
        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);

        //获取链上mini url的存证信息并检查是否包含uri信息
        String subfileName = conJGFileName(gdCompanyID, newSubVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId, conJGFileName(gdCompanyID, newSubVer), 1);
        String chkSubURI = subfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
        assertEquals(true, uriInfo.get("storeData").toString().contains(chkSubURI));
        assertEquals(true, gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(gdCompanyID, newSubVer), subjectType, "1");

        //填充header content字段
        mapTemp.put("content", gdCF.constructContentTreeMap(subjectType, gdCompanyID, newSubVer, "update", String.valueOf(ts8)));

        assertEquals(String.valueOf(gdCpmIdOldVer + 3), newSubVer);

        String[] verForSub = new String[]{"/" + subSIQCRefVer};
        log.info("检查主体存证信息内容与传入一致\n" + mapTemp.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(mapTemp, subjectType, verForSub)), replaceCertain(getSubInfo.toString()));

    }

    @Test
    public void TC20_allUpdateSubjectInfo_Personal()throws Exception {

        gdEquityCode = "2update" + Random(12);
        //挂牌企业登记后开户
        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        uf.regTestUnit_ThenCreateAcc("1", false);

        String cltNo = gdCompanyID;

        //更新主体信息数据
        Map mapTemp = new HashMap();
        log.info("初始化01主体个人数据结构");
        mapTemp.clear();

        //-----------------主体资质信息start---------------//
        List<Map> listSQI = new ArrayList<>();
        List<Map> listQAI = new ArrayList<>();
        List<Map> listISI = new ArrayList<>();

        Map mapSQI = new HashMap();
        Map mapQAI = new HashMap();
        Map mapISI = new HashMap();

        //主体信息 主体基本信息 主体资质信息 资质信息
        mapSQI.put("subject_qualification_category", 2);
        mapSQI.put("subject_market_roles_type", 2);
        List<Integer> type = new ArrayList<>();
        type.add(2);
        type.add(3);
        mapSQI.put("subject_intermediary_qualification", type);
        mapSQI.put("subject_financial_qualification_type", 2);

        //主体信息 主体基本信息 主体资质信息 资质认证信息
        mapQAI.put("subject_qualification_code", "资质代码2");
        mapQAI.put("subject_role_qualification_certification_doc", getListFileObj());
        mapQAI.put("subject_qualification_authenticator", "认证方2");
        mapQAI.put("subject_certification_time", time2);
        mapQAI.put("subject_qualification_reviewer", "审核方2");
        mapQAI.put("subject_review_time", time2);
        mapQAI.put("subject_qualification_status", false);
        listQAI.add(mapQAI);

        //主体信息 主体基本信息 主体资质信息 投资者适当性信息
        mapISI.put("subject_investor_qualification", 2);
        mapISI.put("subject_investor_qualification_sub", "适当性认证子类2");
        mapISI.put("subject_investor_qualification_description", "适当性认证描述2");
        mapISI.put("subject_investor_qualification_certificate", getListFileObj());
        mapISI.put("subject_investor_qualification_certifier_ref", subject_investor_qualification_certifier_ref);
        mapISI.put("subject_investor_qualification_certifier_name", "适当性认证方主体名称2");
        mapISI.put("subject_investor_qualification_certificate_time", time2);
        mapISI.put("subject_investor_qualification_status", true);
        listISI.add(mapISI);

        mapSQI.put("investor_suitability_information", listISI);
        mapSQI.put("qualification_authentication_information", listQAI);

        listSQI.add(mapSQI);
        //-----------------主体资质信息end---------------//


        //-----------------主体基本信息start---------------//
        //对象标识
        mapTemp.put("subject_object_id", gdCompanyID);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_type", 2);
        mapTemp.put("subject_main_administrative_region", 1);
        mapTemp.put("subject_create_time", time2);

        //主体信息 主体基本信息 主体资质信息
        mapTemp.put("subject_qualification_information", listSQI);
        //-----------------主体基本信息end---------------//

        //主体信息 个人主体信息 个人主体基本信息
        mapTemp.put("subject_investor_name", "zhangsan2");
        mapTemp.put("subject_id_type", 1);
        mapTemp.put("subject_id_number", "个人身份证件号CHARACTER2");
        mapTemp.put("subject_id_address", "个人证件地址CHARACTER2");
        mapTemp.put("subject_contact_address", "个人联系地址CHARACTER2");
        mapTemp.put("subject_contact_number", "个人联系电话CHARACTER2");
        mapTemp.put("subject_cellphone_number", "个人手机号CHARACTER2");
        mapTemp.put("subject_personal_fax", "个人传真CHARACTER2");
        mapTemp.put("subject_postal_code", "邮政编码CHARACTER2");
        mapTemp.put("subject_id_doc_mailbox", "电子邮箱CHARACTER2");
        mapTemp.put("subject_education", 2);
        mapTemp.put("subject_occupation", 1);
        mapTemp.put("subject_industry", 2);
        mapTemp.put("subject_birthday", date2);
        mapTemp.put("subject_gender", 1);
        mapTemp.put("subject_work_unit", "工作单位CHARACTER2");
        mapTemp.put("subject_investment_period", "122");
        mapTemp.put("subject_investment_experience", "投资经历CHARACTER2");
        mapTemp.put("subject_native_place", "籍贯CHARACTER2");
        mapTemp.put("subject_province", "省份CHARACTER2");
        mapTemp.put("subject_city", "城市CHARACTER2");


        //执行update操作 更新个人主体信息
        String resp2 = gd.GDUpdateSubjectInfo(gdContractAddress, 1, mapTemp);
        String txId = JSONObject.fromObject(resp2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String response = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //检查uri存证交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("0", jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true, response.contains(cltNo));
        assertEquals(false, response.contains("SH" + cltNo));
        assertEquals(false, response.contains("fund" + cltNo));


        //定义相关对象标识版本变量

        String personSubVer = gdCF.getObjectLatestVer(cltNo);
        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);


        //获取链上mini url的存证信息 并检查是否包含uri信息
        String subfileName = conJGFileName(cltNo, personSubVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId, subfileName, 1);
        String chkSubURI = subfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
        assertEquals(true, uriInfo.get("storeData").toString().contains(chkSubURI));
        assertEquals(true, gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        //直接从minio上获取报送数据文件信息
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName, subjectType, "2");

        Map enSubInfo = mapTemp;

        //填充header content 信息
        enSubInfo.put("content", gdCF.constructContentTreeMap(subjectType, cltNo, personSubVer, "update", String.valueOf(ts8)));

        assertEquals(String.valueOf(gdCpmIdOldVer + 3), personSubVer);


        //需要将比较的对象标识增加版本号信息
        String[] verForSub = new String[]{"/" + subSIQCRefVer};


        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo, subjectType, verForSub)), replaceCertain(getSubInfo.toString()));

    }


    @Test
    public void TC21_allUpdateSHAccInfo_Personal()throws Exception {
        //挂牌企业登记后开户
        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        uf.regTestUnit_ThenCreateAcc("1", false);

        String cltNo = gdCompanyID;
        String shareHolderNo = "SH" + cltNo;


        //更新股权账户信息数据
        updateWord = "udEq02";
        Map mapTemp = gdBF.init02ShareholderAccountInfo();
        mapTemp.put("account_object_id", shareHolderNo);

        //账户信息 账户基本信息
        mapTemp.put("account_subject_ref", cltNo);
        mapTemp.put("account_depository_ref", account_depository_ref);

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress, cltNo, mapTemp);
        String txId = JSONObject.fromObject(upResp).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String response = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //检查uri存证交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("0", jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(1, StringUtils.countOccurrencesOf(response, "data_type"));
        assertEquals(true, response.contains("SH" + cltNo));
        assertEquals(false, response.contains("fund" + cltNo));


        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") - 1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));
        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词", true,
                gdCF.chkSensitiveWord(response, accType));

        //查询投资者账户信息
        response = gd.GDGetShareHolderInfo(gdContractAddress, cltNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


        //定义相关对象标识版本变量
        String accASrefVer = gdCF.getObjectLatestVer(account_subject_ref);
        String accADrefVer = gdCF.getObjectLatestVer(account_depository_ref);
        String accAAARefVer = gdCF.getObjectLatestVer(account_associated_account_ref);

        String shAccVer = gdCF.getObjectLatestVer("SH" + cltNo);
        String fundAccVer = gdCF.getObjectLatestVer("fund" + cltNo);
        String personSubVer = gdCF.getObjectLatestVer(cltNo);

        String SHObjId = shareHolderNo;
        //获取链上mini url的存证信息 并检查是否包含uri信息
        String shAccfileName = conJGFileName(SHObjId, shAccVer);

        //获取链上mini url的存证信息 并检查是否包含uri信息
        Map uriInfo = gdCF.getJGURIStoreHash(txId, shAccfileName, 1);
        String chkSHAccURI = shAccfileName;
        log.info(uriInfo.get("storeData").toString());
        assertEquals(true, uriInfo.get("storeData").toString().contains(chkSHAccURI));
        assertEquals(true, gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        //直接从minio上获取报送数据文件信息
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName, accType, "1");
        Map accSH = mapTemp;

        //填充header content 信息
        accSH.put("content", gdCF.constructContentTreeMap(accType, SHObjId, shAccVer, "update", String.valueOf(ts8)));

        assertEquals("确认主体版本无变更", String.valueOf(gdCpmIdOldVer + 2), personSubVer);

        //账户的如下字段默认引用的是开户主体的对象标识
        account_subject_ref = cltNo;

        //需要将比较的对象标识增加版本号信息
        String[] verForAccSH = new String[]{"/" + personSubVer, "/" + accADrefVer, "/" + accAAARefVer};

        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accSH, accType)), replaceCertain(getSHAccInfo.toString()));
    }

    @Test
    public void TC21_allUpdateFundAccInfo_Personal()throws Exception {
        //挂牌企业登记后开户
        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        uf.regTestUnit_ThenCreateAcc("1", false);

        String cltNo = gdCompanyID;
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;


        //更新股权账户信息数据
        updateWord = "udEq02";
        Map mapTemp = gdBF.init02FundAccountInfo();

        mapTemp.put("account_object_id", fundNo);

        //账户信息 账户基本信息
        mapTemp.put("account_subject_ref", cltNo);
        mapTemp.put("account_depository_ref", account_depository_ref);
        mapTemp.put("account_associated_account_ref", shareHolderNo);

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress, cltNo, mapTemp);
        String txId = JSONObject.fromObject(upResp).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String response = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


        //检查uri存证交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("0", jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(1, StringUtils.countOccurrencesOf(response, "data_type"));
        assertEquals(false, response.contains("SH" + cltNo));
        assertEquals(true, response.contains("fund" + cltNo));


        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") - 1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));
        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词", true,
                gdCF.chkSensitiveWord(response, accType));

        //查询投资者账户信息
        response = gd.GDGetShareHolderInfo(gdContractAddress, cltNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


        //定义相关对象标识版本变量
        String accASrefVer = gdCF.getObjectLatestVer(account_subject_ref);
        String accADrefVer = gdCF.getObjectLatestVer(account_depository_ref);
        String accAAARefVer = gdCF.getObjectLatestVer(account_associated_account_ref);

        String shAccVer = gdCF.getObjectLatestVer("SH" + cltNo);
        String fundAccVer = gdCF.getObjectLatestVer("fund" + cltNo);
        String personSubVer = gdCF.getObjectLatestVer(cltNo);

        String fundObjId = fundNo;

        //获取链上mini url的存证信息 并检查是否包含uri信息
        String fundAccfileName = conJGFileName(fundObjId, fundAccVer);


        //获取链上mini url的存证信息 并检查是否包含uri信息
        Map uriInfo = gdCF.getJGURIStoreHash(txId, fundAccfileName, 1);
        String chkSHAccURI = fundAccfileName;
        log.info(uriInfo.get("storeData").toString());
        assertEquals(true, uriInfo.get("storeData").toString().contains(chkSHAccURI));
        assertEquals(true, gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        //直接从minio上获取报送数据文件信息
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName, accType, "2");

        Map accFund = mapTemp;

        //填充header content 信息
        accFund.put("content", gdCF.constructContentTreeMap(accType, fundObjId, fundAccVer, "update", String.valueOf(ts8)));

        assertEquals("确认主体版本无变更", String.valueOf(gdCpmIdOldVer + 2), personSubVer);

        //账户的如下字段默认引用的是开户主体的对象标识
        account_subject_ref = cltNo;

        //需要将比较的对象标识增加版本号信息

        account_associated_account_ref = shareHolderNo;
        String[] verForAccFund = new String[]{"/" + personSubVer, "/" + accADrefVer, "/" + shAccVer};
//
        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accFund, accType)), replaceCertain(getFundAccInfo.toString()));
    }


    @Test
    public void TC22_allUpdateEquityProdAccInfo()throws Exception {
        String type = "1";
        //挂牌企业登记后开户
        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        uf.regTestUnit_ThenCreateAcc(type, false);

        //更新股权类产品信息
        updateWord = "udEq02";
        Map mapProdComInfo = gdBF.init03EquityProductInfo();
        //对象标识
        mapProdComInfo.put("product_object_id", gdEquityCode);

        String updateProd = gd.GDUpdateProductInfo(gdContractAddress, mapProdComInfo);
        assertEquals("200", JSONObject.fromObject(updateProd).getString("state"));

        String txId = JSONObject.fromObject(updateProd).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String response = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


        //检查uri存证交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("0", jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("0", jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(false, response.contains(gdCompanyID));
        assertEquals(true, response.contains(gdEquityCode));

        //查合约交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") - 1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals("不包含敏感词", true, gdCF.chkSensitiveWord(response, prodType));


        //检查产品信息是否是更新后的信息
        String queryProd = gd.GDProductQuery(gdContractAddress, gdEquityCode);


        //设置各个主体版本变量
        String newSubVer = gdCF.getObjectLatestVer(gdCompanyID);
        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        //检查产品更新不会影响主体版本变更
        assertEquals("主体版本信息版本", String.valueOf(gdCpmIdOldVer + 2), newSubVer);

        String prodPMSRefVer = gdCF.getObjectLatestVer(product_market_subject_ref);
        String prodSPSRefVer = gdCF.getObjectLatestVer(service_provider_subject_ref);

        //获取链上mini url的存证信息并检查是否包含uri信息
        String prodfileName = conJGFileName(gdEquityCode, newEqProdVer);
        String chkProdURI = prodfileName;


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getProInfo = null;
        if (!type.equals("4"))
            getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, type);

        //填充header content字段
        //如果不是机构会员登记 则执行产品填充header content字段
        if (!type.equals("4")) {
            mapProdComInfo.put("content", gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts8)));
        }

        //产品发行主体引用设置为空场景 当前代码会自动补充发行主体对象标识
//        mapProdComInfo.put("product_issuer_subject_ref", gdCompanyID);
        //产品如下字段引用的是发行主体
        product_issuer_subject_ref = gdCompanyID;

        String[] verForProd = new String[]{"/" + prodPMSRefVer, "/" + newSubVer, "/" + prodSPSRefVer};

        if (!type.equals("4")) {
            log.info("检查产品存证信息内容与传入一致\n" + mapProdComInfo.toString() + "\n" + getProInfo.toString());
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer(mapProdComInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
        }


    }

    @Test
    public void TC22_allUpdateBondProdAccInfo()throws Exception {
        String type = "2";
        //挂牌企业登记后开户
        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        uf.regTestUnit_ThenCreateAcc(type, false);

        //检查产品信息
        String queryProd = gd.GDProductQuery(gdContractAddress, gdEquityCode);

//        Map mapProd = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(queryProd).getJSONObject("data").toString());
//        Map<String, String> testMap11 = new TreeMap<String, String>(mapProd);
//        Map<String, String> testMap12 = new TreeMap<String, String>(equityProductInfo);
//        assertEquals(replaceCertain(testMap11.toString()),replaceCertain(testMap12.toString()));

        //更新股权类产品信息
        updateWord = "udBond02";
        Map mapProdComInfo = gdBF.init03BondProductInfo();
        //对象标识
        mapProdComInfo.put("product_object_id", gdEquityCode);

        String updateProd = gd.GDUpdateProductInfo(gdContractAddress, mapProdComInfo);
        assertEquals("200", JSONObject.fromObject(updateProd).getString("state"));

        String txId = JSONObject.fromObject(updateProd).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        String storeData = com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(txDetail).getJSONObject(
                "data").getJSONObject("store").getString("storeData")).get(0).toString();

        assertEquals("不包含敏感词", true, gdCF.chkSensitiveWord(txDetail, prodType));


        //检查产品信息是否是更新后的信息
        queryProd = gd.GDProductQuery(gdContractAddress, gdEquityCode);


        //设置各个主体版本变量
        String newSubVer = gdCF.getObjectLatestVer(gdCompanyID);
        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        //检查产品更新不会影响主体版本变更
        assertEquals("主体版本信息版本", String.valueOf(gdCpmIdOldVer + 2), newSubVer);

        String prodPMSRefVer = gdCF.getObjectLatestVer(product_market_subject_ref);
        String prodSPSRefVer = gdCF.getObjectLatestVer(service_provider_subject_ref);

        //获取链上mini url的存证信息并检查是否包含uri信息
        String prodfileName = conJGFileName(gdEquityCode, newEqProdVer);
        String chkProdURI = prodfileName;
        assertEquals(true, storeData.contains(chkProdURI));
        assertEquals(true, gdCF.bContainJGFlag(storeData));//确认meta信息包含监管关键字


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getProInfo = null;
        if (!type.equals("4"))
            getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, type);

        //填充header content字段
        //如果不是机构会员登记 则执行产品填充header content字段
        if (!type.equals("4")) {
            mapProdComInfo.put("content", gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts8)));
        }

        //产品发行主体引用设置为空场景 当前代码会自动补充发行主体对象标识
//        mapProdComInfo.put("product_issuer_subject_ref", gdCompanyID);
        //产品如下字段引用的是发行主体
        product_issuer_subject_ref = gdCompanyID;

        String[] verForProd = new String[]{"/" + prodPMSRefVer, "/" + newSubVer, "/" + prodSPSRefVer};

        if (!type.equals("4")) {
            log.info("检查产品存证信息内容与传入一致\n" + mapProdComInfo.toString() + "\n" + getProInfo.toString());
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer(mapProdComInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
        }
    }

    @Test
    public void TC22_allUpdateFundProdAccInfo()throws Exception {
        String type = "3";
        //挂牌企业登记后开户
        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        uf.regTestUnit_ThenCreateAcc(type, false);
        //检查产品信息
        String queryProd = gd.GDProductQuery(gdContractAddress, gdEquityCode);

//        Map mapProd = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(queryProd).getJSONObject("data").toString());
//        Map<String, String> testMap11 = new TreeMap<String, String>(mapProd);
//        Map<String, String> testMap12 = new TreeMap<String, String>(equityProductInfo);
//        assertEquals(replaceCertain(testMap11.toString()),replaceCertain(testMap12.toString()));

        //更新股权类产品信息
        updateWord = "udFund02";
        Map mapProdComInfo = gdBF.init03FundProductInfo();
        //对象标识
        mapProdComInfo.put("product_object_id", gdEquityCode);

        String updateProd = gd.GDUpdateProductInfo(gdContractAddress, mapProdComInfo);
        assertEquals("200", JSONObject.fromObject(updateProd).getString("state"));

        String txId = JSONObject.fromObject(updateProd).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        String storeData = com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(txDetail).getJSONObject(
                "data").getJSONObject("store").getString("storeData")).get(0).toString();

        assertEquals("不包含敏感词", true, gdCF.chkSensitiveWord(txDetail, prodType));


        //检查产品信息是否是更新后的信息
        queryProd = gd.GDProductQuery(gdContractAddress, gdEquityCode);


        //设置各个主体版本变量
        String newSubVer = gdCF.getObjectLatestVer(gdCompanyID);
        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        //检查产品更新不会影响主体版本变更
        assertEquals("主体版本信息版本", String.valueOf(gdCpmIdOldVer + 2), newSubVer);

        String prodPMSRefVer = gdCF.getObjectLatestVer(product_market_subject_ref);
        String prodSPSRefVer = gdCF.getObjectLatestVer(service_provider_subject_ref);

        //获取链上mini url的存证信息并检查是否包含uri信息
        String prodfileName = conJGFileName(gdEquityCode, newEqProdVer);
        String chkProdURI = prodfileName;
        assertEquals(true, storeData.contains(chkProdURI));
        assertEquals(true, gdCF.bContainJGFlag(storeData));//确认meta信息包含监管关键字


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getProInfo = null;
        if (!type.equals("4"))
            getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, type);

        //填充header content字段
        //如果不是机构会员登记 则执行产品填充header content字段
        if (!type.equals("4")) {
            mapProdComInfo.put("content", gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts8)));
        }

        //产品发行主体引用设置为空场景 当前代码会自动补充发行主体对象标识
//        mapProdComInfo.put("product_issuer_subject_ref", gdCompanyID);
        //产品如下字段引用的是发行主体
        product_issuer_subject_ref = gdCompanyID;

        String[] verForProd = new String[]{"/" + prodPMSRefVer, "/" + newSubVer, "/" + prodSPSRefVer};

        if (!type.equals("4")) {
            log.info("检查产品存证信息内容与传入一致\n" + mapProdComInfo.toString() + "\n" + getProInfo.toString());
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer(mapProdComInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
        }
    }

}
