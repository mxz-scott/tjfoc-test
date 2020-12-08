package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SmartToken;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class smtInterfaceTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Store store = testBuilder.getStore();

    GoSmartToken st = new GoSmartToken();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    CertTool certTool = new CertTool();

    private static String tokenType;
    private static String tokenType2;
    private static String actualAmount1;


    String constFileName = "account_simple.wlang";
    String contractFileName = "account_simple.wlang";
    String HQuotaFileName = "account_simple_HQuota.wlang";

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.createSTAddresses();
        }

    }

    @Before
    public void beforeConfig() throws Exception {

        //安装smart token定制化合约
        installSmartAccountContract(contractFileName);

        actualAmount1 = "10000.123456";
        double timeStampNow = System.currentTimeMillis();
        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);

        List<Map>list = utilsClass.smartConstuctIssueToList(ADDRESS1, "test", actualAmount1);

        log.info("发行数字资产");
        tokenType = "soloIntf-"+UtilsClass.Random(6);
        String issueResp = smartIssueToken(tokenType,deadline,list);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("查询数字资产余额");
        String queryBalance = st.SmartGetBalanceByAddr(ADDRESS1, "");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType),containsString(actualAmount1));
        assertEquals(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType),containsString("test"));
        assertEquals(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType),containsString("true"));

    }


//    @Test
//    public void testSmartIssueReqInterface() throws Exception {
//        String userContract = smartAccoutCtHash;
//        String tokenType = "smtIntfIReq" + UtilsClass.Random(6);
//        Boolean reIssued = true;
//        BigDecimal timeStart = new BigDecimal(System.currentTimeMillis());
//        BigDecimal expire = new BigDecimal(System.currentTimeMillis() + 12356789);
//        BigDecimal active = new BigDecimal(0);
//        int maxLevel = 0;
//        List<Map> issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount1);
//        String extend = "\"ToAddress\":\"" + ADDRESS1+ "\"," + "\"Data\":\"dddddddd\"";
//
//        log.info("******************* test Issue Req  Parameter : userContract *******************");
//        log.info("合约名为空");
//        String isResp = multiSign.SmartIssueTokenReq("",tokenType,false,
//                expire,active,maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("Invalid parameter :Key: 'UserContract'"));
//
//
////        log.info("合约名为空格");
////        isResp = multiSign.SmartIssueTokenReq(" ",tokenType,false,
////                expire,active,maxLevel,issueToList,"");
////        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
////        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("Invalid parameter :Key: 'UserContract'"));
//
//
////        log.info("合约名为非法/不存在的合约名");
////        isResp = multiSign.SmartIssueTokenReq("12345678913245678",tokenType,false,
////                expire,active,maxLevel,issueToList,"");
////        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
////        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("12345678913245678"));
//
//
//
//        log.info("******************* test Issue Req  Parameter : tokenType *******************");
//
//        log.info("tokenType为空");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,"",false,
//                expire,active,maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("Invalid parameter :Key: 'TokenType'"));
//
//
////        log.info("tokenType为空格");
////        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash," ",false,
////                expire,active,maxLevel,issueToList,"");
////        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
////        assertEquals("tokenType is mandatory", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("tokenType字符串长度为300");
//        String testtokenType = UtilsClass.Random(300);
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,testtokenType,false,
//                expire,active,maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("data").contains(testtokenType));
//
//        log.info("******************* test Issue Req  Parameter : expireDate *******************");
//        log.info("expireDate为空：需手动测试");//手动测试 java不能带非空
//        log.info("expireDate为字母：需手动测试");//手动测试 java不能传不同类型参数
//
//        log.info("expireDate为0");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(0),active,maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("Invalid parameter :Key: 'ExpireDate'"));
//
//        log.info("expireDate为非13位时间戳");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(123456),active,maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("有效日期时间戳需要精确到毫秒"));
//
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal("123456789123456789"),active,maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("有效日期时间戳需要精确到毫秒"));
//
//        log.info("expireDate为过去的时间timeStart");//sdk端不校验 合约端进行校验
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                timeStart,active,maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
////        assertEquals("invalid parameter expireDate", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("expireDate时间早于activeDate");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() - 123456),new BigDecimal(System.currentTimeMillis() + 12345),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("激活日期时间戳必须小于有效期时间戳"));
//
//
//        log.info("******************* test Issue Req  Parameter : token *******************");
//        log.info("token list 为空");
//        issueToList.clear();
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
////        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
////        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("mandatory"));
//
//        log.info("token list 中amount为空");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("Invalid parameter :Key: 'TokenList[0].Amount'"));
//
//
//        log.info("token list 中amount为负数");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"-100");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("Token amount must be a valid number and less than 18446744073709", JSONObject.fromObject(isResp).getString("message"));
//
//
//        log.info("token list 中amount为字母");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"abcd");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("Token amount must be a valid number and less than 18446744073709", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("token list 中amount为0");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"0");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("Token amount must be a valid number and less than 18446744073709", JSONObject.fromObject(isResp).getString("message"));
//
//
//        log.info("token list 中amount为0.0000001");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"0");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("invalid parameter amount", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("token list 中amount7位有效数字");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"90.2345678");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getJSONObject("data").getString("msg").contains("\\\"Value\\\":90.234567"));
//
//        log.info("token list 中amount为小于最大数值临界值");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"18446744073708.999999");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getJSONObject("data").getString("msg").contains("\\\"Value\\\":18446744073708.999999"));
//
//        log.info("token list 中amount为最大数值");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"18446744073709");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getJSONObject("data").getString("msg").contains("\\\"Value\\\":18446744073709"));
//
//        log.info("token list 中amount为最小数字值");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"0.000001");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getJSONObject("data").getString("msg").contains("\\\"Value\\\":0.000001"));
//
//
//        log.info("token list 中amount超过最大数值");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"18446744073709.1");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("Token amount must be a valid number and less than 18446744073709", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("token list 中address为空");
//        issueToList = utilsClass.smartConstuctIssueToList("","123");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("mandatory"));
//
//
//        log.info("token list 中address为空格");
//        issueToList = utilsClass.smartConstuctIssueToList(" ","123");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("mandatory"));
//
//
//        log.info("token list 中address为非法地址格式");
//        issueToList = utilsClass.smartConstuctIssueToList("446add","123");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("invalid parameter", JSONObject.fromObject(isResp).getString("message"));
//
//
//        log.info("token list 中address为非数据库中的地址");
//        String AddrNotInDB = "4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB";
//        issueToList = utilsClass.smartConstuctIssueToList(AddrNotInDB,"123");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//
//
//        log.info("token list 为多个不超过上限");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"100");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS2,"100",issueToList);
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS3,"100",issueToList);
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS4,"100",issueToList);
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS5,"100",issueToList);
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains(ADDRESS1));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains(ADDRESS2));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains(ADDRESS3));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains(ADDRESS4));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains(ADDRESS5));
//
//        log.info("token list 为多个不超过上限,总额度超过上限");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"10446744073709");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS2,"8000000000000",issueToList);
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS3,"100",issueToList);
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("can not"));
//
//
//        log.info("token list 为多个不超过上限,相同地址");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"10446744073709");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"8000000000000",issueToList);
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals(true, JSONObject.fromObject(isResp).getString("message").contains("can not"));
//
//
//        log.info("******************* test Issue Req  Parameter : reissued *******************");
//        log.info("增发flag为false");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"50000");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//
//        log.info("增发flag为true");
//        issueToList = utilsClass.smartConstuctIssueToList(ADDRESS1,"50000");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                new BigDecimal(System.currentTimeMillis() + 123456),new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//
//
//        log.info("******************* test Issue Req  Parameter : activeDate *******************");
//        log.info("activeDate为空：需手动测试");//手动测试 java不能带非空
//        log.info("activeDate为字母：需手动测试");//手动测试 java不能传不同类型参数
//
//        log.info("activeDate为0");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,expire,
//                new BigDecimal(0),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("invalid parameter activeDate", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("activeDate为非13位时间戳");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,expire,
//                new BigDecimal(123456),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("invalid parameter activeDate", JSONObject.fromObject(isResp).getString("message"));
//
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,expire,
//                new BigDecimal("123456789123456789"),maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("invalid parameter activeDate", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("activeDate为过去的时间timeStart");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                expire,timeStart,maxLevel,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("invalid parameter expireDate", JSONObject.fromObject(isResp).getString("message"));
//
//
//
//        log.info("******************* test Issue Req  Parameter : level *******************");
//
//        log.info("level为负数");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                expire,active,-100,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("invalid parameter level", JSONObject.fromObject(isResp).getString("message"));
//
//        log.info("level为小数");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                expire,active,5.22,issueToList,"");
//        assertEquals("400", JSONObject.fromObject(isResp).getString("state"));
//        assertEquals("Invalid parameter", JSONObject.fromObject(isResp).getString("message"));
//
//
//        log.info("******************* test Issue Req  Parameter : extend *******************");
//        log.info("extend为不匹配的json字符串");
//        isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,false,
//                expire,active,5.22,issueToList,"12345646");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//
//        String sigMsg1 = JSONObject.fromObject(isResp).getJSONObject("data").getString("sigMsg");
//        assertEquals(sigMsg1,String.valueOf(Hex.encodeHex(
//                JSONObject.fromObject(isResp).getJSONObject("data").getString("msg").getBytes(StandardCharsets.UTF_8))));
//
//        String tempSM3Hash = certTool.getSm3Hash(PEER4IP,sigMsg1);
//        String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",tempSM3Hash,"hex");
//
//        String pubkey = utilsClass.readStringFromFile(testDataPath + "cert/SM2/keys1/pubkey.pem").replaceAll("\r\n","\n");
//
//        String approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,cryptMsg,pubkey);
//
//    }
//
//    @Test
//    public void smartIssueApproveInterfaceTest()throws Exception{
//
//
//        log.info("******************* test Issue Appr  Parameter : sigMsg *******************");
//        String tokenTest01 = "smtIntfIAppr" + UtilsClass.Random(6);
//        String isResp = multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenTest01,true,
//                new BigDecimal(System.currentTimeMillis() + 12356789),new BigDecimal(0),
//                0,utilsClass.smartConstuctIssueToList(ADDRESS1,"100"),"");
//        assertEquals("200", JSONObject.fromObject(isResp).getString("state"));
//
//        String sigMsg1 = JSONObject.fromObject(isResp).getJSONObject("data").getString("sigMsg");
//        String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",certTool.getSm3Hash(PEER4IP,sigMsg1),"hex");
//        String pubkey = utilsClass.readStringFromFile(testDataPath + "cert/SM2/keys1/pubkey.pem").replaceAll("\r\n","\n");
//
//
//        log.info("所有字段为空");
//        String approveResp = multiSign.SmartIssueTokenApprove("","","");
//        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
//        assertEquals("mandatory", JSONObject.fromObject(approveResp).getString("message"));
//
//
//        log.info("sigMsg 为空");
//        approveResp = multiSign.SmartIssueTokenApprove("",cryptMsg,pubkey);
//        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
//        assertEquals("mandatory", JSONObject.fromObject(approveResp).getString("message"));
//
//
//        log.info("sigMsg与cryptMsg不匹配");
//        approveResp = multiSign.SmartIssueTokenApprove("123456789",cryptMsg,pubkey);
//        assertEquals("200", JSONObject.fromObject(approveResp).getString("state"));
//
//
//        log.info("******************* test Issue Appr  Parameter : sigCrypt *******************");
//        log.info("sigCrypt 为空");
//        approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,"",pubkey);
//        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
//        assertEquals("mandatory", JSONObject.fromObject(approveResp).getString("message"));
//
//        log.info("sigCrypt格式错误");
//        cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",certTool.getSm3Hash(PEER4IP,sigMsg1),"base64");
//        approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,"",pubkey);
//        assertEquals("200", JSONObject.fromObject(approveResp).getString("state"));
//
//        log.info("sigCrypt不匹配的公私钥");
//        cryptMsg = certTool.sign(PEER4IP ,PRIKEY2,"",certTool.getSm3Hash(PEER4IP,sigMsg1),"base64");
//        approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,"",pubkey);
//        assertEquals("200", JSONObject.fromObject(approveResp).getString("state"));
//
//
//
//        log.info("******************* test Issue Appr  Parameter : pubKey *******************");
//        log.info("pubKey 为空");
//        approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,cryptMsg,"");
//        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
//        assertEquals("mandatory", JSONObject.fromObject(approveResp).getString("message"));
//
//        log.info("pubKey格式错误");
//        cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",certTool.getSm3Hash(PEER4IP,sigMsg1),"base64");
//        approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,cryptMsg,pubkey.replaceAll("\n",""));
//        assertEquals("200", JSONObject.fromObject(approveResp).getString("state"));
//
//    }

//    @Test
//    public void smartTransferInterfaceTest()throws Exception{
//        String fromAddr = ADDRESS1;
//        List<Map> list = soloSign.constructToken(ADDRESS3,tokenType,"100.25");
//        List<Map> list2 = soloSign.constructToken(ADDRESS5,tokenType2,"200.555",list);
//
//
//        log.info("转出地址为空");
//        String transferInfo = multiSign.SmartTransfer("",PRIKEY1,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("Parameter MultiAddr is mandatory",JSONObject.fromObject(transferInfo).getString("message"));
//
//        log.info("转出地址不存在");
//        transferInfo = multiSign.SmartTransfer("4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB",PRIKEY1,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("Parameter MultiAddr is mandatory",JSONObject.fromObject(transferInfo).getString("message"));
//
//
//        log.info("私钥为空");
//        transferInfo = multiSign.SmartTransfer(fromAddr,"","",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("Parameter 'PriKey' is mandatory",JSONObject.fromObject(transferInfo).getString("message"));
//
//        log.info("私钥不匹配");
//        transferInfo = multiSign.SmartTransfer(fromAddr,PRIKEY4,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferInfo).getString("message").contains("not match"));
//
//
//        log.info("转向list为空");
//        list.clear();
//        transferInfo = multiSign.SmartTransfer(fromAddr,PRIKEY4,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferInfo).getString("message").contains("Invalid parameter"));
//
//        log.info("转向list中地址为空");
//        list = soloSign.constructToken("",tokenType,"100.25");
//        transferInfo = multiSign.SmartTransfer(fromAddr,PRIKEY4,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferInfo).getString("message").contains("Invalid parameter"));
//
//
//        log.info("转向list中tokentype为空");
//        list = soloSign.constructToken(ADDRESS3,"","100.25");
//        transferInfo = multiSign.SmartTransfer(fromAddr,PRIKEY4,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferInfo).getString("message").contains("Invalid parameter"));
//
//
//        log.info("转向list中amount为空");
//        list = soloSign.constructToken(ADDRESS3,tokenType,"");
//        transferInfo = multiSign.SmartTransfer(fromAddr,PRIKEY4,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferInfo).getString("message").contains("Invalid parameter"));
//
//
//        log.info("转向list中amount负数");
//        list = soloSign.constructToken(ADDRESS3,tokenType,"-455");
//        transferInfo = multiSign.SmartTransfer(fromAddr,PRIKEY4,"",list,"", "");
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferInfo).getString("message").contains("Invalid parameter"));
//
//        log.info("extend格式不匹配合约中的格式");
//        list = soloSign.constructToken(ADDRESS3,tokenType,"100");
//        transferInfo = multiSign.SmartTransfer(fromAddr,PRIKEY4,"",list,"", "123456");
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//
//        String txHash = JSONObject.fromObject(transferInfo).getString("data");
////        sleepAndSaveInfo(SLEEPTIME);
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",
//                JSONObject.fromObject(store.GetTxDetail(txHash)).getString("state"));
//
//    }
//
//    @Test
//    public void smartRecycleInterfaceTest(){
//        log.info("回收地址为空");
//        String balaceResp = multiSign.SmartRecyle(ADDRESS1,PRIKEY1,"",tokenType,"10","");
//        assertEquals("200",JSONObject.fromObject(balaceResp).getString("state"));
//        assertEquals("Parameter MultiAddr is mandatory",JSONObject.fromObject(balaceResp).getString("message"));
//
//        log.info("私钥为空");
//        balaceResp = multiSign.SmartRecyle(ADDRESS1,"","",tokenType,"10","");
//        assertEquals("200",JSONObject.fromObject(balaceResp).getString("state"));
//        assertEquals("Parameter 'Prikey' is mandatory",JSONObject.fromObject(balaceResp).getString("message"));
//
//        log.info("私钥不匹配");
//        balaceResp = multiSign.SmartRecyle(ADDRESS1,PRIKEY4,"",tokenType,"10","");
//        assertEquals("200",JSONObject.fromObject(balaceResp).getString("state"));
//        assertEquals("not match",JSONObject.fromObject(balaceResp).getString("message"));
//
//
//        log.info("tokenType为空");
//        balaceResp = multiSign.SmartRecyle(ADDRESS1,PRIKEY1,"","","10","");
//        assertEquals("200",JSONObject.fromObject(balaceResp).getString("state"));
//        assertEquals("Parameter TokenType is mandatory",JSONObject.fromObject(balaceResp).getString("message"));
//
//        log.info("amount为空");
//        balaceResp = multiSign.SmartRecyle(ADDRESS1,PRIKEY1,"",tokenType,"","");
//        assertEquals("200",JSONObject.fromObject(balaceResp).getString("state"));
//        assertEquals("Parameter Amount is mandatory",JSONObject.fromObject(balaceResp).getString("message"));
//    }
//
//
//    @Test
//    public void smartBalanceInterfaceTest(){
//        log.info("查询地址为空");
//        String balaceResp = multiSign.SmartGetBalanceByAddr("",tokenType);
//        assertEquals("200",JSONObject.fromObject(balaceResp).getString("state"));
//        assertEquals("Parameter 'Address' is mandatory",JSONObject.fromObject(balaceResp).getString("message"));
//    }
//
//    @Test
//    public void smartGetOwnerAddrInterfaceTest(){
//        log.info("查询tokenType为空");
//        String balaceResp = multiSign.SmartGetOwnerAddrs("");
//        assertEquals("200",JSONObject.fromObject(balaceResp).getString("state"));
//        assertEquals("Parameter tokenType is mandatory",JSONObject.fromObject(balaceResp).getString("message"));
//    }
//
//
    public void installSmartAccountContract(String abfileName)throws Exception{
        WVMContractTest wvmContractTestSA = new WVMContractTest();
        UtilsClass utilsClassSA = new UtilsClass();
        CommonFunc commonFuncTeSA = new CommonFunc();

        //如果smartAccoutCtHash为空或者contractFileName不为constFileName 即"wvm\\account_simple.wlang" 时会重新安装
        if(smartAccoutCtHash.equals("") || (!contractFileName.equals(constFileName))){
            //安装
            String response =wvmContractTestSA.wvmInstallTest(abfileName,"");
            assertEquals("200",JSONObject.fromObject(response).getString("state"));
            commonFuncTeSA.sdkCheckTxOrSleep(commonFuncTeSA.getTxHash(response,utilsClassSA.sdkGetTxHashType20),
                    utilsClassSA.sdkGetTxDetailTypeV2,SLEEPTIME);
            smartAccoutCtHash = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        }
    }

    //单签账户目前的签名公私钥对为PUBKEY1 PRIKEY1
    public String smartIssueToken(String tokenType,BigDecimal deadline,List<Map> issueToList)throws Exception{
        String isResult= st.SmartIssueTokenReq(smartAccoutCtHash,tokenType,true,
                deadline,new BigDecimal(0),0,issueToList,"");
        String sigMsg1 = JSONObject.fromObject(isResult).getJSONObject("data").getString("sigMsg");

        String tempSM3Hash = certTool.getSm3Hash(PEER4IP,sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",tempSM3Hash,"hex");

        String pubkey = utilsClass.readStringFromFile(testDataPath + "cert/SM2/keys1/pubkey.pem").replaceAll("\r\n","\n");

        String approveResp = st.SmartIssueTokenApprove(sigMsg1,cryptMsg,pubkey);
        return approveResp;
    }
}
