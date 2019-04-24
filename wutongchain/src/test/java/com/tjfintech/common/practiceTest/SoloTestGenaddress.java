package com.tjfintech.common.practiceTest;


import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.ADDRESS1;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoloTestGenaddress {
    TestBuilder testBuilder= TestBuilder.getInstance();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;//设置发行的tokentype

    /**
     * TC1332-创建单签的账号
     * @throws Exception
     */

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
            assertThat(genaddress,containsString(value));
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

        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, "-1", "", ADDRESS1);	//Amount字段传入负值
        assertThat(isResult,containsString("Amount must be greater than 0 and less than 900000000"));
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, "", "", ADDRESS1);//Amount字段传入空值
        assertThat(isResult,containsString("Amount must be greater than 0 and less than 900000000"));
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, "1.1234567789123", "", ADDRESS1);//Amount字段传入小数点后位数超出十位数
        assertThat(isResult,containsString("200"));
        isResult = soloSign.issueToken(utilsClass.PRIKEY2, tokenType, "1800000000", "", ADDRESS1);//Amount字段传入超过9亿，18亿，20亿值
        assertThat(isResult,containsString("Amount must be greater than 0 and less than 900000000"));
    }

    /**
     * 转账交易
     */
    @Test
    public void TC1336_transfer(){
        String transfer;
        Map<String, String> map = new HashMap<>();
        map.put("ToAddr","3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY"); //转入的账户地址
        map.put("TokenType","TestToken"); //Token类型
        map.put("Amount","20000");    //转出的数量
        List<Map> list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, "", "111");//PriKey字段传入空值
        assertThat(transfer,containsString("Private key is mandatory"));
        transfer = soloSign.Transfer(list, "abc", "111");//PriKey字段传入非base64编码的值
        assertThat(transfer,containsString("Private key must be base64 string"));
        transfer = soloSign.Transfer(list, "YWJj", "111");//	PriKey字段传入一些没有意义的字符的base64编码的值
        assertThat(transfer,containsString("unsupport pem file"));
        map.put("ToAddr",""); //转入的账户地址
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");	//TokenAddr字段传入空值
        assertThat(transfer,containsString("invalid address"));
        map.put("TokenType",""); //Token类型
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");	//TokenType字段传入空值
        assertThat(transfer,containsString("Token type cannot be empty"));
        map.put("Amount","");    //转出的数量
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");	//Amount字段传入空值
        assertThat(transfer,containsString("Token amount must be a valid number and less than 900000000"));
        map.put("Amount","-1");    //转出的数量
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY2, "111");	//Amount字段传入负值
        assertThat(transfer,containsString("Token amount must be a valid number and less than 900000000"));
        //Amount字段传入数量超出余额
        map.put("ToAddr","3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY"); //转入的账户地址
        map.put("TokenType","TestToken11211"); //Token类型
        map.put("Amount","2222222");    //转出的数量
        list =new ArrayList<>();
        list.add(map);
        transfer = soloSign.Transfer(list, utilsClass.PRIKEY1, "111");//PriKey字段传入空值
        assertThat(transfer,containsString("insufficient balance"));

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


