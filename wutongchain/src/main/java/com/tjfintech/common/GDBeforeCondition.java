package com.tjfintech.common;


import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;


@Slf4j

public class GDBeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    GuDeng gd = testBuilder.getGuDeng();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();


    //赋值权限999 区分是否主子链
    public void setPermission999()throws Exception{

        String toolPath="cd "+ ToolPATH +";";
        String exeCmd="./" + ToolTPName + " permission ";

        SDKID=utilsClass.getSDKID();
        String ledger ="";
        ledger=(subLedger!="")?" -z "+subLedger:"";
        String preCmd=toolPath+exeCmd+"-p "+PEER1RPCPort+" -s SDK "+ledger+" -d "+SDKID+" -m ";
        String getPerm=toolPath+"./" + ToolTPName + " getpermission -p "+PEER1RPCPort + " -d " + SDKID + ledger;


        //如果没有权限 则设置权限  修改为设置 兼容版本升级时 权限列表变更需要重新赋权限的问题
//        if(!shExeAndReturn(PEER1IP,getPerm).contains(fullPerm)){
            assertEquals(true,shExeAndReturn(PEER1IP,preCmd + "999").contains("success"));
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals(true,shExeAndReturn(PEER1IP,getPerm).contains(fullPerm));
//        }
    }

    public void clearDataSetPerm999() throws Exception{
        utilsClass.delDataBase();//清空sdk当前使用数据库数据
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB,resetPeerBase);
        //重启SDK
        utilsClass.setAndRestartSDK();

        setPermission999();
    }

    @Test
    public void gdCreateAccout()throws Exception{
        initRegulationData();//初始化监管对接数据
        sleepAndSaveInfo(5000);

        String cltNo = gdAccClientNo1;

        //创建第1个账户
        Map mapAcc = new HashMap();
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID1 = mapAcc.get("keyID").toString();
        gdAccount1 = mapAcc.get("accout").toString();
        String txId1 = mapAcc.get("txId").toString();

        //创建第2个账户
        mapAcc.clear();
        cltNo = gdAccClientNo2;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID2 = mapAcc.get("keyID").toString();
        gdAccount2 = mapAcc.get("accout").toString();
        String txId2 = mapAcc.get("txId").toString();

        //创建第3个账户
        mapAcc.clear();
        cltNo = gdAccClientNo3;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID3 = mapAcc.get("keyID").toString();
        gdAccount3 = mapAcc.get("accout").toString();
        String txId3 = mapAcc.get("txId").toString();

        //创建第4个账户
        mapAcc.clear();
        cltNo = gdAccClientNo4;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID4 = mapAcc.get("keyID").toString();
        gdAccount4 = mapAcc.get("accout").toString();
        String txId4 = mapAcc.get("txId").toString();

        //创建第5个账户
        mapAcc.clear();
        cltNo = gdAccClientNo5;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID5 = mapAcc.get("keyID").toString();
        gdAccount5 = mapAcc.get("accout").toString();
        String txId5 = mapAcc.get("txId").toString();

        //创建第6个账户
        mapAcc.clear();
        cltNo = gdAccClientNo6;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID6 = mapAcc.get("keyID").toString();
        gdAccount6 = mapAcc.get("accout").toString();
        String txId6 = mapAcc.get("txId").toString();

        //创建第7个账户
        mapAcc.clear();
        cltNo = gdAccClientNo7;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID7 = mapAcc.get("keyID").toString();
        gdAccount7 = mapAcc.get("accout").toString();
        String txId7 = mapAcc.get("txId").toString();

        //创建第8个账户
        mapAcc.clear();
        cltNo = gdAccClientNo8;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID8 = mapAcc.get("keyID").toString();
        gdAccount8 = mapAcc.get("accout").toString();
        String txId8 = mapAcc.get("txId").toString();

        //创建第9个账户
        mapAcc.clear();
        cltNo = gdAccClientNo9;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID9 = mapAcc.get("keyID").toString();
        gdAccount9 = mapAcc.get("accout").toString();
        String txId9 = mapAcc.get("txId").toString();

        //创建第10个账户
        mapAcc.clear();
        cltNo = gdAccClientNo10;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID10 = mapAcc.get("keyID").toString();
        gdAccount10 = mapAcc.get("accout").toString();
        String txId10 = mapAcc.get("txId").toString();

        commonFunc.sdkCheckTxOrSleep(txId10,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //判断所有开户接口交易上链
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId4)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId5)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId6)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId7)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId8)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId9)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId10)).getString("state"));


    }

    public Map<String,String> gdCreateAccParam(String clientNo){
        String cltNo = clientNo;
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        init02EquityAccountInfo();
        equityaccountInfo.put("账户对象标识",cltNo);  //更新账户对象标识字段
        log.info(equityaccountInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", equityaccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        init02FundAccountInfo();
        fundaccountInfo.put("账户对象标识",cltNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo",fundaccountInfo);

        //构造个人/投资者主体信息
        init01PersonalSubjectInfo();
        investorSubjectInfo.put("对象标识",cltNo);  //更新对象标识字段
        investorSubjectInfo.put("主体标识","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, investorSubjectInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        assertEquals(cltNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
        String keyID = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        String addr= JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

        Map mapAccInfo = new HashMap();
        mapAccInfo.put("keyID",keyID);
        mapAccInfo.put("accout",addr);
        mapAccInfo.put("txId",txId);
        mapAccInfo.put("response",response);

        mapAccAddr.put(addr,clientNo);

        return mapAccInfo;
    }

//    @Test
    public void initRegulationData() {
        //更新系统合约
        gd.GDEquitySystemInit(gdContractAddress,gdPlatfromKeyID);

        log.info("初始化监管相关数据结构");
        enterpriseSubjectInfo = init01EnterpriseSubjectInfo();      //初始化企业主体数据信息  涉及接口 企业挂牌登记
        investorSubjectInfo = init01PersonalSubjectInfo();        //初始化个人主体数据信息  涉及接口 开户
        equityaccountInfo = init02EquityAccountInfo();          //初始化账户数据信息 股权账户  涉及接口 开户
        fundaccountInfo = init02FundAccountInfo();            //初始化账户数据信息 资金账户  涉及接口 开户
        equityProductInfo = init03EquityProductInfo();          //初始化股权类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
        bondProductInfo = init03BondProductInfo();            //初始化债券类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
        txInformation = init04TxInfo();                     //初始化交易数据信息  涉及接口 过户转让
        registerInfo = init05RegInfo();                    //初始化登记数据信息  涉及接口 发行 股份性质变更 过户转让 增发 冻结 解除冻结
        settleInfo = init06SettleInfo();                 //初始化资金结算数据信息  涉及接口 资金清算
        disclosureInfo = init07PublishInfo();                //初始化信息数据信息  涉及接口 写入公告

        //初始化listRegInfo
        listRegInfo.clear();
        listRegInfo.add(registerInfo);
        listRegInfo.add(registerInfo);
    }

    public Map init01EnterpriseSubjectInfo() {
        Map mapTemp = new HashMap();

        log.info("初始化01主体企业数据结构");
        List<String> fileList = new ArrayList<>();
        fileList.add("file.txt");
        mapTemp.clear();
        mapTemp.put("对象标识",gdCompanyID);  //对象标识使用公司ID
        mapTemp.put("主体标识",gdCompanyID + "sub");
        mapTemp.put("行业主体代号","123");
        mapTemp.put("主体类型",0);
        mapTemp.put("主体信息创建时间","2020/09/12 12:01:12");

        List<Map> listQual = new ArrayList<>();
        Map qualification1 = new HashMap();
        Map qualification2 = new HashMap();
        listQual.add(qualification1);
        listQual.add(qualification2);

        qualification1.put("资质认证类型",0);
        qualification1.put("资质认证文件",fileList);
        qualification1.put("资质认证方","苏州市监管局");
        qualification1.put("资质审核方","苏州市监管局");
        qualification1.put("认证时间","2012/09/12 12:01:12");
        qualification1.put("审核时间","2012/09/12 12:01:12");

        qualification2.put("资质认证类型",3);
        qualification2.put("资质认证文件",fileList);
        qualification2.put("资质认证方","苏州市监管局");
        qualification2.put("资质审核方","苏州市监管局");
        qualification2.put("认证时间","2010/09/12 12:01:12");
        qualification2.put("审核时间","2010/09/12 12:01:12");

        mapTemp.put("主体资质信息",listQual);
        mapTemp.put("机构类型",0);
        mapTemp.put("机构性质",0);
        mapTemp.put("公司全称","苏州同济区块链研究院");
        mapTemp.put("英文名称","tongji");
        mapTemp.put("公司简称","苏同院");
        mapTemp.put("英文简称","sztj");
        mapTemp.put("企业类型",0);
        mapTemp.put("企业成分",1);
        mapTemp.put("统一社会信用代码","91370105MA3N4THQ54");
        mapTemp.put("组织机构代码","91370105MA3N4THQ54");
        mapTemp.put("设立日期","2010/09/12");
        mapTemp.put("营业执照","营业执行.pdf");
        mapTemp.put("经营范围","all");
        mapTemp.put("企业所属行业",0);
        mapTemp.put("主营业务","软件");
        mapTemp.put("公司简介","提供区块链技术与应用研发测评人才培养以及产业孵化等综合性服务平台");
        mapTemp.put("注册资本",10000000);
        mapTemp.put("注册资本币种",156);
        mapTemp.put("实收资本",156);
        mapTemp.put("实收资本币种",156);
        mapTemp.put("注册地址","苏州");
        mapTemp.put("办公地址","苏州相城");
        mapTemp.put("联系地址","苏州相城");
        mapTemp.put("联系电话","051266188618");
        mapTemp.put("传真","051266188618");
        mapTemp.put("邮政编码","215133");
        mapTemp.put("互联网地址","http://www.tj-fintech.com/");
        mapTemp.put("电子邮箱","zz@wutongchain.com");
        mapTemp.put("公司章程","stli.pdf");
        mapTemp.put("主管单位","相城区人民政府");
        mapTemp.put("股东总数（个）",10);
        mapTemp.put("股本总数(股)",1000000000);
        mapTemp.put("法定代表人姓名","任山东");
        mapTemp.put("法人性质",0);
        mapTemp.put("法定代表人身份证件类型",0);
        mapTemp.put("法定代表人身份证件号码","123111111111145");
        mapTemp.put("法定代表人职务",0);
        mapTemp.put("法定代表人手机号","15865487895");

        return mapTemp;
    }

    public Map init01PersonalSubjectInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化01主体个人数据结构");
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test1.pdf");
        fileList1.add("test2.pdf");
        List<Map> mapQuali = new ArrayList<>();
        Map qual = new HashMap();

        mapTemp.clear();
        String cltNo = "test00001";
        mapTemp.put("对象标识",cltNo);
        mapTemp.put("主体标识",cltNo);
        mapTemp.put("行业主体代号","JR");
        mapTemp.put("主体类型",0);
        mapTemp.put("主体信息创建时间","2020/09/12 12:01:12");

        qual.put("资质认证类型",0);
        qual.put("资质认证文件",fileList1);
        qual.put("资质认证方","苏州市监管局");
        qual.put("资质审核方","苏州市监管局");
        qual.put("认证时间","2020/09/12 12:01:12");
        qual.put("审核时间","2020/09/12 12:01:12");

        mapQuali.add(qual);
        mapTemp.put("主体资质信息",mapQuali);
        mapTemp.put("个人姓名","zhangsan");
        mapTemp.put("个人身份证类型",0);
        mapTemp.put("个人身份证件号","325689199512230001");
        mapTemp.put("个人联系地址","相城");
        mapTemp.put("个人联系电话","15865487895");
        mapTemp.put("个人手机号","15865487895");
        mapTemp.put("学历",4);
        mapTemp.put("个人所属行业",0);
        mapTemp.put("出生日期","1949/09/12");
        mapTemp.put("性别",0);
        mapTemp.put("评级结果","通过");
        mapTemp.put("评级时间","2020/09/12 12:01:12");
        mapTemp.put("评级原始记录","记录");

        return mapTemp;
    }

    public Map init02EquityAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        //默认股权账户
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test1.pdf");
        fileList1.add("test2.pdf");

        List<String> fileList2 = new ArrayList<>();
        fileList2.add("test21.pdf");
        fileList2.add("test22.pdf");
        List<String> fileList3 = new ArrayList<>();
        fileList3.add("test31.pdf");
        fileList3.add("test32.pdf");
        List<String> fileList4 = new ArrayList<>();
        fileList4.add("test41.pdf");
        fileList4.add("test42.pdf");
        List<String> fileList5 = new ArrayList<>();
        fileList5.add("test51.pdf");
        fileList5.add("test52.pdf");
        mapTemp.clear();
        mapTemp.put("账户对象标识","testacc00001");
        mapTemp.put("账户所属主体引用","hrefid00001");
        mapTemp.put("开户机构主体引用","drefid00001");
        mapTemp.put("账号","h0123555");
        mapTemp.put("账户类型",0);  //默认股权账户
        mapTemp.put("账户用途",0);
        mapTemp.put("账号状态",0);
        mapTemp.put("账户开户时间","2012/6/25");
        mapTemp.put("账户开户核验凭证",fileList4);
        mapTemp.put("账户销户时间","2022/6/25");
        mapTemp.put("账户销户核验凭证",fileList2);
        mapTemp.put("账户冻结时间","2020/6/25");
        mapTemp.put("账户冻结核验凭证",fileList3);
        mapTemp.put("账户解冻时间","2020/6/25");
        mapTemp.put("账户解冻核验凭证",fileList4);
        mapTemp.put("关联关系",0);
        mapTemp.put("关联账户对象引用","t5pdf");
        mapTemp.put("关联账户开户文件",fileList5);
        return mapTemp;
    }

    public Map init02FundAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        //默认股权账户
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test1.pdf");
        fileList1.add("test2.pdf");

        List<String> fileList2 = new ArrayList<>();
        fileList2.add("test21.pdf");
        fileList2.add("test22.pdf");
        List<String> fileList3 = new ArrayList<>();
        fileList3.add("test31.pdf");
        fileList3.add("test32.pdf");
        List<String> fileList4 = new ArrayList<>();
        fileList4.add("test41.pdf");
        fileList4.add("test42.pdf");
        List<String> fileList5 = new ArrayList<>();
        fileList5.add("test51.pdf");
        fileList5.add("test52.pdf");
        mapTemp.clear();
        mapTemp.put("账户对象标识","testacc00001");
        mapTemp.put("账户所属主体引用","hrefid00001");
        mapTemp.put("开户机构主体引用","drefid00001");
        mapTemp.put("账号","h0123555");
        mapTemp.put("账户类型",1);  //资金账户
        mapTemp.put("账户用途",0);
        mapTemp.put("账号状态",0);
        mapTemp.put("账户开户时间","2012/6/25");
        mapTemp.put("账户开户核验凭证",fileList4);
        mapTemp.put("账户销户时间","2022/6/25");
        mapTemp.put("账户销户核验凭证",fileList2);
        mapTemp.put("账户冻结时间","2020/6/25");
        mapTemp.put("账户冻结核验凭证",fileList3);
        mapTemp.put("账户解冻时间","2020/6/25");
        mapTemp.put("账户解冻核验凭证",fileList4);
        mapTemp.put("关联关系",0);
        mapTemp.put("关联账户对象引用","t5pdf");
        mapTemp.put("关联账户开户文件",fileList5);
        return mapTemp;
    }

    public Map init03EquityProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();
        mapTemp.put("产品对象标识",gdEquityCode + "01");
        mapTemp.put("发行主体引用",gdCompanyID);
        mapTemp.put("发行主体名称","suzhou");
        mapTemp.put("登记机构主体引用","regobj001");
        mapTemp.put("托管机构主体引用","tuoguanobj001");
        mapTemp.put("产品代码","SH00001");
        mapTemp.put("产品全称","联合股权");
        mapTemp.put("产品简称","联股");
        mapTemp.put("产品类型",0);
        mapTemp.put("最大账户数量",0);
        mapTemp.put("信息披露方式",0);
        mapTemp.put("产品规模单位",0);
        mapTemp.put("产品规模币种","156");
        mapTemp.put("产品规模总额",10000000);
        mapTemp.put("浏览范围",0);
        mapTemp.put("交易范围",0);
        mapTemp.put("承销机构主体引用","chxobj0001");
        mapTemp.put("承销机构名称","chx123");
        mapTemp.put("律师事务所主体引用","lawobj0001");
        mapTemp.put("律师事务所名称","lawcorp");
        mapTemp.put("会计事务所主体引用","accountobj001");
        mapTemp.put("会计事务所名称","accoutcorp");
        mapTemp.put("发行方联系人","李四");
        mapTemp.put("发行方联系信息","acccorp");

        List<String> listFile = new ArrayList<>();
        listFile.add("dd.pdf");
        List<Map> mapList1 = new ArrayList<>();
        Map equityMap = new HashMap();

        equityMap.put("发行代码","tea111");
        equityMap.put("发行价格",10000);
        equityMap.put("发行股数",0);
        equityMap.put("全年净利润",10000);
        equityMap.put("发起前股数",0);
        equityMap.put("发行后股数",0);
        equityMap.put("发行后总市值",10000);
        equityMap.put("半年净利润",10000);
        equityMap.put("募集金额",10000);
        equityMap.put("定向发行人数",0);
        equityMap.put("发行开始日期","2020/10/25");
        equityMap.put("发行结束日期","2020/10/25");
        equityMap.put("登记日期","2020/10/25");
        equityMap.put("发行文件编号","tea111");
        equityMap.put("发行文件列表",listFile);
        equityMap.put("挂牌代码","tea111");
        equityMap.put("挂牌日期","2020/10/25");
        equityMap.put("挂牌备注信息","tea111");
        equityMap.put("挂牌状态",0);
        equityMap.put("摘牌日期","2020/10/25");
        equityMap.put("摘牌原因",0);

        mapList1.add(equityMap);
        mapTemp.put("股权类-发行增资信息",mapList1);
        return mapTemp;
    }

    public Map init03BondProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();
        mapTemp.put("产品对象标识",gdEquityCode + "02");
        mapTemp.put("发行主体引用",gdCompanyID);
        mapTemp.put("发行主体名称","issue00001");
        mapTemp.put("登记机构主体引用","tea11001");
        mapTemp.put("托管机构主体引用","tea11002");
        mapTemp.put("产品代码","SH00001");
        mapTemp.put("产品全称","联合股权");
        mapTemp.put("产品简称","联股");
        mapTemp.put("产品类型",0);
        mapTemp.put("最大账户数量",10);
        mapTemp.put("信息披露方式",0);
        mapTemp.put("产品规模单位",100000);
        mapTemp.put("产品规模币种","156");
        mapTemp.put("产品规模总额",1000000);
        mapTemp.put("浏览范围",0);
        mapTemp.put("交易范围",0);
        mapTemp.put("承销机构主体引用","cx00001");
        mapTemp.put("承销机构名称","cxabc");
        mapTemp.put("律师事务所主体引用","lawobj00001");
        mapTemp.put("律师事务所名称","lawyer");
        mapTemp.put("会计事务所主体引用","countobj001");
        mapTemp.put("会计事务所名称","countax");
        mapTemp.put("发行方联系人","zhagnss");
        mapTemp.put("发行方联系信息","15968526398");
        mapTemp.put("发行代码","12011");
        mapTemp.put("存续期限","2022/10/25");
        mapTemp.put("最小账户数量",0);
        mapTemp.put("产品面值",100);
        mapTemp.put("票面利率",10);
        mapTemp.put("利率形式","155");
        mapTemp.put("付息频率","12");
        mapTemp.put("非闰年计息天数",0);
        mapTemp.put("闰年计息天数",0);
        mapTemp.put("发行价格",100);
        mapTemp.put("选择权条款",0);
        mapTemp.put("（本期）发行规模上限",1000000);
        mapTemp.put("（本期）发行规模下限",100);
        mapTemp.put("发行开始日期","2020/10/25");
        mapTemp.put("发行结束日期","2023/10/25");
        mapTemp.put("登记日期","2020/10/25");
        mapTemp.put("起息日期","2020/10/25");
        mapTemp.put("到期日期","2026/10/25");
        mapTemp.put("首次付息日期","2020/10/25");
        mapTemp.put("发行文件编号","wenjian00001");
        List<String> listFile = new ArrayList<>();
        listFile.add("dd.pdf");
        mapTemp.put("发行文件列表",listFile);
        mapTemp.put("发行方主体信用评级",0);
        mapTemp.put("增信机构主体引用","acobj0001");
        mapTemp.put("增信机构名称","acter");
        mapTemp.put("增信机构主体评级",0);
        mapTemp.put("信用评级机构主体引用","cdcobj001");
        mapTemp.put("信用评级机构名称","scder");
        mapTemp.put("担保机构主体引用","sdobj001");
        mapTemp.put("担保机构名称","sdqwe");
        mapTemp.put("担保安排","122");
        mapTemp.put("产品终止条件","撤销");
        return mapTemp;
    }


    public Map init04TxInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化04交易数据结构");
        //04交易报告
        mapTemp.put("交易对象标识","txoid00001");
        mapTemp.put("交易产品引用",gdEquityCode + "01");
        mapTemp.put("产品名称","联合股权");
        mapTemp.put("交易类型",0);
        mapTemp.put("交易场所","上海");
        mapTemp.put("交易描述信息","交易");
        mapTemp.put("交易成交流水号","00000000001");
        mapTemp.put("成交方式",0);
        mapTemp.put("成交币种","156");
        mapTemp.put("成交价格",1000);
        mapTemp.put("成交数量",1000);
        mapTemp.put("成交时间","2020/10/8");
        mapTemp.put("交易成交描述信息","交易成功");
        mapTemp.put("发行方主体引用",gdCompanyID);
        mapTemp.put("发行方名称","issue001");
        mapTemp.put("投资方主体引用","accobj0001");
        mapTemp.put("投资方名称","联合");
        mapTemp.put("原持有方主体引用","acchobj001");
        mapTemp.put("原持有方名称","zhagnsan");
        mapTemp.put("对手方主体引用","acchobj002");
        mapTemp.put("对手方名称","李四");
        mapTemp.put("委托核验凭证","ddd.pdf");
        mapTemp.put("成交核验凭证","erq.pdf");

        List<Map> mapList1 = new ArrayList<>();
        Map equityMap = new HashMap();
        equityMap.put("中介类型",0);
        equityMap.put("中介机构主体引用","obj001");
        equityMap.put("中介机构名称","中介");
        mapList1.add(equityMap);
        mapTemp.put("交易中介信息",mapList1);

        return mapTemp;
    }

    public Map init05RegInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化05登记数据结构");
        //05登记
        List<String> listRegFile = new ArrayList<>();
        listRegFile.add("verify.crt");
        mapTemp.clear();
        mapTemp.put("登记对象标识","regid00001");
        mapTemp.put("登记类型",0);
        mapTemp.put("登记流水号",regNo);
        mapTemp.put("登记时间","2020/7/8");
        mapTemp.put("登记主体引用",gdCompanyID);
        mapTemp.put("登记主体类型",0);
        mapTemp.put("权利登记单位",0);
        mapTemp.put("登记币种","156");
        mapTemp.put("变动额",10000);
        mapTemp.put("当前可用余额",10000);
        mapTemp.put("当前可用余额占比",78);
        mapTemp.put("质押变动额",10000);
        mapTemp.put("当前质押余额",10000);
        mapTemp.put("冻结变动额",10000);
        mapTemp.put("当前冻结余额",10000);
        mapTemp.put("持有状态",0);
        mapTemp.put("持有属性",0);
        mapTemp.put("来源类型",0);
        mapTemp.put("登记说明","登记联合股权项目产品");
        mapTemp.put("登记核验凭证",listRegFile);
        mapTemp.put("登记产品类型",0);
        mapTemp.put("登记产品引用","regobj00011");
        mapTemp.put("权利人账户引用","accoid00001");
        mapTemp.put("交易报告引用",0);
        mapTemp.put("名册主体引用","dd");
        mapTemp.put("权利类型",0);
        mapTemp.put("登记日期","2020/7/8");
        mapTemp.put("股东主体引用","haccobj001");
        mapTemp.put("股东主体类型",0);
        mapTemp.put("股份性质",0);
        mapTemp.put("认缴金额",10000);
        mapTemp.put("实缴金额",10000);
        mapTemp.put("持股比例",20);
        mapTemp.put("债权人主体引用","bondobj0001");
        mapTemp.put("债权人类型",0);
        mapTemp.put("认购数量",0);
        mapTemp.put("认购金额",10000);
        mapTemp.put("债权人联系方式","ws@wutongchain.com");
        return mapTemp;
    }

    public Map init06SettleInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化06资金清算数据结构");
        mapTemp.clear();
        mapTemp.put("资金结算对象标识","stoid00001");
        mapTemp.put("结算机构主体引用","setobj00001");
        mapTemp.put("结算类型",0);
        mapTemp.put("结算流水号","00000001");
        mapTemp.put("结算时间","2020/8/7");
        mapTemp.put("交易报告引用","txobj00001");
        mapTemp.put("结算币种","156");
        mapTemp.put("结算金额",10000);
        mapTemp.put("结算说明","季度结算");
        mapTemp.put("结算操作凭证","operate.pdf");
        mapTemp.put("转出方银行代号","A000001");
        mapTemp.put("转出方银行名称","A000002");
        mapTemp.put("转出方银行账号","AX0000001");
        mapTemp.put("转出方账户引用","axobj0001");
        mapTemp.put("转出方账户名称","AX0000002");
        mapTemp.put("转出方发生前金额",1000);
        mapTemp.put("转出方发生后余额",10000);
        mapTemp.put("转入方银行代号","B000001");
        mapTemp.put("转入方银行名称","B000002");
        mapTemp.put("转入方银行账号","BX0000001");
        mapTemp.put("转入方账户引用","bxobj0001");
        mapTemp.put("转入方账户名称","BX0000002");
        mapTemp.put("转入方资金账号","BY0000002");
        mapTemp.put("转入方发生前金额",10000);
        mapTemp.put("转入方发生后余额",9000);
        return mapTemp;
    }

    public Map init07PublishInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化07信披数据结构");

        //07信披监管数据
        List<String> listPubFile = new ArrayList<>();
        listPubFile.add("tix.pdf");

        //诚信档案
        List<Map> listCredit = new ArrayList<>();
        Map mapCd = new HashMap();

        mapTemp.clear();

        mapTemp.put("信批对象标识", "disoid00001");
        mapTemp.put("信批主体引用", "pubobj0001");
        mapTemp.put("期间起始日期", "2020/9/6");
        mapTemp.put("截止日期", "2020/9/6");
        mapTemp.put("报表类型", 0);
        mapTemp.put("期末总资产(元)", 100000);
        mapTemp.put("期末净资产(元)", "100000");
        mapTemp.put("总负债(元)", "100000");
        mapTemp.put("本期营业收入(元)", "100000");
        mapTemp.put("本期利润总额（元）", "100000");
        mapTemp.put("本期净利润（元）", "100000");
        mapTemp.put("现金流量（元）", "100000");
        mapTemp.put("是否有研发费用", "100000");
        mapTemp.put("研发费用（元）", "5000");
        mapTemp.put("资产负债表(PDF)", "4552.pdf");
        mapTemp.put("现金流量表(PDF)", "dd.pdf");
        mapTemp.put("利润表(PDF)", "dd445.pdf");
        mapTemp.put("重大事件类型", 0);
        mapTemp.put("文件列表", listPubFile);
        mapTemp.put("提报时间", "2020/9/6");


        mapCd.put("提供方主体引用", "aaaaaaaaa");
        mapCd.put("提供方名称", "aaa" );
        mapCd.put("认定方主体标识引用", "idref");
        mapCd.put("认定方名称", "rdname");
        mapCd.put("鉴定方主体标识引用", "jdbiaozhi");
        mapCd.put("鉴定方名称", "jdname");
        mapCd.put("事项编号", "xyzza");
        mapCd.put("事项名称", "item_name");
        mapCd.put("事项类型", 14);
        mapCd.put("事项描述", "describe_aaasss");
        mapCd.put("效力期限", 1);
        mapCd.put("开始日期", "2020/10/29");
        mapCd.put("结束日期", "2020/12/30");
        mapCd.put("状态", 1);
        mapCd.put("事项凭证",listPubFile);
        listCredit.add(mapCd);
        listCredit.add(mapCd);

        mapTemp.put("诚信档案",listCredit);
        return mapTemp;
    }

}
