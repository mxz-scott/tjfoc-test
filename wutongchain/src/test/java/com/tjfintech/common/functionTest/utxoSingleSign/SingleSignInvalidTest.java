package com.tjfintech.common.functionTest.utxoSingleSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PRIKEY1;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SingleSignInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;//设置发行的tokentype
    private static String MaxValue;
    private static String MaxValuePlus1;
    CommonFunc commonFunc = new CommonFunc();
    /**
     * TC1332-创建单签的账号
     * @throws Exception
     */
    @Before
    public void updateMaxValue() throws Exception {

        if(MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME);
        }

        if (UtilsClass.PRECISION == 10) {
            MaxValue = "1844674407";
            MaxValuePlus1 = "1844674408";
        }else {
            MaxValue = "18446744073709";
            MaxValuePlus1 = "18446744073711";
        }

    }

    @Test
    public void TC1332_genaddress() throws Exception {
        Map<String,String> Data =  new HashMap<>();
        Data.put("", "failed to decode public key");//	PubKey字段传入空值
        Data.put("abc", "Public key must be base64 string");//PubKey字段传入非base64编码的值
        Data.put("YWJj", "failed to decode public key");//	PubKey字段传入一些没有意义的字符的base64编码的值
        //遍历Data集合
        Set<String> strings = Data.keySet();
        for(String key: strings){
            String value=Data.get(key);
            String genaddress = soloSign.genAddress(key);

        }

    }

    /**
     * 单签发行token
     *
     * @throws Exception
     */
    @Test
    public void TC1333_issuetoken() throws Exception {
        tokenType = UtilsClass.Random(6);
        String isResult;
        isResult = soloSign.issueToken("", tokenType, "100000", "", ADDRESS1);//PriKey字段传入空值
        assertThat(isResult, containsString("Private key is mandatory"));
        isResult = soloSign.issueToken("abc", tokenType, "100000", "", ADDRESS1);//PriKey字段传入非base64编码的值
        assertThat(isResult, containsString("Private key must be base64 string"));
        isResult = soloSign.issueToken("YWJjeHg=", tokenType, "100000", "", ADDRESS1);//PriKey字段传入一些没有意义的字符的base64编码的值
        assertThat(isResult,containsString("unsupport pem file"));
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, "", "100000", "", ADDRESS1);//	TokenType字段传入空值
        assertThat(isResult,containsString("TokenType shouldn't be empty"));

        //Amount字段传入负值
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, "-1", "", ADDRESS1);
        assertThat(isResult,containsString("Amount must be greater than 0 and less than "+MaxValue));

        //Amount字段传入空值
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, "", "", ADDRESS1);
        assertThat(isResult,containsString("Amount must be greater than 0 and less than "+MaxValue));

        //Amount字段传入超过最大值
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, MaxValuePlus1, "", ADDRESS1);
        assertThat(isResult,containsString("Amount must be greater than 0 and less than "+MaxValue));

        //Amount字段传入小数点后位数超出十位数
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, "1.1234567789123", "", ADDRESS1);
        assertThat(isResult,containsString("200"));


    }

    /**
     * Tc 249发行token的非法测试
     * 1	发行token的金额超过允许的最大值
     * 2	发行token的金额为0
     * 3	发行token的金额为负数
     * 4	发行token的金额为非数字字符
     * 5	token type为空
     */
    @Test
    public void TC249_IssueTokenInvalid() {
        String tokenTypeInvalid = "SOLOTC-" + UtilsClass.Random(2);
        String issueInfo1 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "900000000000000000000000000000", "发行token",ADDRESS1);
        String issueInfo2 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "0", "发行token",ADDRESS1);
        String issueInfo3 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "-140", "发行token",ADDRESS1);
        String issueInfo4 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "abc", "发行token",ADDRESS1);
        String issueInfo5 = soloSign.issueToken(PRIKEY1, "", "1000", "发行token",ADDRESS1);
//        assertThat(issueInfo1, containsString("400"));
//        assertThat(issueInfo2, containsString("400"));
//        assertThat(issueInfo3, containsString("400"));
//        assertThat(issueInfo4, containsString("400"));
//        assertThat(issueInfo5, containsString("400"));
        assertThat(issueInfo1, containsString("Amount must be greater than 0 and less than "+MaxValue));
        assertThat(issueInfo2, containsString("Amount must be greater than 0 and less than "+MaxValue));
        assertThat(issueInfo3, containsString("Amount must be greater than 0 and less than "+MaxValue));
        assertThat(issueInfo4, containsString("Amount must be greater than 0 and less than "+MaxValue));
        assertThat(issueInfo5,containsString("TokenType shouldn't be empty"));
        log.info("查询归集地址中token余额");
        String response1 = soloSign.Balance(PRIKEY1, tokenTypeInvalid);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("0"));

    }

    /**
     * 转账交易
     */
    @Test
    public void TC1336_transfer(){
        String transfer;
        Map<String, String> map = new HashMap<>();
        map.put("toAddress","3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY"); //转入的账户地址
        map.put("tokenType","TestToken"); //Token类型
        map.put("amount","20000");    //转出的数量
        List<Map> list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, "", "111");//PriKey字段传入空值
        assertThat(transfer,containsString("Private key is mandatory"));
        transfer = soloSign.Transfer(list, "abc", "111");//PriKey字段传入非base64编码的值
        assertThat(transfer,containsString("Private key must be base64 string"));
        transfer = soloSign.Transfer(list, "YWJj", "111");//	PriKey字段传入一些没有意义的字符的base64编码的值
        assertThat(transfer,containsString("unsupport pem file"));

        map.put("toAddress",""); //转入的账户地址
        map.put("tokenType","TestToken"); //Token类型
        map.put("amount","20000");    //转出的数量
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");	//TokenAddr字段传入空值
        assertThat(transfer,containsString("invalid address"));

        map.put("tokenType",""); //Token类型
        map.put("toAddress","3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY");
        map.put("amount","20000");    //转出的数量
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");	//TokenType字段传入空值
        assertThat(transfer,containsString("Token type cannot be empty"));

        //Amount字段传入空值
        map.put("amount","");    //转出的数量
        map.put("toAddress","3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY"); //转入的账户地址
        map.put("tokenType","TestToken"); //Token类型
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");
        assertThat(transfer,containsString("Token amount must be a valid number and less than "+MaxValue));

        //Amount字段传入负值
        map.put("amount","-1");    //转出的数量
        map.put("toAddress","3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY"); //转入的账户地址
        map.put("tokenType","TestToken"); //Token类型
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");
        assertThat(transfer,containsString("Token amount must be a valid number and less than "+MaxValue));

        //Amount字段传入数量超出余额
        map.put("toAddress","3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY"); //转入的账户地址
        map.put("tokenType","TestToken11211"); //Token类型
        map.put("amount","2222222");    //转出的数量
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY1, "111");//PriKey字段传入空值
        assertThat(transfer,containsString("insufficient balance"));

    }
    /**
     * TC248 转账金额非法测试
     * 1    转账金额为0
     * 2	转账金额为非数字字符
     * 3	转账金额为负数
     * 4	转账金额超过最大值
     * 5	转账token type不存在
     */
    @Test
    public void TC248_transferInvalid() throws Exception {
        String transferData = "单签地址向" + PUBKEY3 + "转账非法测试";


        List<Map> list1 = soloSign.constructToken(ADDRESS3, tokenType, "0");
        List<Map> list2 = soloSign.constructToken(ADDRESS3, tokenType, "abc");
        List<Map> list3 = soloSign.constructToken(ADDRESS3, tokenType, "-10");
        List<Map> list4 = soloSign.constructToken(ADDRESS3, tokenType, "92000000000000000000000000000000000000000000000000");
        List<Map> list5 = soloSign.constructToken(ADDRESS3, "nullToken", "100.25");
        log.info(transferData);

        String transferInfo1 = soloSign.Transfer(list1, PRIKEY1, transferData);
        String transferInfo2 = soloSign.Transfer(list2, PRIKEY1, transferData);
        String transferInfo3 = soloSign.Transfer(list3, PRIKEY1, transferData);
        String transferInfo4 = soloSign.Transfer(list4, PRIKEY1, transferData);
        String transferInfo5 = soloSign.Transfer(list5, PRIKEY1, transferData);

//        assertThat(transferInfo1, containsString("400"));
//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
        assertThat(transferInfo1, containsString("Amount must be greater than 0 and less than "+MaxValue));
        assertThat(transferInfo2, containsString("Token amount must be a valid number and less than "+MaxValue));
        assertThat(transferInfo3, containsString("Token amount must be a valid number and less than "+MaxValue));
        assertThat(transferInfo4, containsString("Token amount must be a valid number and less than "+MaxValue));
        assertThat(transferInfo5, containsString("insufficient balance"));
    }

    /**
     * 查询余额
     */
    @Test
    public void TC1337_balance(){

        Map<String,String> Data =  new HashMap<>();
        Data.put("", "must specify private key");//	PriKey字段传入空值
        Data.put("abc", "Private key must be base64 string");//PriKey字段传入非base64编码的值
        Data.put("YWJjeHg=", "unsupport pem file");//	PriKey字段传入一些没有意义的字符的base64编码的值
        //遍历Data集合
        Set<String> strings = Data.keySet();
        for(String key: strings){
            String value=Data.get(key);
            String data = soloSign.Balance(key, "TestToken");
            assertThat(data,containsString(value));
        }

    }


}


