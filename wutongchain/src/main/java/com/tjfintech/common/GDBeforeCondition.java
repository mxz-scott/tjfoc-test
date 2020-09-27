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

        return mapAccInfo;
    }

//    @Test
    public void initRegulationData() {
        log.info("初始化监管相关数据结构");
        init01EnterpriseSubjectInfo();      //初始化企业主体数据信息  涉及接口 企业挂牌登记
        init01PersonalSubjectInfo();        //初始化个人主体数据信息  涉及接口 开户
        init02EquityAccountInfo();          //初始化账户数据信息 股权账户  涉及接口 开户
        init02FundAccountInfo();            //初始化账户数据信息 资金账户  涉及接口 开户
        init03EquityProductInfo();          //初始化股权类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
        init03BondProductInfo();            //初始化债券类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
        init04TxInfo();                     //初始化交易数据信息  涉及接口 过户转让
        init05RegInfo();                    //初始化登记数据信息  涉及接口 发行 股份性质变更 过户转让 增发 冻结 解除冻结
        init06SettleInfo();                 //初始化资金结算数据信息  涉及接口 资金清算
        init07PublishInfo();                //初始化信息数据信息  涉及接口 写入公告

        //初始化listRegInfo
        listRegInfo.add(registerInfo);
        listRegInfo.add(registerInfo);
    }

    public void init01EnterpriseSubjectInfo() {
        log.info("初始化01主体企业数据结构");
        List<String> fileList = new ArrayList<>();
        fileList.add("file.txt");
        enterpriseSubjectInfo.clear();
        enterpriseSubjectInfo.put("对象标识",gdCompanyID);  //对象标识使用公司ID
        enterpriseSubjectInfo.put("主体标识",gdCompanyID + "sub");
        enterpriseSubjectInfo.put("行业主体代号","123");
        enterpriseSubjectInfo.put("主体类型",0);
        enterpriseSubjectInfo.put("主体信息创建时间","2020/09/12 12:01:12");

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

        enterpriseSubjectInfo.put("主体资质信息",listQual);
        enterpriseSubjectInfo.put("机构类型",0);
        enterpriseSubjectInfo.put("机构性质",0);
        enterpriseSubjectInfo.put("公司全称","苏州同济区块链研究院");
        enterpriseSubjectInfo.put("英文名称","tongji");
        enterpriseSubjectInfo.put("公司简称","苏同院");
        enterpriseSubjectInfo.put("英文简称","sztj");
        enterpriseSubjectInfo.put("企业类型",0);
        enterpriseSubjectInfo.put("企业成分",1);
        enterpriseSubjectInfo.put("统一社会信用代码","91370105MA3N4THQ54");
        enterpriseSubjectInfo.put("组织机构代码","91370105MA3N4THQ54");
        enterpriseSubjectInfo.put("设立日期","2010/09/12");
        enterpriseSubjectInfo.put("营业执照","营业执行.pdf");
        enterpriseSubjectInfo.put("经营范围","all");
        enterpriseSubjectInfo.put("企业所属行业",0);
        enterpriseSubjectInfo.put("主营业务","软件");
        enterpriseSubjectInfo.put("公司简介","提供区块链技术与应用研发测评人才培养以及产业孵化等综合性服务平台");
        enterpriseSubjectInfo.put("注册资本",10000000);
        enterpriseSubjectInfo.put("注册资本币种",156);
        enterpriseSubjectInfo.put("实收资本",156);
        enterpriseSubjectInfo.put("实收资本币种",156);
        enterpriseSubjectInfo.put("注册地址","苏州");
        enterpriseSubjectInfo.put("办公地址","苏州相城");
        enterpriseSubjectInfo.put("联系地址","苏州相城");
        enterpriseSubjectInfo.put("联系电话","051266188618");
        enterpriseSubjectInfo.put("传真","051266188618");
        enterpriseSubjectInfo.put("邮政编码","215133");
        enterpriseSubjectInfo.put("互联网地址","http://www.tj-fintech.com/");
        enterpriseSubjectInfo.put("电子邮箱","zz@wutongchain.com");
        enterpriseSubjectInfo.put("公司章程","stli.pdf");
        enterpriseSubjectInfo.put("主管单位","相城区人民政府");
        enterpriseSubjectInfo.put("股东总数（个）",10);
        enterpriseSubjectInfo.put("股本总数(股)",1000000000);
        enterpriseSubjectInfo.put("法定代表人姓名","任山东");
        enterpriseSubjectInfo.put("法人性质",0);
        enterpriseSubjectInfo.put("法定代表人身份证件类型",0);
        enterpriseSubjectInfo.put("法定代表人身份证件号码","123111111111145");
        enterpriseSubjectInfo.put("法定代表人职务",0);
        enterpriseSubjectInfo.put("法定代表人手机号","15865487895");
    }

    public void init01PersonalSubjectInfo() {
        log.info("初始化01主体个人数据结构");
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test1.pdf");
        fileList1.add("test2.pdf");
        List<Map> mapQuali = new ArrayList<>();
        Map qual = new HashMap();

        investorSubjectInfo.clear();
        String cltNo = "test00001";
        investorSubjectInfo.put("对象标识",cltNo);
        investorSubjectInfo.put("主体标识","sid" + cltNo);
        investorSubjectInfo.put("行业主体代号","JR");
        investorSubjectInfo.put("主体类型",0);
        investorSubjectInfo.put("主体信息创建时间","2020/09/12 12:01:12");

        qual.put("资质认证类型",0);
        qual.put("资质认证文件",fileList1);
        qual.put("资质认证方","苏州市监管局");
        qual.put("资质审核方","苏州市监管局");
        qual.put("认证时间","2020/09/12 12:01:12");
        qual.put("审核时间","2020/09/12 12:01:12");

        mapQuali.add(qual);
        investorSubjectInfo.put("主体资质信息",mapQuali);
        investorSubjectInfo.put("个人姓名","zhangsan");
        investorSubjectInfo.put("个人身份证类型",0);
        investorSubjectInfo.put("个人身份证件号","325689199512230001");
        investorSubjectInfo.put("个人联系地址","相城");
        investorSubjectInfo.put("个人联系电话","15865487895");
        investorSubjectInfo.put("个人手机号","15865487895");
        investorSubjectInfo.put("学历",4);
        investorSubjectInfo.put("个人所属行业",0);
        investorSubjectInfo.put("出生日期","1949/09/12");
        investorSubjectInfo.put("性别",0);
        investorSubjectInfo.put("评级结果","通过");
        investorSubjectInfo.put("评级时间","2020/09/12 12:01:12");
        investorSubjectInfo.put("评级原始记录","记录");
    }

    public void init02EquityAccountInfo() {
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
        equityaccountInfo.clear();
        equityaccountInfo.put("账户对象标识","testacc00001");
        equityaccountInfo.put("账户所属主体引用","hrefid00001");
        equityaccountInfo.put("开户机构主体引用","drefid00001");
        equityaccountInfo.put("账号","h0123555");
        equityaccountInfo.put("账户类型",0);  //默认股权账户
        equityaccountInfo.put("账户用途",0);
        equityaccountInfo.put("账号状态",0);
        equityaccountInfo.put("账户开户时间","2012/6/25");
        equityaccountInfo.put("账户开户核验凭证",fileList4);
        equityaccountInfo.put("账户销户时间","2022/6/25");
        equityaccountInfo.put("账户销户核验凭证",fileList2);
        equityaccountInfo.put("账户冻结时间","2020/6/25");
        equityaccountInfo.put("账户冻结核验凭证",fileList3);
        equityaccountInfo.put("账户解冻时间","2020/6/25");
        equityaccountInfo.put("账户解冻核验凭证",fileList4);
        equityaccountInfo.put("关联关系",0);
        equityaccountInfo.put("关联账户对象引用","t5pdf");
        equityaccountInfo.put("关联账户开户文件",fileList5);
    }

    public void init02FundAccountInfo() {
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
        fundaccountInfo.clear();
        fundaccountInfo.put("账户对象标识","testacc00001");
        fundaccountInfo.put("账户所属主体引用","hrefid00001");
        fundaccountInfo.put("开户机构主体引用","drefid00001");
        fundaccountInfo.put("账号","h0123555");
        fundaccountInfo.put("账户类型",1);  //资金账户
        fundaccountInfo.put("账户用途",0);
        fundaccountInfo.put("账号状态",0);
        fundaccountInfo.put("账户开户时间","2012/6/25");
        fundaccountInfo.put("账户开户核验凭证",fileList4);
        fundaccountInfo.put("账户销户时间","2022/6/25");
        fundaccountInfo.put("账户销户核验凭证",fileList2);
        fundaccountInfo.put("账户冻结时间","2020/6/25");
        fundaccountInfo.put("账户冻结核验凭证",fileList3);
        fundaccountInfo.put("账户解冻时间","2020/6/25");
        fundaccountInfo.put("账户解冻核验凭证",fileList4);
        fundaccountInfo.put("关联关系",0);
        fundaccountInfo.put("关联账户对象引用","t5pdf");
        fundaccountInfo.put("关联账户开户文件",fileList5);
    }

    public void init03EquityProductInfo() {
        log.info("初始化03产品数据结构");
        equityProductInfo.clear();
        equityProductInfo.put("产品对象标识",gdEquityCode + "01");
        equityProductInfo.put("发行主体引用",gdCompanyID);
        equityProductInfo.put("发行主体名称","suzhou");
        equityProductInfo.put("登记机构主体引用","regobj001");
        equityProductInfo.put("托管机构主体引用","tuoguanobj001");
        equityProductInfo.put("产品代码","SH00001");
        equityProductInfo.put("产品全称","联合股权");
        equityProductInfo.put("产品简称","联股");
        equityProductInfo.put("产品类型",0);
        equityProductInfo.put("最大账户数量",0);
        equityProductInfo.put("信息披露方式",0);
        equityProductInfo.put("产品规模单位",0);
        equityProductInfo.put("产品规模币种","156");
        equityProductInfo.put("产品规模总额",10000000);
        equityProductInfo.put("浏览范围",0);
        equityProductInfo.put("交易范围",0);
        equityProductInfo.put("承销机构主体引用","chxobj0001");
        equityProductInfo.put("承销机构名称","chx123");
        equityProductInfo.put("律师事务所主体引用","lawobj0001");
        equityProductInfo.put("律师事务所名称","lawcorp");
        equityProductInfo.put("会计事务所主体引用","accountobj001");
        equityProductInfo.put("会计事务所名称","accoutcorp");
        equityProductInfo.put("发行方联系人","李四");
        equityProductInfo.put("发行方联系信息","acccorp");

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
        equityProductInfo.put("股权类-发行增资信息",mapList1);
    }

    public void init03BondProductInfo() {
        log.info("初始化03产品数据结构");
        bondProductInfo.clear();
        bondProductInfo.put("产品对象标识","poid00001");
        bondProductInfo.put("发行主体引用",gdCompanyID);
        bondProductInfo.put("发行主体名称","issue00001");
        bondProductInfo.put("登记机构主体引用","tea11001");
        bondProductInfo.put("托管机构主体引用","tea11002");
        bondProductInfo.put("产品代码","SH00001");
        bondProductInfo.put("产品全称","联合股权");
        bondProductInfo.put("产品简称","联股");
        bondProductInfo.put("产品类型",0);
        bondProductInfo.put("最大账户数量",10);
        bondProductInfo.put("信息披露方式",0);
        bondProductInfo.put("产品规模单位",100000);
        bondProductInfo.put("产品规模币种","156");
        bondProductInfo.put("产品规模总额",1000000);
        bondProductInfo.put("浏览范围",0);
        bondProductInfo.put("交易范围",0);
        bondProductInfo.put("承销机构主体引用","cx00001");
        bondProductInfo.put("承销机构名称","cxabc");
        bondProductInfo.put("律师事务所主体引用","lawobj00001");
        bondProductInfo.put("律师事务所名称","lawyer");
        bondProductInfo.put("会计事务所主体引用","countobj001");
        bondProductInfo.put("会计事务所名称","countax");
        bondProductInfo.put("发行方联系人","zhagnss");
        bondProductInfo.put("发行方联系信息","15968526398");
        bondProductInfo.put("发行代码","12011");
        bondProductInfo.put("存续期限","2022/10/25");
        bondProductInfo.put("最小账户数量",0);
        bondProductInfo.put("产品面值",100);
        bondProductInfo.put("票面利率",10);
        bondProductInfo.put("利率形式","155");
        bondProductInfo.put("付息频率","12");
        bondProductInfo.put("非闰年计息天数",0);
        bondProductInfo.put("闰年计息天数",0);
        bondProductInfo.put("发行价格",100);
        bondProductInfo.put("选择权条款",0);
        bondProductInfo.put("（本期）发行规模上限",1000000);
        bondProductInfo.put("（本期）发行规模下限",100);
        bondProductInfo.put("发行开始日期","2020/10/25");
        bondProductInfo.put("发行结束日期","2023/10/25");
        bondProductInfo.put("登记日期","2020/10/25");
        bondProductInfo.put("起息日期","2020/10/25");
        bondProductInfo.put("到期日期","2026/10/25");
        bondProductInfo.put("首次付息日期","2020/10/25");
        bondProductInfo.put("发行文件编号","wenjian00001");
        List<String> listFile = new ArrayList<>();
        listFile.add("dd.pdf");
        bondProductInfo.put("发行文件列表",listFile);
        bondProductInfo.put("发行方主体信用评级",0);
        bondProductInfo.put("增信机构主体引用","acobj0001");
        bondProductInfo.put("增信机构名称","acter");
        bondProductInfo.put("增信机构主体评级",0);
        bondProductInfo.put("信用评级机构主体引用","cdcobj001");
        bondProductInfo.put("信用评级机构名称","scder");
        bondProductInfo.put("担保机构主体引用","sdobj001");
        bondProductInfo.put("担保机构名称","sdqwe");
        bondProductInfo.put("担保安排","122");
        bondProductInfo.put("产品终止条件","撤销");

    }


    public void init04TxInfo() {
        log.info("初始化04交易数据结构");
        //04交易报告
        txInformation.put("交易对象标识","txoid00001");
        txInformation.put("交易产品引用","txprdobj00001");
        txInformation.put("产品名称","联合股权");
        txInformation.put("交易类型",0);
        txInformation.put("交易场所","上海");
        txInformation.put("交易描述信息","交易");
        txInformation.put("交易成交流水号","00000000001");
        txInformation.put("成交方式",0);
        txInformation.put("成交币种","156");
        txInformation.put("成交价格",1000);
        txInformation.put("成交数量",1000);
        txInformation.put("成交时间","2020/10/8");
        txInformation.put("交易成交描述信息","交易成功");
        txInformation.put("发行方主体引用",gdCompanyID);
        txInformation.put("发行方名称","issue001");
        txInformation.put("投资方主体引用","accobj0001");
        txInformation.put("投资方名称","联合");
        txInformation.put("原持有方主体引用","acchobj001");
        txInformation.put("原持有方名称","zhagnsan");
        txInformation.put("对手方主体引用","acchobj002");
        txInformation.put("对手方名称","李四");
        txInformation.put("委托核验凭证","ddd.pdf");
        txInformation.put("成交核验凭证","erq.pdf");

        List<Map> mapList1 = new ArrayList<>();
        Map equityMap = new HashMap();
        equityMap.put("中介类型",0);
        equityMap.put("中介机构主体引用","obj001");
        equityMap.put("中介机构名称","中介");
        mapList1.add(equityMap);
        txInformation.put("交易中介信息",mapList1);


    }

    public void init05RegInfo() {
        log.info("初始化05登记数据结构");
        //05登记
        List<String> listRegFile = new ArrayList<>();
        listRegFile.add("verify.crt");
        registerInfo.clear();
        registerInfo.put("登记对象标识","regid00001");
        registerInfo.put("登记类型",0);
        registerInfo.put("登记流水号","regsno00001");
        registerInfo.put("登记时间","2020/7/8");
        registerInfo.put("登记主体引用",gdCompanyID);
        registerInfo.put("登记主体类型",0);
        registerInfo.put("权利登记单位",0);
        registerInfo.put("登记币种","156");
        registerInfo.put("变动额",10000);
        registerInfo.put("当前可用余额",10000);
        registerInfo.put("当前可用余额占比",78);
        registerInfo.put("质押变动额",10000);
        registerInfo.put("当前质押余额",10000);
        registerInfo.put("冻结变动额",10000);
        registerInfo.put("当前冻结余额",10000);
        registerInfo.put("持有状态",0);
        registerInfo.put("持有属性",0);
        registerInfo.put("来源类型",0);
        registerInfo.put("登记说明","登记联合股权项目产品");
        registerInfo.put("登记核验凭证",listRegFile);
        registerInfo.put("登记产品类型",0);
        registerInfo.put("登记产品引用","regobj00011");
        registerInfo.put("权利人账户引用","accoid00001");
        registerInfo.put("交易报告引用",0);
        registerInfo.put("名册主体引用","dd");
        registerInfo.put("权利类型",0);
        registerInfo.put("登记日期","2020/7/8");
        registerInfo.put("股东主体引用","haccobj001");
        registerInfo.put("股东主体类型",0);
        registerInfo.put("股份性质",0);
        registerInfo.put("认缴金额",10000);
        registerInfo.put("实缴金额",10000);
        registerInfo.put("持股比例",20);
        registerInfo.put("债权人主体引用","bondobj0001");
        registerInfo.put("债权人类型",0);
        registerInfo.put("认购数量",0);
        registerInfo.put("认购金额",10000);
        registerInfo.put("债权人联系方式","ws@wutongchain.com");

    }

    public void init06SettleInfo() {
        log.info("初始化06资金清算数据结构");
        settleInfo.clear();
        settleInfo.put("资金结算对象标识","stoid00001");
        settleInfo.put("结算机构主体引用","setobj00001");
        settleInfo.put("结算类型",0);
        settleInfo.put("结算流水号","00000001");
        settleInfo.put("结算时间","2020/8/7");
        settleInfo.put("交易报告引用","txobj00001");
        settleInfo.put("结算币种","156");
        settleInfo.put("结算金额",10000);
        settleInfo.put("结算说明","季度结算");
        settleInfo.put("结算操作凭证","operate.pdf");
        settleInfo.put("转出方银行代号","A000001");
        settleInfo.put("转出方银行名称","A000002");
        settleInfo.put("转出方银行账号","AX0000001");
        settleInfo.put("转出方账户引用","axobj0001");
        settleInfo.put("转出方账户名称","AX0000002");
        settleInfo.put("转出方发生前金额",1000);
        settleInfo.put("转出方发生后余额",10000);
        settleInfo.put("转入方银行代号","B000001");
        settleInfo.put("转入方银行名称","B000002");
        settleInfo.put("转入方银行账号","BX0000001");
        settleInfo.put("转入方账户引用","bxobj0001");
        settleInfo.put("转入方账户名称","BX0000002");
        settleInfo.put("转入方资金账号","BY0000002");
        settleInfo.put("转入方发生前金额",10000);
        settleInfo.put("转入方发生后余额",9000);

    }

    public void init07PublishInfo() {
        log.info("初始化07信披数据结构");

        //07信披监管数据
        List<String> listPubFile = new ArrayList<>();
        listPubFile.add("tix.pdf");

        disclosureInfo.clear();

        disclosureInfo.put("信批对象标识", "disoid00001");
        disclosureInfo.put("信批主体引用", "pubobj0001");
        disclosureInfo.put("期间起始日期", "2020/9/6");
        disclosureInfo.put("截止日期", "2020/9/6");
        disclosureInfo.put("报表类型", 0);
        disclosureInfo.put("期末总资产(元)", 100000);
        disclosureInfo.put("期末净资产(元)", "100000");
        disclosureInfo.put("总负债(元)", "100000");
        disclosureInfo.put("本期营业收入(元)", "100000");
        disclosureInfo.put("本期利润总额（元）", "100000");
        disclosureInfo.put("本期净利润（元）", "100000");
        disclosureInfo.put("现金流量（元）", "100000");
        disclosureInfo.put("是否有研发费用", "100000");
        disclosureInfo.put("研发费用（元）", "5000");
        disclosureInfo.put("资产负债表(PDF)", "4552.pdf");
        disclosureInfo.put("现金流量表(PDF)", "dd.pdf");
        disclosureInfo.put("利润表(PDF)", "dd445.pdf");
        disclosureInfo.put("重大事件类型", 0);
        disclosureInfo.put("文件列表", listPubFile);
        disclosureInfo.put("提报时间", "2020/9/6");
    }

}
