package com.tjfintech.common.functionTest.tokenModuleTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
//import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenInterfaceTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    String AddrNotInDB = "4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB";
//    String errParamCode = "\"state\": 400";
//    String errParamMsg = "client parameter error";
    String errParamMsgIss = "address,tokentype,amount and colladdr should not be empty!";
    String errParamMsgDes = "address,amount,tokentype and list should not be empty!";
    String errParamMsgTrf = "address,des address,tokentype and amount should not be empty!";
    String errParamMsgDes2 = "tokentype should not be empty!";
    String errInvalidAddr = "invalid address";
    String specChar = "!\"#$%&'()*+,-./:;<=>?@[\\]^`{|}~¥¦§¨©ª«¬®¯°±²³´中“”お⛑";
    String specChar2 = "!#$%&'()*+,-./:;=?@~";
    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenMultiAddr1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.updatePubPriKey();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @Test
    public void createAccountInterfaceTest()throws Exception{
        String entityID = "";
        String entityName = "";
        String groupID = "";
        String comments = "";
        ArrayList<String> listTag = new ArrayList<>();

        //id为空
        String createResp = tokenModule.tokenCreateAccount("","test","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;"));

        //id为空格
        createResp = tokenModule.tokenCreateAccount(" ","test","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("entityID or entityName can't just be a space or more!"));

        //id界限长度上限 32 含所有支持字符类型 T
        entityID = "0RTTdag_" + UtilsClass.Random(24);
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //id界限长度上限不含 _ T
        entityID = "aY" + UtilsClass.Random(30);
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //id为已存在id的一部分 T
        createResp = tokenModule.tokenCreateAccount(entityID.substring(0,10),"test","","",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //id为已存在id name也是已存在的 T
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("Error 1062: Duplicate entry '" + entityID + "' for key 'id'"));

        //id为已存在id 字母的lowercase,20200831改为数据库判断键值，id大小写不敏感
        createResp = tokenModule.tokenCreateAccount(entityID.toLowerCase(),"test"+ UtilsClass.Random(6),"","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("Error 1062: Duplicate entry '" + entityID.toLowerCase() + "' for key 'id'"));

        //id为已存在id 字母的uppercase
        createResp = tokenModule.tokenCreateAccount(entityID.toUpperCase(),"test"+ UtilsClass.Random(6),"","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("Error 1062: Duplicate entry '" + entityID.toUpperCase() + "' for key 'id'"));

        //id为已存在id name不存在 T
        createResp = tokenModule.tokenCreateAccount(entityID,"test"+ UtilsClass.Random(6),"","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("Error 1062: Duplicate entry '" + entityID + "' for key 'id'"));

        //id长度超过上限 33 F 当前提示不合理
        entityID = UtilsClass.Random(33);
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //id长度超过上限 含_ 33 F 当前提示不合理
        entityID = "_" + UtilsClass.Random(32);
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //id长度超过上限 128 F 当前提示不合理
        entityID = UtilsClass.Random(128);
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));



        //不支持特殊字符 20200324修改为支持
        for(char testchar : specChar.toCharArray()){
            entityID = String.valueOf(testchar);
            log.info("*********************test char " + entityID);
            createResp = tokenModule.tokenCreateAccount(entityID,"test","","",listTag);
            assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        }

        //xxs脚本
        entityID =  "<SCRIPT SRC=s.js></SCRIPT>";
        createResp = tokenModule.tokenCreateAccount(entityID,UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        log.info("test parameter entityName");
        //name为空
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),"","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;"));

        //name为空格
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6)," ","","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("entityID or entityName can't just be a space or more!"));


        //name长度上限32位不含_ T
        entityName = "0abcERT" + UtilsClass.Random(25);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度上限32位含_ T
        entityName = "_0abcERT" + UtilsClass.Random(24);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度上限32位含_ T
        entityName = "_" + UtilsClass.Random(31);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度超过上限32位 33
        entityName = UtilsClass.Random(33);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //name长度超过上限32位含_ 33
        entityName =  "__" + UtilsClass.Random(31);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //name长度超过上限32位 128 F
        entityName = UtilsClass.Random(128);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //name已存在 id不存在
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),"test","","",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //支持特殊字符
        for(char testchar : specChar.toCharArray()){
            entityName = String.valueOf(testchar);
            log.info("*********************test char " + entityName);
            createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",listTag);
            assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        }

        entityName =  "<SCRIPT SRC=s.js></SCRIPT>";
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        log.info("test parameter group id");

        //groupID长度31位
        groupID = "_123WdeS" + UtilsClass.Random(23);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //groupID长度上限32位
        groupID = "_123WdeSa" + UtilsClass.Random(23);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //groupID长度上限32位含_
        groupID = "__" + UtilsClass.Random(30);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //groupID长度超过上限32位不含_
        groupID = UtilsClass.Random(33);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //groupID长度超过上限32位 128
        groupID = UtilsClass.Random(128);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //groupID长度超过上限32位含_ 33
        groupID =  "__" + UtilsClass.Random(31);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //支持特殊字符
        for(char testchar : specChar.toCharArray()){
            groupID = String.valueOf(testchar);
            log.info("*********************test char " + groupID);
            createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
            assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        }

        //使用xss字符
        groupID = "<SCRIPT SRC=s.js></SCRIPT>";
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        groupID = "test" + Random(3);
        log.info("test parameter tags");

        listTag.clear();
        //测试多个字符 长度为127单个元素
        listTag.add(UtilsClass.Random(127));
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        listTag.clear();
        //测试多个128个字符 长度为128单个
        listTag.add(UtilsClass.Random(128));
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        listTag.clear();
        //测试长度为129单个
        listTag.add(UtilsClass.Random(129));
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //测试长度为257单个
        listTag.add(UtilsClass.Random(257));
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //tag为5个短字符串组成
        listTag.clear();
        listTag.add("_2aR");
        listTag.add("0_Fg");
        listTag.add(" ");//仅有空格
        listTag.add("bl ank");//包含空格
        listTag.add(specChar);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        listTag.clear();
        //测试多个128个字符 4个元素
        listTag.add("_1aR" + UtilsClass.Random(28));
        listTag.add("0_Fg" + UtilsClass.Random(28));
        listTag.add("a_fK" + UtilsClass.Random(28));
        listTag.add("T_4c" + UtilsClass.Random(28));
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        listTag.clear();
        //测试多个128个字符 5个元素
        listTag.add("_1aR" + UtilsClass.Random(28));
        listTag.add("0_Fg" + UtilsClass.Random(28));
        listTag.add("a_fK" + UtilsClass.Random(28));
        listTag.add("T_4c" + UtilsClass.Random(20));
        listTag.add(UtilsClass.Random(8));
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        listTag.clear();
        //测试多个数组 4个单字符
        for(int i = 0;i<4;i++) {
            listTag.add(UtilsClass.Random(1));
        }
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        listTag.clear();
        //测试多个数组 5个单字符 数组长度上限
        for(int i = 0;i<5;i++) {
            listTag.add(UtilsClass.Random(1));
        }
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        listTag.clear();
        //测试多个数组 6个数组
        for(int i = 0;i<6;i++) {
            listTag.add(UtilsClass.Random(1));
        }
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));


        listTag.clear();
        //测试多个数组 128个数组
        for(int i = 0;i<128;i++) {
            listTag.add(UtilsClass.Random(1));
        }
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));


        listTag.clear();
        //测试多个数组 129个数组 F
        for(int i = 0;i<129;i++) {
            listTag.add(UtilsClass.Random(1));
        }
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));


        listTag.clear();
        //测试超过128个字符 129 4个数组
        listTag.add("_1aR" + UtilsClass.Random(29));
        listTag.add("0 Fg" + UtilsClass.Random(28));
        listTag.add("a_fK" + UtilsClass.Random(28));
        listTag.add("T_4c" + UtilsClass.Random(28));
        log.info("*********************test char " + groupID);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));


        listTag.clear();
        //测试超过128个字符 129 5个数组
        listTag.add("_1aR" + UtilsClass.Random(29));
        listTag.add("0 Fg" + UtilsClass.Random(28));
        listTag.add("a_fK" + UtilsClass.Random(28));
        listTag.add("T_4c" + UtilsClass.Random(20));
        listTag.add(UtilsClass.Random(9));
        log.info("*********************test char " + groupID);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //测试包含特殊字符
        //支持特殊字符
        for(char testchar : specChar.toCharArray()){
            String temp = String.valueOf(testchar);
            listTag.clear();
            listTag.add("_1aR" + temp + UtilsClass.Random(3));

            createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
            assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        }

        //测试tag中包含xss字符
        listTag.clear();
        listTag.add("<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>");
        listTag.add("<IMG SRC=”jav ascript:alert(‘XSS’);”>");
        listTag.add("<script>z=z+’.net/1.’</script>");
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,"",listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        log.info("test parameter comments");
        groupID = "test" +Random(4);
        listTag.clear();
        listTag.add("test params");

        //测试包含特殊字符 无异常
        comments = specChar;
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //comments长度上限127位不含中文
        comments = UtilsClass.Random(127);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //comments长度上限128位不含中文
        comments = UtilsClass.Random(128);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        //comments长度上限128位含中文
        comments = "测试" + UtilsClass.Random(126);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        //comments长度127位
        comments = "中文" + UtilsClass.Random(125);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //comments长度129
        comments = UtilsClass.Random(129);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //comments长度 300
        comments = UtilsClass.Random(300);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  entityID(len):1-32  entityName(len):1-32  groupID(len):0-32  tags(array len):0-5,total:0-128  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //comments使用xss字符串
        comments =  "<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>";
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
    }

    @Test
    public void createMultiAccountByPubkeyInterfaceTest()throws Exception{

        Map<String, Object> pubkeys = new HashMap<>();
        String name = "createByPub";
        int minSignatures = 1;
        String groupID = "testid";//0324移除groupID
        String comments = "create multi address";
        ArrayList<String> listTag = new ArrayList<>();//0324移除tag

        log.info("test parameter pubkey");
        //pubkeys仅有一个且为空
        String createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("n[0](number of addresses) \\u003c minSignature[1]"));

        //pubkeys 一个地址 单签地址
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//        assertEquals(true,createResp.contains("Need more than one address"));
        assertEquals(true,createResp.contains("创建多签地址的公钥个数或者地址个数不能等于"));


        //pubkey 为非api数据库地址公钥
        pubkeys.clear();
        pubkeys.put("1",PUBKEY1);
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//        assertEquals(true,createResp.contains("address["+tokenMultiAddr1+"] not exist!;error:sql: no rows in result set"));
        assertEquals(true,createResp.contains("创建多签地址的公钥个数或者地址个数不能等于1"));


        //addresses 一个地址 单签地址的一部分
        pubkeys.clear();
        pubkeys.put("1",PUBKEY1.substring(10));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("创建多签地址的公钥个数或者地址个数不能等于1!"));


        //addresses 两个 两个都为空
        pubkeys.clear();
        pubkeys.put("1","");
        pubkeys.put("2","");
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("不能传入相同公钥!"));

        //addresses 两个 其中一个为空 一个为单签地址
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2","");
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("空字符串不能作为公钥传入!"));

        //addresses 两个 其中一个为空 一个为多签地址
        pubkeys.clear();
        pubkeys.put("1",tokenMultiAddr1);
        pubkeys.put("2","");
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("illegal base64 data at input byte 48"));


        //addresses 两个 其中一个单签 一个多签
        pubkeys.clear();
        pubkeys.put("1",tokenAccount1);
        pubkeys.put("2",tokenMultiAddr1);
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("illegal base64 data at input byte 48"));

        //addresses 两个 地址相同
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("不能传入相同公钥!"));

        //addresses 三个 其中一个为空
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3","");
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("空字符串不能作为公钥传入!"));

        //addresses 三个 其中一个为空 一个非法
        pubkeys.clear();
        pubkeys.put("1",tokenAccount1);
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3","");
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("illegal base64 data at input byte 48"));



        //addresses 三个 其中两个为空
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2","");
        pubkeys.put("3","");
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("不能传入相同公钥!"));

        //addresses 三个 一个非api公钥 两个api单签地址公钥
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3",PUBKEY1);
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 三个 两个相同
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("3",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("不能传入相同公钥!"));


        //addresses 4个
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount3)).getString("data"));
        pubkeys.put("4",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount4)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 7个
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount3)).getString("data"));
        pubkeys.put("4",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount4)).getString("data"));
        pubkeys.put("5",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount5)).getString("data"));
        pubkeys.put("6",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount6)).getString("data"));
        pubkeys.put("7",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount7)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 10个
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount3)).getString("data"));
        pubkeys.put("4",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount4)).getString("data"));
        pubkeys.put("5",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount5)).getString("data"));
        pubkeys.put("6",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount6)).getString("data"));
        pubkeys.put("7",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount7)).getString("data"));
        pubkeys.put("8",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        pubkeys.put("9",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        pubkeys.put("10",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 10个 签名11
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount3)).getString("data"));
        pubkeys.put("4",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount4)).getString("data"));
        pubkeys.put("5",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount5)).getString("data"));
        pubkeys.put("6",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount6)).getString("data"));
        pubkeys.put("7",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount7)).getString("data"));
        pubkeys.put("8",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        pubkeys.put("9",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        pubkeys.put("10",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));

        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,11,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("公钥个数[10]不能小于最小签名数[11]!",
                JSONObject.fromObject(createResp).getString("data"));

        //addresses 11个
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        pubkeys.put("3",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount3)).getString("data"));
        pubkeys.put("4",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount4)).getString("data"));
        pubkeys.put("5",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount5)).getString("data"));
        pubkeys.put("6",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount6)).getString("data"));
        pubkeys.put("7",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount7)).getString("data"));
        pubkeys.put("8",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        pubkeys.put("9",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        pubkeys.put("10",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));

        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//        assertEquals("parameter length error;  name(len):1-32  addresses(array len):0-10  comments(len):0-128;",
//                JSONObject.fromObject(createResp).getString("data"));



        log.info("test parameter name");
        //name为空
        name = "";
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount5)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount6)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度超过上限33位
        name = "_sT0" + Random(29);
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"))).getString("data"));

        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("parameter length error;  name(len):1-32  addresses(array len):0-10  comments(len):0-128;"));


        log.info("test parameter minSignatures");
        //minSignatures 为0
        name = Random(6);
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount3)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        minSignatures = 0;
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("minSignature[0] \\u003c 1"));

        //minSignatures超过地址个数
        pubkeys.clear();
        pubkeys.put("1",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount3)).getString("data"));
        pubkeys.put("2",JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount2)).getString("data"));
        minSignatures = 3;
        createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("公钥个数[2]不能小于最小签名数[3]!"));



    }


    @Test
    public void createDuplicateMultiAddr01() throws Exception {
        String testAccout1 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",null)
        ).getString("data");

        String testAccout2 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",null)
        ).getString("data");

        String pubkey1 = JSONObject.fromObject(tokenModule.tokenGetPubkey(testAccout1)).getString("data");
        String pubkey2 = JSONObject.fromObject(tokenModule.tokenGetPubkey(testAccout2)).getString("data");

        Map<String, Object> pubkeys = new HashMap<>();
        Map<String, Object> addresses = new HashMap<>();
        String name = "testdup";
        int minSignatures = 1;
        String groupID = "testdup";//0324移除groupID
        String comments = "create multi address for dup";
        ArrayList<String> listTag = new ArrayList<>();//0324移除tag
        //addresses 三个 包含一个不存在的地址或者未托管的地址
        addresses.clear();
        addresses.put("1", testAccout1);
        addresses.put("2", testAccout2);

        pubkeys.clear();
        pubkeys.put("1",pubkey1);
        pubkeys.put("2",pubkey2);

        String createResp = tokenModule.tokenCreateMultiAddr(addresses, name, minSignatures, groupID, comments, listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        String createResp2 = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys, name, minSignatures, groupID, comments, listTag);
        assertEquals("400",JSONObject.fromObject(createResp2).getString("state"));
        assertEquals(true, createResp2.contains("mul address has been exist"));
    }

    @Test
    public void createDuplicateMultiAddr02() throws Exception {
        String testAccout1 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",null)
                ).getString("data");

        String testAccout2 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",null)
        ).getString("data");

        String pubkey1 = JSONObject.fromObject(tokenModule.tokenGetPubkey(testAccout1)).getString("data");
        String pubkey2 = JSONObject.fromObject(tokenModule.tokenGetPubkey(testAccout2)).getString("data");

        Map<String, Object> pubkeys = new HashMap<>();
        Map<String, Object> addresses = new HashMap<>();
        String name = "testdup";
        int minSignatures = 1;
        String groupID = "testdup";//0324移除groupID
        String comments = "create multi address for dup";
        ArrayList<String> listTag = new ArrayList<>();//0324移除tag
        //addresses 三个 包含一个不存在的地址或者未托管的地址
        addresses.clear();
        addresses.put("1", testAccout1);
        addresses.put("2", testAccout2);

        pubkeys.clear();
        pubkeys.put("1",pubkey1);
        pubkeys.put("2",pubkey2);

        String createResp = tokenModule.tokenCreateMultiAddrByPubkeys(pubkeys, name, minSignatures, groupID, comments, listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        String createResp2 = tokenModule.tokenCreateMultiAddr(addresses, name, minSignatures, groupID, comments, listTag);
        assertEquals("400",JSONObject.fromObject(createResp2).getString("state"));
        assertEquals(true, createResp2.contains("mul address has been exist"));
    }

    @Test
    public void createMultiAccountInterfaceTest()throws Exception{

        Map<String, Object> addresses = new HashMap<>();
        String name = "test";
        int minSignatures = 1;
        String groupID = "testid";//0324移除groupID
        String comments = "create multi address";
        ArrayList<String> listTag = new ArrayList<>();//0324移除tag

        log.info("test parameter addresses");
        //addresses仅有一个且为空
        String createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("n[0](number of addresses) \\u003c minSignature[1]"));

        //addresses 一个地址 单签地址
        addresses.put("1",tokenAccount1);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals(true,createResp.contains("Need more than one address"));
        assertEquals(true,createResp.contains("创建多签地址的公钥个数或者地址个数不能等于"));


        //addresses 一个地址 多签地址
        addresses.put("1",tokenMultiAddr1);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals(true,createResp.contains("address["+tokenMultiAddr1+"] not exist!;error:sql: no rows in result set"));
        assertEquals(true,createResp.contains("创建多签地址的公钥个数或者地址个数不能等于1"));

        //addresses 一个地址 数据库中不存在的地址
        addresses.put("1",AddrNotInDB);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals(true,createResp.contains("address["+AddrNotInDB+"] not exist!;error:sql: no rows in result set"));
        assertEquals(true,createResp.contains("创建多签地址的公钥个数或者地址个数不能等于1"));


        //addresses 一个地址 单签地址的一部分
        addresses.put("1",tokenAccount1.substring(10));
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("创建多签地址的公钥个数或者地址个数不能等于1"));


        //addresses 两个 两个都为空
        addresses.put("1","");
        addresses.put("2","");
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address should not be empty!"));

        //addresses 两个 其中一个为空 一个为单签地址
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2","");
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address should not be empty!"));

        //addresses 两个 其中一个为空 一个为多签地址
        addresses.clear();
        addresses.put("1",tokenMultiAddr1);
        addresses.put("2","");
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address["+tokenMultiAddr1+"] not exist!;error:sql: no rows in result set"));

        //addresses 两个 其中一个单签 一个多签
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenMultiAddr1);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address["+tokenMultiAddr1+"] not exist!;error:sql: no rows in result set"));

        //addresses 两个 地址相同
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount1);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("the subaddresses cannot be the same"));

        //addresses 三个 其中一个为空
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3","");
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address should not be empty!"));


        //addresses 三个 其中两个为空
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2","");
        addresses.put("3","");
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address should not be empty!"));

        //addresses 三个 一个多签 两个单签
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenMultiAddr1);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address["+tokenMultiAddr1+"] not exist!;error:sql: no rows in result set"));

        //addresses 三个 两个相同
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount2);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("the subaddresses cannot be the same"));

        //addresses 三个 包含一个不存在的地址
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3",AddrNotInDB);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address["+AddrNotInDB+"] not exist!;error:sql: no rows in result set"));
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 4个
        addresses.clear();
        addresses.put("1",tokenAccount5);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount3);
        addresses.put("4",tokenAccount4);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 7个
        addresses.clear();
        addresses.put("1",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount3);
        addresses.put("4",tokenAccount4);
        addresses.put("5",tokenAccount5);
        addresses.put("6",tokenAccount6);
        addresses.put("7",tokenAccount7);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 10个
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount3);
        addresses.put("4",tokenAccount4);
        addresses.put("5",tokenAccount5);
        addresses.put("6",tokenAccount6);
        addresses.put("7",tokenAccount7);
        addresses.put("8",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("9",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("10",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //addresses 10个 签名11
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount3);
        addresses.put("4",tokenAccount4);
        addresses.put("5",tokenAccount5);
        addresses.put("6",tokenAccount6);
        addresses.put("7",tokenAccount7);
        addresses.put("8",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("9",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("10",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,11,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("n[10](number of addresses) < minSignature[11]",
                JSONObject.fromObject(createResp).getString("data"));

        //addresses 11个
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount3);
        addresses.put("4",tokenAccount4);
        addresses.put("5",tokenAccount5);
        addresses.put("6",tokenAccount6);
        addresses.put("7",tokenAccount7);
        addresses.put("8",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("9",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("10",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("11",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  name(len):1-32  addresses(array len):0-10  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        log.info("测试addresses中其中一个地址包含特殊字符");
        //addresses   其中一个为特殊字符 F 当前panic
        for(char testchar : specChar.toCharArray()){
            String tempAddr = String.valueOf(testchar);
            log.info("*********************test char " + tempAddr);
            addresses.clear();
            addresses.put("1",tokenAccount3);
            addresses.put("2",tempAddr);
            createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
            assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        }


        log.info("test parameter name");
        //name为空
        name = "";
        addresses.clear();
        addresses.put("1",tokenAccount7);
        addresses.put("2",tokenAccount3);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        //name为空格
        name = " ";
        addresses.clear();
        addresses.put("1",tokenAccount5);
        addresses.put("2",tokenAccount3);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度上限31位 包含空格
        name = "0a_T0" + Random(26);
        addresses.clear();
        addresses.put("1",tokenAccount4);
        addresses.put("2",tokenAccount6);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度上限32位 包含空格
        name = "_s T0" + Random(27);
        addresses.clear();
        addresses.put("1",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("2",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度超过上限33位
        name = "_sT0" + Random(29);
        addresses.clear();
        addresses.put("1",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        addresses.put("2",JSONObject.fromObject(
                tokenModule.tokenCreateAccount(Random(6),Random(6),Random(6),"",listTag)).getString("data"));
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("parameter length error;  name(len):1-32  addresses(array len):0-10  comments(len):0-128;"));


        //name长度超过上限 129
        name = "_sT0" + Random(125);
        addresses.clear();
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("parameter length error;  name(len):1-32  addresses(array len):0-10  comments(len):0-128;"));


        //测试包含特殊字符
        //支持特殊字符
        name = specChar.substring(0,31);
//        for(char testchar : specChar.toCharArray()){
//            name = String.valueOf(testchar);
//            log.info("*********************test char " + name);
            addresses.clear();
            addresses.put("1",tokenAccount4);
            addresses.put("2",tokenAccount7);
            createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
            assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//        }

        name = specChar.substring(31);
        addresses.clear();
        addresses.put("1",tokenAccount5);
        addresses.put("2",tokenAccount2);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        log.info("test parameter minSignatures");
        //minSignatures 为0
        name = Random(6);
        addresses.clear();
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        minSignatures = 0;
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("minSignature[0] \\u003c 1"));

        //minSignatures超过地址个数
        addresses.clear();
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        minSignatures = 3;
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("n[2](number of addresses) \\u003c minSignature[3]"));

        //0324移除groupID和tags字段

//        log.info("test parameter group id");
//
//        //groupID长度上限32位
//        groupID = "_123WdeSa" + UtilsClass.Random(55);
//        addresses.clear();
//        minSignatures = 1;
//        addresses.put("1",tokenAccount7);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//
//        //groupID长度上限32位含_
//        groupID = "__" + UtilsClass.Random(30);
//        addresses.clear();
//        minSignatures = 2;
//        addresses.put("1",tokenAccount7);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//
//        //groupID长度超过上限32位不含_
//        groupID = UtilsClass.Random(33);
//        addresses.clear();
//        minSignatures = 3;
//        addresses.put("1",tokenAccount7);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//
//        //groupID长度超过上限32位 128
//        groupID = UtilsClass.Random(128);
//        addresses.clear();
//        minSignatures = 3;
//        addresses.put("1",tokenAccount7);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//
//        //groupID长度超过上限32位含_ 33
//        groupID =  "__" + UtilsClass.Random(63);
//        addresses.clear();
//        minSignatures = 3;
//        addresses.put("1",tokenAccount7);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//
//        //检查所有特殊字符 不允许 当前仅支持特殊字符 _
//        //支持特殊字符
//        for(char testchar : specChar.toCharArray()){
//            groupID = String.valueOf(testchar);
//            log.info("*********************test char " + groupID);
//            addresses.clear();
//            minSignatures = 3;
//            addresses.put("1",tokenAccount7);
//            addresses.put("2",tokenAccount6);
//            addresses.put("3",tokenAccount5);
//            addresses.put("4",tokenAccount4);
//            createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//            assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//        }
//
//        log.info("test parameter tags");
//        listTag.clear();
//        listTag.add("_123aR");
//        listTag.add("0_123Fg");
//        listTag.add("a_frgKp");
//        listTag.add("T_a4cvPO");
//        addresses.clear();
//        minSignatures = 1;
//        addresses.put("1",tokenAccount3);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//
//
//        listTag.clear();
//        //测试多个128个字符
//        listTag.add("_1aR" + UtilsClass.Random(60));
//        listTag.add("0_Fg" + UtilsClass.Random(60));
//        listTag.add("a_fK" + UtilsClass.Random(60));
//        listTag.add("T_4c" + UtilsClass.Random(60));
//        addresses.clear();
//        minSignatures = 2;
//        addresses.put("1",tokenAccount3);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//
//        listTag.clear();
//        //测试多个128个字符 128个字
//        for(int i = 0;i<128;i++) {
//            listTag.add(UtilsClass.Random(1));
//        }
//        addresses.clear();
//        minSignatures = 3;
//        addresses.put("1",tokenAccount3);
//        addresses.put("2",tokenAccount6);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//
//
//        listTag.clear();
//        //测试多个128个字符 长度为128单个
//        listTag.add(UtilsClass.Random(128));
//        addresses.clear();
//        minSignatures = 1;
//        addresses.put("1",tokenAccount3);
//        addresses.put("2",tokenAccount2);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//
//        listTag.clear();
//        //测试多个128个字符 长度为127单个
//        listTag.add(UtilsClass.Random(127));
//        addresses.clear();
//        minSignatures = 2;
//        addresses.put("1",tokenAccount3);
//        addresses.put("2",tokenAccount2);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
//
//
//        listTag.clear();
//        //测试超过128个字符 129
//        listTag.add("_1aR" + UtilsClass.Random(61));
//        listTag.add("0_Fg" + UtilsClass.Random(60));
//        listTag.add("a_fK" + UtilsClass.Random(60));
//        listTag.add("T_4c" + UtilsClass.Random(60));
//        addresses.clear();
//        minSignatures = 3;
//        addresses.put("1",tokenAccount3);
//        addresses.put("2",tokenAccount2);
//        addresses.put("3",tokenAccount5);
//        addresses.put("4",tokenAccount4);
//        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//
//        //测试包含特殊字符
//        //检查所有特殊字符 不允许 当前仅支持特殊字符 _
//        //支持特殊字符
//        for(char testchar : specChar.toCharArray()){
//            String temp = String.valueOf(testchar);
//            listTag.clear();
//            listTag.add("_1aR" + temp + UtilsClass.Random(61));
//
//            groupID = String.valueOf(testchar);
//            addresses.clear();
//            minSignatures = 3;
//            addresses.put("1",tokenAccount3);
//            addresses.put("2",tokenAccount2);
//            addresses.put("3",tokenAccount5);
//            addresses.put("4",tokenAccount4);
//            createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
//            assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
//        }
//
//        listTag.clear();
//        listTag.add("test other params");


        log.info("test parameter comments");
        //测试128长度 不含中文 无异常
        comments = UtilsClass.Random(128);
        addresses.clear();
        minSignatures = 1;
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount4);
        addresses.put("4",tokenAccount7);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //测试包含特殊字符 无异常
        comments = specChar;
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),UtilsClass.Random(6),groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //comments长度上限128位含中文
        comments = "测试" + UtilsClass.Random(126);
        addresses.clear();
        minSignatures = 2;
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount4);
        addresses.put("4",tokenAccount7);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


        //comments长度127位
        comments = "中文" + UtilsClass.Random(125);
        addresses.clear();
        minSignatures = 3;
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount4);
        addresses.put("4",tokenAccount7);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //comments长度129
        comments = UtilsClass.Random(129);
        addresses.clear();
        minSignatures = 1;
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount4);
        addresses.put("4",tokenAccount6);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  name(len):1-32  addresses(array len):0-10  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //comments长度 300
        comments = UtilsClass.Random(300);
        addresses.clear();
        minSignatures = 1;
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount4);
        addresses.put("4",tokenAccount6);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals("parameter length error;  name(len):1-32  addresses(array len):0-10  comments(len):0-128;",
                JSONObject.fromObject(createResp).getString("data"));

        //comments使用xss字符串
        comments =  "<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>";
        addresses.clear();
        minSignatures = 1;
        addresses.put("1",tokenAccount3);
        addresses.put("2",tokenAccount2);
        addresses.put("3",tokenAccount4);
        addresses.put("4",tokenAccount6);
        createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

    }

    @Test
    public void getpublickeyInterfacetest(){

        //地址为空
        String resp = tokenModule.tokenGetPubkey("");
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals("parameter length error;  address:required;",
                JSONObject.fromObject(resp).getString("data"));

        //地址为空格
        resp = tokenModule.tokenGetPubkey(" ");
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals("invalid address",
                JSONObject.fromObject(resp).getString("data"));

        //地址为特殊字符
        for(char testChar : specChar.toCharArray()) {
            log.info("test char :" + testChar);
            resp = tokenModule.tokenGetPubkey(String.valueOf(testChar));
            assertEquals("400",JSONObject.fromObject(resp).getString("state"));
            assertEquals("invalid address",JSONObject.fromObject(resp).getString("data"));
        }

        //地址非法
        resp = tokenModule.tokenGetPubkey("1589");
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals("invalid address",JSONObject.fromObject(resp).getString("data"));

        //地址为非数据库中的地址
        resp = tokenModule.tokenGetPubkey(AddrNotInDB);
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals("publickey not found",JSONObject.fromObject(resp).getString("data"));


        //地址为多签地址
        resp = tokenModule.tokenGetPubkey(tokenMultiAddr1);
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals("该接口只支持单签地址!",JSONObject.fromObject(resp).getString("data"));

        //地址为单签地址的一半
        resp = tokenModule.tokenGetPubkey(tokenAccount1.substring(10));
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals("invalid address",JSONObject.fromObject(resp).getString("data"));
    }

    @Test
    public void issueInterfaceTest()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType11 = "ng11Token"+ UtilsClass.Random(3);
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = "5000.999999";
        String comments = "issue invalid test";

         tokenModule.tokenDelMintAddr(issueAddr);
         tokenModule.tokenDelCollAddr(collAddr);
         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //使用未注册的发行地址进行发行
         String issueResp = "";
         issueResp = tokenModule.tokenIssue(tokenAccount3,collAddr,stokenType11,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

         String stokenType12 = "ng12Token"+ UtilsClass.Random(3);
         //归集地址未注册
         issueResp = tokenModule.tokenIssue(issueAddr,tokenAccount3,stokenType12,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

         log.info("test issueAddr parameter...............");
         String stokenType13 = "ng13Token"+ UtilsClass.Random(3);
         //发行地址设置为空
         issueResp = tokenModule.tokenIssue("",collAddr,stokenType13,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains("parameter length error;  tokenType(len):1-64  comments(len):0-128;"));

         String stokenType14 = "ng14Token"+ UtilsClass.Random(3);
         //发行地址非法-原归集地址的一部分
         issueResp = tokenModule.tokenIssue(issueAddr.substring(10),collAddr,stokenType14,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType15 = "ng15Token"+ UtilsClass.Random(3);
         //发行地址非法-原归集地址*2
         issueResp = tokenModule.tokenIssue(issueAddr + issueAddr,collAddr,stokenType15,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType16 = "ng16Token"+ UtilsClass.Random(3);
         //发行地址非法-"432"
         issueResp = tokenModule.tokenIssue("432",collAddr,stokenType16,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType17 = "ng17Token"+ UtilsClass.Random(3);
         //发行地址非法-超长地址-"123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890"
         issueResp = tokenModule.tokenIssue("123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890",collAddr,stokenType17,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType18 = "ng18Token"+ UtilsClass.Random(3);
         //发行地址不在数据库中
         issueResp = tokenModule.tokenIssue(AddrNotInDB,collAddr,stokenType18,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains("addr doesn't exist!"));


         String stokenType19 = "ng19Token"+ UtilsClass.Random(3);
         //发行地址首尾存在空格
         issueResp = tokenModule.tokenIssue(" " + issueAddr + " ",collAddr,stokenType19,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains("invalid address"));

         //发行地址为特殊字符
         for(char testChar : specChar.toCharArray()){
             String temp = String.valueOf(testChar);
             String tempToken = "tempToken"+ UtilsClass.Random(3);
             issueResp = tokenModule.tokenIssue(temp,collAddr,tempToken,issAmount,comments);
             assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
             assertEquals(true, issueResp.contains("invalid address"));
         }

         log.info("test collAddr parameter...............");
         String stokenType21 = "ng21Token"+ UtilsClass.Random(3);
         //归集地址设置为空
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,"",stokenType21,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType22 = "ng22Token"+ UtilsClass.Random(3);
        //归集地址非法-原归集地址的一部分
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr.substring(10),stokenType22,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType23 = "ng23Token"+ UtilsClass.Random(3);
        //归集地址非法-原归集地址*2
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr + collAddr,stokenType23,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType24 = "ng24Token"+ UtilsClass.Random(3);
         //归集地址非法- "123"
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,"123",stokenType24,issAmount,comments);
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType25 = "ng25Token"+ UtilsClass.Random(3);
         //归集地址非法-超长- "123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890"
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,"123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890123432789012343278901234327890",stokenType25,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains(errInvalidAddr));

         String stokenType26 = "ng26Token"+ UtilsClass.Random(3);
         //归集地址非法-非数据库中的地址 未注册
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,AddrNotInDB,stokenType26,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));


        String stokenType27 = "ng27Token"+ UtilsClass.Random(3);
        //归集地址首尾存在空格
        issueResp = tokenModule.tokenIssue(issueAddr," " + collAddr + " ",stokenType19,issAmount,comments);
        assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
        assertEquals(true, issueResp.contains("invalid address"));

        //归集地址为特殊字符
        for(char testChar : specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            String tempToken = "tempToken"+ UtilsClass.Random(3);
            issueResp = tokenModule.tokenIssue(issueAddr,temp,tempToken,issAmount,comments);
            assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
            assertEquals(true, issueResp.contains("invalid address"));
        }

        log.info("test tokenType parameter...............");
         //tokenType为空
         String stokenType31 = "";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType31,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                 JSONObject.fromObject(issueResp).getString("data"));

         //tokenType为空格 F
         String stokenType32 = " ";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType32,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals("The string can only be 0-9,a-z,A-Z or _",
                 JSONObject.fromObject(issueResp).getString("data"));


         //tokenType为超长字符
         String stokenType33 = UtilsClass.Random(65);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType33,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                 JSONObject.fromObject(issueResp).getString("data"));

         //tokenType为64位字符 预期可以发行成功
         String stokenTypeOK34 = "__" + UtilsClass.Random(62);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK34,issAmount,comments);
         assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

         //tokenType为63位字符 预期可以发行成功
         String stokenTypeOK35 = UtilsClass.Random(63);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK35,issAmount,comments);
         assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

         //tokenType包含中文 预期可以发行成功  修改仅支持大小写字符数字和下划线
         //检查不能包含特殊字符
        String stokenTypeNG36 = "";
         for (char testchar : specChar.toCharArray()){
             stokenTypeNG36 = String.valueOf(testchar);
             issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeNG36,issAmount,comments);
             assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
             assertEquals("The string can only be 0-9,a-z,A-Z or _",
                     JSONObject.fromObject(issueResp).getString("data"));
         }


         log.info("test issAmount parameter...............");
         //数量为空 F
         //当前可以发行 数值为0
         String stokenType41 = "ng41Token" + UtilsClass.Random(6);
         issAmount = "";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType41,issAmount,comments);
//         assertEquals(true, issueResp.contains(errParamMsgIss));
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals("Amount must be greater than 0 and less than 18446744073709",
                 JSONObject.fromObject(issueResp).getString("data"));

         //数量为带负号的字串
         String stokenType42 = "ng42Token" + UtilsClass.Random(6);
         issAmount = "-100";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType42,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

         //数量为字母的字串
         String stokenType43 = "ng43Token" + UtilsClass.Random(6);
         issAmount = "ab";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType43,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为0
         //当前可以发行 数值为0
         String stokenType44 = "ng44Token" + UtilsClass.Random(6);
         issAmount = "0";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType44,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

         //数量字符超长125位
         //当前可以发行 数值为0
         String stokenType45 = "ng45Token" + UtilsClass.Random(6);
         issAmount = "10000000000000012343278901000000000000001234327890100000000000000123432789010000000000000012343278901000000000000001234327890";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType45,issAmount,comments);
         assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));


         //数量超过最大值
        String stokenType46 = "ng46Token" + UtilsClass.Random(6);
        issAmount = "18446744073709.1";
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType46,issAmount,comments);
        assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
        assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量超过最大值 小数点后6位
        String stokenType47 = "ng47Token" + UtilsClass.Random(6);
        issAmount = "18446744073708.999999" + UtilsClass.Random(44) ;
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType47,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        //数量为特殊字符
        for(char testChar : specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            String testType = "ng48Token" + UtilsClass.Random(6);
            issueResp = tokenModule.tokenIssue(issueAddr,collAddr,testType,temp,comments);
            assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
            assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));
        }


         log.info("test comments parameter...............");
         issAmount = "1235";
         //测试128长度 不含中文 无异常
         String stokenType50 = "51okToken" + UtilsClass.Random(6);
         comments = UtilsClass.Random(128);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType50,issAmount,comments);
         assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        //测试包含特殊字符 无异常
        String stokenType51 = "51okToken" + UtilsClass.Random(6);
        comments = specChar;
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType51,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));


        //comments长度上限128位含中文
        comments = "测试" + UtilsClass.Random(126);
        String stokenType52 = "52okToken" + UtilsClass.Random(6);
        String issueResp2 = tokenModule.tokenIssue(issueAddr,collAddr,stokenType52,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));

        //comments长度127位
        comments = "中文" + UtilsClass.Random(125);
        String stokenType53 = "53okToken" + UtilsClass.Random(6);
        String issueResp3 = tokenModule.tokenIssue(issueAddr,collAddr,stokenType53,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp3).getString("state"));

        //comments长度129
        comments = UtilsClass.Random(129);
        String stokenType54 = "54errToken" + UtilsClass.Random(6);
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType54,issAmount,comments);
        assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                JSONObject.fromObject(issueResp).getString("data"));

        //comments长度 300
        comments = UtilsClass.Random(300);
        String stokenType55 = "55errToken" + UtilsClass.Random(6);
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType55,issAmount,comments);
        assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                JSONObject.fromObject(issueResp).getString("data"));

        //comments使用xss字符串
        comments =  "<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>";
        String stokenType56 = "56okToken" + UtilsClass.Random(6);
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType56,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        //comments为空
         comments = "";
        String stokenType57 = "57okToken" + UtilsClass.Random(6);
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType57,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        //comments为空格
        comments = "";
        String stokenType58 = "58okToken" + UtilsClass.Random(6);
        issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType58,issAmount,comments);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));


         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        assertEquals("200", JSONObject.fromObject(issueResp2).getString("state"));
        assertEquals("200", JSONObject.fromObject(issueResp3).getString("state"));
//
         String queryBalance = tokenModule.tokenGetBalance(collAddr,"");
         assertEquals(false, queryBalance.contains(stokenType11));
         assertEquals(false, queryBalance.contains(stokenType12));
         assertEquals(false, queryBalance.contains(stokenType13));
         assertEquals(false, queryBalance.contains(stokenType14));
         assertEquals(false, queryBalance.contains(stokenType15));
         assertEquals(false, queryBalance.contains(stokenType16));
         assertEquals(false, queryBalance.contains(stokenType17));
         assertEquals(false, queryBalance.contains(stokenType18));

         assertEquals(false, queryBalance.contains(stokenType21));
         assertEquals(false, queryBalance.contains(stokenType22));
         assertEquals(false, queryBalance.contains(stokenType23));
         assertEquals(false, queryBalance.contains(stokenType24));
         assertEquals(false, queryBalance.contains(stokenType25));
         assertEquals(false, queryBalance.contains(stokenType26));
         assertEquals(false, queryBalance.contains(stokenType27));

//         assertEquals(false, queryBalance.contains(stokenType31));
         assertEquals(false, queryBalance.contains(stokenType32));
         assertEquals(false, queryBalance.contains(stokenType33));//当前长度限制与文档不符

         assertEquals(false, queryBalance.contains(stokenType41));
         assertEquals(false, queryBalance.contains(stokenType42));
         assertEquals(false, queryBalance.contains(stokenType43));
         assertEquals(false, queryBalance.contains(stokenType44));
         assertEquals(false, queryBalance.contains(stokenType45));


         assertEquals(false, queryBalance.contains(stokenType54));
         assertEquals(false, queryBalance.contains(stokenType55));


         assertEquals(true, queryBalance.contains(stokenTypeOK34));
         assertEquals(true, queryBalance.contains(stokenTypeOK35));
         assertEquals(false, queryBalance.contains(stokenTypeNG36));

         assertEquals(true, queryBalance.contains(stokenType50));
         assertEquals(true, queryBalance.contains(stokenType51));
         assertEquals(true, queryBalance.contains(stokenType52));
         assertEquals(true, queryBalance.contains(stokenType53));
         assertEquals(true, queryBalance.contains(stokenType56));
         assertEquals(true, queryBalance.contains(stokenType57));
         assertEquals(true, queryBalance.contains(stokenType58));
    }

    @Test
    public void transferInterfaceTest()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo_"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        double trfAmount1 = 100;

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("发行地址：" + issueAddr);
        log.info("归集地址：" + collAddr);
        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = "转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = "";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);


        log.info("test from parameter...............");
        //from地址为空
        transferResp = tokenModule.tokenTransfer("",to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址为空格
        transferResp = tokenModule.tokenTransfer(" ",to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址非法-432  F
        transferResp = tokenModule.tokenTransfer("432",to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));

        //from地址非法-from地址的一部分
        transferResp = tokenModule.tokenTransfer(from.substring(10),to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址非法-from*2
        transferResp = tokenModule.tokenTransfer(from + from,to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址不在地址数据库中
        transferResp = tokenModule.tokenTransfer(AddrNotInDB,to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("addr doesn't exist!"));


        log.info("测试特殊字符 for 转账 from 地址");
        //from地址使用特殊字符
        for(char testChar:specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            transferResp = tokenModule.tokenTransfer(temp,to,transferToken,transferAmount,comments);
            assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
            assertEquals(true,transferResp.contains("invalid address"));
        }


        log.info("test to list null");
        transferResp = commonFunc.tokenModule_TransferTokenList(from,null);
        assertEquals(true,transferResp.contains("to list should not be empty!"));

        log.info("test to parameter...............");
        //to地址为空
        transferResp = tokenModule.tokenTransfer(from,"",transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("parameter length error;  tokenType(len):1-64  comments(len):0-128;"));

        //to地址非法-432
        transferResp = tokenModule.tokenTransfer(from,"432",transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //to地址非法-from地址的一部分
        transferResp = tokenModule.tokenTransfer(from,to.substring(10),transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //to地址非法-to*2
        transferResp = tokenModule.tokenTransfer(from,to + to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //to地址不在地址数据库中  目前可以转账成功 可以转入 无法转出
        transferResp = tokenModule.tokenTransfer(from,AddrNotInDB,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        //to地址为自己 即自己转给自己
        transferResp = tokenModule.tokenTransfer(from,from,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("can't transfer it to yourself"));


        log.info("测试特殊字符 for 转账 to 地址");
        //from地址使用特殊字符
        for(char testChar:specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            transferResp = tokenModule.tokenTransfer(from,temp,transferToken,transferAmount,comments);
            assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
            assertEquals(true,transferResp.contains("invalid address"));
        }


        log.info("test tokenType parameter...............");
        //tokenType为空
        transferResp = tokenModule.tokenTransfer(from,to,"",transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals(true,transferResp.contains(errParamMsgTrf));

        //tokenType为空格
        transferResp = tokenModule.tokenTransfer(from,to," ",transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
//        The string can only be 0-9,a-z,A-Z or _
        assertEquals(true,transferResp.contains("The string can only be 0-9,a-z,A-Z or _"));

        for (char testchar : specChar.toCharArray()){
            String temp = String.valueOf(testchar);
            transferResp = tokenModule.tokenTransfer(from,to,temp,transferAmount,comments);
            assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
            assertEquals(true,transferResp.contains("The string can only be 0-9,a-z,A-Z or _"));
        }


        //tokenType不存在
        transferResp = tokenModule.tokenTransfer(from,to,transferToken + transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("Insufficient Balance"));

        //tokenType为已存在的tokenType的一部分，但是是不存在的tokenType
        transferResp = tokenModule.tokenTransfer(from,to,transferToken.substring(3),transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("Insufficient Balance"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("test amount parameter...............");
        //数量为空
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"",comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为带负号的字串
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"-100",comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为字母的字串
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"an",comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为中文的字串
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"中文",comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));


        //数量为0
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"0",comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量字符超长33位
        transferAmount = "18446744073708.999999" + Random(44);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("Insufficient Balance"));

        //数量字符超长125位
        transferAmount = "18446744073708.999999" + Random(104);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("Insufficient Balance"));


        log.info("test comments parameter...............");
        //测试128长度 不含中文 无异常
        comments = UtilsClass.Random(128);
        transferAmount ="10";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        //测试包含特殊字符 无异常
        comments = specChar;
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));


        //comments长度上限128位含中文
        comments = "测试" + UtilsClass.Random(126);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        //comments长度127位
        comments = "中文" + UtilsClass.Random(125);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        //comments长度129
        comments = UtilsClass.Random(129);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;"
                ,JSONObject.fromObject(transferResp).getString("data"));

        //comments长度 300
        comments = UtilsClass.Random(300);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                JSONObject.fromObject(transferResp).getString("data"));

        //comments使用xss字符串
        comments =  "<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        //comments为空
        comments = "";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

    }

    //该用例测试仅针对TDList的正确性做测试
    @Test
    public void multiTransferWithIDInterfaceTest()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenID_"+ UtilsClass.Random(8);
        double sAmount = 5000.9;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr2;
        double trfAmount1 = 100;

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("发行地址：" + issueAddr);
        log.info("归集地址：" + collAddr);
        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //向单签账户转账 不带IDList
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = "转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = "";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("4900.9",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr1,transferToken)
                ).getJSONObject("data").getString(transferToken));

        log.info("test IDList parameter for 3/3 Account...............");
        //list为空
        ArrayList<String> idList = new ArrayList<>();
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr3,transferToken,"100");

        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("4800.9",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr1,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //list中的内容为空
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals("传入id个数[1]应等于最小签名数[3]!",JSONObject.fromObject(transferResp).getString("data"));


        //list中的内容为空*2
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains("传入id个数[2]应等于最小签名数[3]!"));

        //list中的内容为空*3
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains("空字符串不能作为用户id传入"));


        //list中的内容正确的ID1,但个数不正确 仅一个
        idList.clear();
        idList.add(userId01);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains("传入id个数[1]应等于最小签名数[3]!"));


        //list中的内容正确的ID 但个数不足
        idList.add(userId02);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains("传入id个数[2]应等于最小签名数[3]!"));

        //list中的内容正确的ID 个数正确
        idList.add(userId03);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("4700.9",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr1,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //list中的ID存在不匹配的ID 个数正确
        idList.clear();
        idList.add(userId01);
        idList.add(userId02);
        idList.add(userId04);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "用户id[" + userId04 + "]对应的地址不是地址["+ tokenMultiAddr1+ "]的组成部分或非本地账户!"));

        //list中的ID重复 开发解释说目前api仅用作浦发用 支持1/2账户 不校验重复性
        idList.clear();
        idList.add(userId01);
        idList.add(userId01);
        idList.add(userId01);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
//        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
//                "用户id[" + userId04 + "]对应的地址不是地址["+ tokenMultiAddr1+ "]的组成部分或非本地账户!"));

        //list中的ID重复 开发解释说目前api仅用作浦发用 支持1/2账户 不校验重复性
        idList.clear();
        idList.add(userId01);
        idList.add(userId01);
        idList.add(userId02);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
//        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
//                "用户id[" + userId04 + "]对应的地址不是地址["+ tokenMultiAddr1+ "]的组成部分或非本地账户!"));

        //list中的ID存在空的id 个数正确
        idList.clear();
        idList.add(userId01);
        idList.add(userId02);
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "空字符串不能作为用户id传入"));

        //list中的ID存错误的ID 个数正确
        idList.clear();
        idList.add(userId01);
        idList.add(userId02);
        idList.add("123ddf");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "用户id[" + "123ddf" + "]对应的地址不是地址["+ tokenMultiAddr1+ "]的组成部分或非本地账户!"));

        //list中的ID存错误的ID*2 个数正确
        idList.clear();
        idList.add(userId01);
        idList.add("tkAc6AoQ5TB1");
        idList.add("123ddf");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "用户id[" + "tkAc6AoQ5TB1" + "]对应的地址不是地址["+ tokenMultiAddr1+ "]的组成部分或非本地账户!"));

        //list中的ID存错误的ID*3 个数正确
        idList.clear();
        idList.add("");
        idList.add("tkAc6AoQ5TB");
        idList.add("123ddf");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "空字符串不能作为用户id传入"));

        //list中的ID存错误的ID 个数超出
        idList.clear();
        idList.add(userId01);
        idList.add(userId02);
        idList.add(userId03);
        idList.add(userId04);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "传入id个数[4]应等于最小签名数[3]!"));


        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("4600.9",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr1,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //*********************************************************************************************************//
        log.info("test IDList parameter for 1/2 Account...............");
        from = tokenMultiAddr2;
        assertEquals("100",
                JSONObject.fromObject(tokenModule.tokenGetBalance(from,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //list为空
        list = utilsClass.tokenConstructToken(tokenMultiAddr4,transferToken,"5");
        idList.clear();
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("95",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr2,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //list中的内容为空
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "空字符串不能作为用户id传入"));


        //list中的内容为空*2
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "传入id个数[2]应等于最小签名数[1]!"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("95",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr2,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //list中的内容为空格
        idList.clear();
        idList.add(" ");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "用户id[" + " " + "]对应的地址不是地址["+ tokenMultiAddr2+ "]的组成部分或非本地账户!"));


        //list中的内容正确的ID1
        idList.clear();
        idList.add(userId01);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("90",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr2,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //list中的内容正确的ID2
        idList.clear();
        idList.add(userId02);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("85",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenMultiAddr2,transferToken)
                ).getJSONObject("data").getString(transferToken));


        //list中的内容错误的ID 个数正确
        idList.clear();
        idList.add(userId03);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "用户id[" + userId03 + "]对应的地址不是地址["+ tokenMultiAddr2+ "]的组成部分或非本地账户!"));

        //list中的ID存在不匹配的ID 个数正确
        idList.clear();
        idList.add(userId01);
        idList.add(userId02);
        idList.add(userId04);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "传入id个数[3]应等于最小签名数[1]!"));

        //list中的ID存在空的id 个数不对
        idList.clear();
        idList.add(userId01);
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "传入id个数[2]应等于最小签名数[1]!"));


        //list中的ID存错误的ID*1 个数正确
        idList.clear();
        idList.add("tkAc6AoQ5TB1");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "用户id[" + "tkAc6AoQ5TB1" + "]对应的地址不是地址["+ tokenMultiAddr2+ "]的组成部分或非本地账户!"));

    }

    //该用例测试仅针对TDList的正确性做测试
    @Test
    public void soloTransferWithIDInterfaceTest()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenID_"+ UtilsClass.Random(8);
        double sAmount = 5000.9;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        double trfAmount1 = 100;

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("发行地址：" + issueAddr);
        log.info("归集地址：" + collAddr);
        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //向单签账户转账 不带IDList
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = "转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = "";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("4900.9",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenAccount1,transferToken)
                ).getJSONObject("data").getString(transferToken));

        log.info("test IDList parameter for 3/solo Account...............");
        //list为空
        ArrayList<String> idList = new ArrayList<>();
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,transferToken,"100");

        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("4800.9",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenAccount1,transferToken)
                ).getJSONObject("data").getString(transferToken));

        //list中的内容为空
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains("空字符串不能作为用户id传入"));


        //list中的内容为空*2
        idList.add("");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains("单签地址不能传入多个id!"));


        //list中的内容错误的ID1
        idList.clear();
        idList.add(userId02);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "签名id[" + userId02 + "]和地址对应id[" + userId01+ "]不匹配!"));

        //list中的内容为数据库中不存在的错误的ID1
        idList.clear();
        idList.add("notexist");
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "签名id[" + "notexist" + "]和地址对应id[" + userId01+ "]不匹配!"));

        //list中的ID存在不匹配的ID 个数正确
        idList.clear();
        idList.add(userId01);
        idList.add(userId02);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(transferResp).getString("data").contains(
                "单签地址不能传入多个id!"));

        //list中的内容正确的ID1
        idList.clear();
        idList.add(userId01);
        transferResp = tokenModule.tokenTransferWithID(from,idList,comments,list);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("4700.9",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenAccount1,transferToken)
                ).getJSONObject("data").getString(transferToken));
    }

    @Test
    public void transferByUTXOInterfaceTest()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo_"+ UtilsClass.Random(8);
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = "5000.999999";

        //转账信息
        String from = collAddr;
        String to1 = tokenAccount2;
        String to2 = tokenAccount3;
        String trfAmount1 = "100";

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("发行地址：" + issueAddr);
        log.info("归集地址：" + collAddr);
        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        String issueResp = tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        String utxoHash = JSONObject.fromObject(issueResp).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //使用utxo转账
        String transferToken = issueToken;
        String transferAmount = trfAmount1;
        List<Map>listutxo = utilsClass.tokenConstructUTXO(utxoHash,0,transferAmount,to1);
        String transferResp = tokenModule.tokenTransfer(from,"utxo list transfer",listutxo);
        String transferHash = JSONObject.fromObject(transferResp).getString("data");
        JSONObject.fromObject(transferResp).getString("data");

        //新增utxo未花费引用列表功能
        log.info("test UTXO parameter...............");
        //utxo.hash为空
        listutxo.clear();
        // from = to1;
        listutxo = utilsClass.tokenConstructUTXO("",1,transferAmount,to1);
        transferResp = tokenModule.tokenTransfer(from,"utxo.hash为空",listutxo);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("get utxo 1 failed! sql: no rows in result set"));

        //utxo.index为null

//        listutxo.clear();
//        listutxo = utilsClass.tokenConstructUTXO(transferHash,null,transferAmount,to1);
//        transferResp = tokenModule.tokenTransfer(from,"",listutxo);
//        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals(true,transferResp.contains("get utxo 1 failed! sql: no rows in result set"));

        //utxo.amount为空
        listutxo.clear();
        listutxo = utilsClass.tokenConstructUTXO(transferHash,1,"",to1);
        transferResp = tokenModule.tokenTransfer(from,"",listutxo);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("amount字段不能为空!"));

        //utxo.address为空
        listutxo.clear();
        listutxo = utilsClass.tokenConstructUTXO(transferHash,1,transferAmount,"");
        transferResp = tokenModule.tokenTransfer(from,"",listutxo);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //utxo.hash为异常值
        listutxo.clear();
        listutxo = utilsClass.tokenConstructUTXO("123",1,transferAmount,to1);
        transferResp = tokenModule.tokenTransfer(from,"",listutxo);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("get utxo 1 failed! sql: no rows in result set"));

        //utxo.address为异常值
        listutxo.clear();
        listutxo = utilsClass.tokenConstructUTXO(transferHash,1,transferAmount,"123");
        transferResp = tokenModule.tokenTransfer(from,"",listutxo);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("invalid address"));

        //utxo.hash和index组不存在
        listutxo.clear();
        listutxo = utilsClass.tokenConstructUTXO(transferHash,3,transferAmount,to1);
        transferResp = tokenModule.tokenTransfer(from,"",listutxo);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("get utxo 1 failed! sql: no rows in result set"));

        //utxo第一笔list正确，第二笔list的address和from地址相同
        listutxo.clear();
        listutxo = utilsClass.tokenConstructUTXO(transferHash,1,transferAmount,to1);
        List<Map>listutxo2 = utilsClass.tokenConstructUTXO(transferHash,1,transferAmount,from,listutxo);
        transferResp = tokenModule.tokenTransfer(from,"",listutxo2);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("不能转账给自己!"));

        //utxo第一笔list正确，第二笔list异常
        listutxo.clear();
        listutxo2.clear();
        listutxo = utilsClass.tokenConstructUTXO(transferHash,1,transferAmount,to1);
        listutxo2 = utilsClass.tokenConstructUTXO("xx123abc",0,transferAmount,to1,listutxo);
        transferResp = tokenModule.tokenTransfer(from,"",listutxo2);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("get utxo 2 failed! sql: no rows in result set"));


        //utxo第一笔hash和第二笔hash引用的address与from地址不统一
        listutxo.clear();
        listutxo2.clear();
        listutxo = utilsClass.tokenConstructUTXO(transferHash,1,transferAmount,to2);
        listutxo2 = utilsClass.tokenConstructUTXO(transferHash,0,transferAmount,to2,listutxo);
        transferResp = tokenModule.tokenTransfer(from,"",listutxo2);
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("utxo 2 的持有者和 utxo 1的持有者不一致!"));


        //utxo为空值,引用to参数
        List<Map>listto = utilsClass.tokenConstructToken(to1,transferToken,transferAmount);
        listutxo.clear();
        transferResp = tokenModule.tokenTransfer(from,"utxo列表为空",listto,listutxo);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //utxo和to同时正确传参，优先utxo
        transferHash = JSONObject.fromObject(transferResp).getString("data");
        listto.clear();
        listto = utilsClass.tokenConstructToken(to2,transferToken,"222");
        listutxo = utilsClass.tokenConstructUTXO(transferHash,1,transferAmount,to2);
        transferResp = tokenModule.tokenTransfer(from,"",listto,listutxo);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(to2,JSONObject.fromObject(transferResp).getJSONArray("utxo").getJSONObject(0).getString("address"));
        assertEquals(transferAmount,JSONObject.fromObject(transferResp).getJSONArray("utxo").getJSONObject(0).getString("amount"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals("200",JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(transferAmount,JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void destoryInterfaceTest()throws Exception {
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount = "";

        //单签地址发行18446744073708
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issAmount = "5000.999999";
        issueToken = "tokenSo_" + UtilsClass.Random(8);


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("发行地址：" + issueAddr);
        log.info("归集地址：" + collAddr);
        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken, issAmount, comments);
        String issueToken2 = "tokenSo_" + UtilsClass.Random(8);
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken2, issAmount, comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));

        comments = "回收token";
        String destoryResp = "";
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();

        log.info("test list null");
        destoryResp = commonFunc.tokenModule_DestoryTokenByList2(null);
        assertEquals(true,destoryResp.contains("error"));

        log.info("test from parameter...............");
        //回收地址为空
        destoryResp = tokenModule.tokenDestoryByList("",issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("invalid address"));
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));


        //回收地址非法-432
        destoryResp = tokenModule.tokenDestoryByList("432",issueToken,"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址非法-collAddr地址的一部分
        destoryResp = tokenModule.tokenDestoryByList(collAddr.substring(10),issueToken,"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址非法-collAddr*2
        destoryResp = tokenModule.tokenDestoryByList(collAddr + collAddr,issueToken,"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址不在地址数据库中
        destoryResp = tokenModule.tokenDestoryByList(AddrNotInDB,issueToken,"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
//        assertEquals(true,destoryResp.contains("addr doesn't exist!"));
        assertEquals(true,destoryResp.contains("Insufficient Balance"));


        for (char testchar : specChar.toCharArray()){
            String tempAddr = String.valueOf(testchar);
            log.info("===================test spec char for destory " + tempAddr);
            destoryResp = tokenModule.tokenDestoryByList(tempAddr,issueToken,"0.00001",comments);
            assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
            assertEquals(true,destoryResp.contains("invalid address"));
        }

        //回收地址为无token账户 填写tokentype和amount
        String getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,"");
        destoryResp = tokenModule.tokenDestoryByList(tokenAccount3,issueToken,"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        //回收地址为无token账户 填写tokentype不填写amount
        destoryResp = tokenModule.tokenDestoryByList(tokenAccount3,issueToken,"",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));
        getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,"");

        //回收地址为无token账户 不填写tokentype填写amount
        destoryResp = tokenModule.tokenDestoryByList(tokenAccount3,"","100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("parameter length error;  tokenType(len):1-64  comments(len):0-128;"));

        //回收地址为无token账户 不填写tokentype和amount
        destoryResp = tokenModule.tokenDestoryByList(tokenAccount3,"","",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("parameter length error;  tokenType(len):1-64  comments(len):0-128;"));

        getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,"");

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("200", JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();



        log.info("test tokentype parameter...............");

        //tokenType不填写
        destoryResp = tokenModule.tokenDestoryByList(collAddr,"","100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("parameter length error;  tokenType(len):1-64  comments(len):0-128;"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();

        //tokenType 为空格
        destoryResp = tokenModule.tokenDestoryByList(collAddr," ","100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("The string can only be 0-9,a-z,A-Z or _"));

        //tokenType非该账户的tokenType
        destoryResp = tokenModule.tokenDestoryByList(collAddr,"tokenSo_0ak6f9fx","100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();

        //tokenType的一部分
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken.substring(3),"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();

        //tokenType为特殊字符
        for (char testchar : specChar.toCharArray()){
            String tempToken = String.valueOf(testchar);
            log.info("===================test spec char for destory " + tempToken);
            destoryResp = tokenModule.tokenDestoryByList(collAddr,tempToken,"0.00001",comments);
            assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
            assertEquals(true,destoryResp.contains("The string can only be 0-9,a-z,A-Z or _"));
        }


        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();


        log.info("test amount parameter...............");
        //数量字段变更为必输字段
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        //"Amount must be greater than 0 and less than 18446744073709"
        assertEquals(true,destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();

        //数量字段变更为必输字段 为空格
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken," ",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        //"Amount must be greater than 0 and less than 18446744073709"
        assertEquals(true,destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为带负号的字串
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"-100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为字母的字串
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"ojil",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为0
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"0",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));


        //数量字符33位
        String tempAmount = "18446744073708.999999" + Random(44);
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,tempAmount,comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
//        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量字符超长125位
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"10000000000000012343278901000000000000001234327890100000000000000123432789010000000000000012343278901000000000000001234327890",comments);
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));


        for (char testchar : specChar.toCharArray()){
            String tempAmout = String.valueOf(testchar);
            log.info("===================test spec char for destory " + tempAmout);
            destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,tempAmout,comments);
            assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
            assertEquals(true,destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));
        }

        log.info("test comments parameter...............");
        //测试128长度 不含中文 无异常
        comments = UtilsClass.Random(128);
        destoryResp = tokenModule.tokenDestoryByList(tokenAccount1,issueToken,"100",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //测试包含特殊字符 无异常
        comments = specChar;
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //comments长度上限128位含中文
        comments = "测试" + UtilsClass.Random(126);
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //comments长度127位
        comments = "中文" + UtilsClass.Random(125);
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //comments长度129
        comments = UtilsClass.Random(129);
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",JSONObject.fromObject(destoryResp).getString("data"));

        //comments长度 300
        comments = UtilsClass.Random(300);
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",JSONObject.fromObject(destoryResp).getString("data"));

        //comments使用xss字符串
        comments =  "<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>";
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //comments为空
        comments = "";
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

    }

    @Test
    public void destoryByUTXOInterfaceTest()throws Exception {

        String issAmount = "5000.999999";
        String issueToken = "tokenSo_" + UtilsClass.Random(8);
        String issueToken2 = "tokenSo_" + UtilsClass.Random(8);
        HashMap<String, Object> mapSendMsg = new HashMap<>();

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(tokenAccount1);
        tokenModule.tokenAddCollAddr(tokenAccount1);
        tokenModule.tokenAddMintAddr(tokenMultiAddr1);
        tokenModule.tokenAddCollAddr(tokenMultiAddr1);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        String issueResp = tokenModule.tokenIssue(tokenAccount1, tokenAccount1, issueToken, issAmount, comments);
        String issueResp2 = tokenModule.tokenIssue(tokenMultiAddr1, tokenMultiAddr1, issueToken2, issAmount, comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1, "");
        String queryBalance2 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(issueToken2));
        String issueHash = JSONObject.fromObject(issueResp).getString("data");
        String issueHash2 = JSONObject.fromObject(issueResp2).getString("data");

        //验证通过utxo回收接口正确
        String destoryResp = "";
        List<Map>listutxo = utilsClass.tokenConstrucDestroytUTXO(issueHash,0,"100");
        List<Map>listutxo2 = utilsClass.tokenConstrucDestroytUTXO(issueHash2,0,"100",listutxo);
        List<Map>list = utilsClass.tokenConstructToken(tokenAccount1,issueToken,"200") ;
        destoryResp = tokenModule.tokenDestoryByList(list,listutxo2,"通过utxo回收",mapSendMsg);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryBalance = tokenModule.tokenGetBalance(tokenAccount1, "");
        queryBalance2 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(issueToken2));
        String destoryHash = JSONObject.fromObject(destoryResp).getString("data");

        log.info("test UTXO parameter...............");
        //utxo.hash为空
        listutxo.clear();
        listutxo = utilsClass.tokenConstrucDestroytUTXO("",0,"100");
        destoryResp = tokenModule.tokenDestoryByList(null,listutxo,"utxo.hash为空",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("no rows in result set"));

        //utxo.index为null
//        listutxo.clear();
//        listutxo = utilsClass.tokenConstructUTXO(transferHash,null,transferAmount,to1);
//        transferResp = tokenModule.tokenTransfer(from,"",listutxo);
//        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals(true,transferResp.contains("get utxo 1 failed! sql: no rows in result set"));

        //utxo.amount为空
        listutxo.clear();
        listutxo = utilsClass.tokenConstrucDestroytUTXO(destoryHash,1,"");
        destoryResp = tokenModule.tokenDestoryByList(null,listutxo,"utxo.amount为空",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));


        //utxo.hash为异常值
        listutxo.clear();
        listutxo = utilsClass.tokenConstrucDestroytUTXO("123",0,"100");
        destoryResp = tokenModule.tokenDestoryByList(null,listutxo,"utxo.hash错误",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("no rows in result set"));


        //utxo.hash和index组不存在
        listutxo.clear();
        listutxo = utilsClass.tokenConstrucDestroytUTXO(destoryHash,10,"100");
        destoryResp = tokenModule.tokenDestoryByList(null,listutxo,"utxo.hash和index组不存在",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("no rows in result set"));


        //utxo第一笔list正确，第二笔list异常
        listutxo.clear();
        listutxo2.clear();
        listutxo = utilsClass.tokenConstrucDestroytUTXO(destoryHash,1,"100");
        listutxo2 = utilsClass.tokenConstrucDestroytUTXO("123",10,"100");
        destoryResp = tokenModule.tokenDestoryByList(null,listutxo2,"utxo第一笔list正确，第二笔list异常",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("no rows in result set"));


        //已经回收过
        listutxo.clear();
        listutxo2.clear();
        listutxo = utilsClass.tokenConstrucDestroytUTXO(destoryHash,0,"100");
        listutxo2 = utilsClass.tokenConstrucDestroytUTXO(destoryHash,2,"100");
        destoryResp = tokenModule.tokenDestoryByList(null,listutxo2,"已经回收过",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("已经被回收"));

        //回收金额大于余额
        listutxo.clear();
        listutxo2.clear();
        listutxo = utilsClass.tokenConstrucDestroytUTXO(destoryHash,1,"10000");
        listutxo2 = utilsClass.tokenConstrucDestroytUTXO(destoryHash,3,"10000");
        destoryResp = tokenModule.tokenDestoryByList(null,listutxo2,"回收金额大于余额",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("回收超额"));

        //utxo和list都为空值
        listutxo.clear();
        list.clear();
        destoryResp = tokenModule.tokenDestoryByList(list,listutxo,"utxo和list都为空值",mapSendMsg);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true,destoryResp.contains("utxo列表和回收list 不能同时为空!"));

        //utxo为空引用utxo
        listutxo.clear();
        list.clear();
        list = utilsClass.tokenConstructToken(tokenAccount1,issueToken,"200") ;
        destoryResp = tokenModule.tokenDestoryByList(list,listutxo,"utxo为空引用utxo",mapSendMsg);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1, "");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("4700.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void destoryByTokenTypeInterfaceTest()throws Exception {
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount = "";

        //单签地址发行token 18446744073708
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issAmount = "5000.999999";
        issueToken = "tokenSo_" + UtilsClass.Random(8);


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("发行地址：" + issueAddr);
        log.info("归集地址：" + collAddr);
        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken, issAmount, comments);
        String issueToken2 = "tokenSo_" + UtilsClass.Random(56);
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken2, issAmount, comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));

        comments = "回收token";
        String destoryResp = "";
        destoryResp = tokenModule.tokenDestoryByList(collAddr,issueToken,"100",comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();


        log.info("test tokentype parameter...............");

        //tokenType不填写
        destoryResp = tokenModule.tokenDestoryByTokenType("","100");
//        assertEquals(true,destoryResp.contains(errParamMsgDes2));
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                JSONObject.fromObject(destoryResp).getString("data"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));


        //tokenType不存在的tokenType
        destoryResp = tokenModule.tokenDestoryByTokenType("tokenSo_","100");
        assertEquals(true,destoryResp.contains("invalid tokenType"));
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));

        //tokenType的一部分
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken.substring(3),"100");
        assertEquals(true,destoryResp.contains("invalid tokenType"));
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();

        //tokenType为已存在的64位 且comments为空
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken2,"");
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //tokenType为65位
        destoryResp = tokenModule.tokenDestoryByTokenType(Random(65),"__");
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                JSONObject.fromObject(destoryResp).getString("data"));


        //tokenType为特殊字符
        for (char testchar : specChar.toCharArray()){
            String tempToken = String.valueOf(testchar);
            log.info("===================test spec char for destory by tokenType" + tempToken);
            destoryResp = tokenModule.tokenDestoryByTokenType(tempToken,"100");
            assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
            assertEquals("The string can only be 0-9,a-z,A-Z or _",JSONObject.fromObject(destoryResp).getString("data"));
        }


        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance();

        log.info("test comments parameter...............");
        //测试128长度 不含中文 无异常
        comments = UtilsClass.Random(128);
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken,comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //测试包含特殊字符 无异常
        comments = specChar;
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken,comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //comments长度上限128位含中文
        issueToken = "tokenSo_" + UtilsClass.Random(8);
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken, issAmount, comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        comments = "测试" + UtilsClass.Random(126);
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken,comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //comments长度127位
        comments = "中文" + UtilsClass.Random(125);
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken,comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //comments使用xss字符串
        comments =  "<SCRIPT SRC=http://***/XSS/xss.js></SCRIPT>";
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken,comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));

        //comments长度129
        comments = UtilsClass.Random(129);
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken,comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                JSONObject.fromObject(destoryResp).getString("data"));

        //comments长度 300
        comments = UtilsClass.Random(300);
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken,comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals("parameter length error;  tokenType(len):1-64  comments(len):0-128;",
                JSONObject.fromObject(destoryResp).getString("data"));

        //comments为空  tokentype为已回收过的类型
        comments = "";
        destoryResp = tokenModule.tokenDestoryByTokenType(issueToken2,comments);
        assertEquals("400",JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals("invalid tokenType",JSONObject.fromObject(destoryResp).getString("data").trim());
    }

    @Test
    public void queryBalanceInterfaceTest()throws Exception {

        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount = "";

        //单签地址发行token 5000.999999
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken = "tokenSo_" + UtilsClass.Random(8);
        issAmount = "5000.999999";


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("发行地址：" + issueAddr);
        log.info("归集地址：" + collAddr);
        String comments = "发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken, issAmount, comments);
        String issueToken2 = "123_" + UtilsClass.Random(60);
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken2, issAmount, comments);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));


        log.info("test address parameter...............");
        //查询地址为空
        queryBalance = tokenModule.tokenGetBalance("",issueToken);
        assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(true,queryBalance.contains("invalid address"));

        //查询地址非法-432
        queryBalance = tokenModule.tokenGetBalance("432",issueToken);
        assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(true,queryBalance.contains("invalid address"));

        //查询地址非法-collAddr地址的一部分
        queryBalance = tokenModule.tokenGetBalance(collAddr.substring(10),issueToken);
        assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(true,queryBalance.contains("invalid address"));

        //查询地址非法-collAddr*2
        queryBalance = tokenModule.tokenGetBalance(collAddr + collAddr,issueToken);
        assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(true,queryBalance.contains("invalid address"));

        //查询地址不在地址数据库中
        queryBalance = tokenModule.tokenGetBalance(AddrNotInDB,issueToken);
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //地址采用特殊字符 因接口本身会不支持一些特殊字符，这些接口过滤不测试
        for (char testchar : specChar2.toCharArray()){
            String temp = String.valueOf(testchar);
            log.info("===================test spec char for get balance " + temp);
            queryBalance = tokenModule.tokenGetBalance(temp,"");
            assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
            assertEquals(true,queryBalance.contains("invalid address"));
        }

        //查询地址为无token账户 填写tokentype和amount
        String getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,issueToken);
        assertEquals("200",JSONObject.fromObject(getOtherBalance).getString("state"));
        assertEquals(true,getOtherBalance.contains("\"data\":{}"));


        log.info("test tokenType parameter...............");
        //tokenType为空
        queryBalance = tokenModule.tokenGetBalance(collAddr,"");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(true,queryBalance.contains(issueToken));
        assertEquals(true,queryBalance.contains(issueToken2));


        //tokenType为64位存在的tokentype
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken2);
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));

        //tokenType为65位 当前无报错
        queryBalance = tokenModule.tokenGetBalance(collAddr,"_" + Random(64));
        assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("The length of tokentype is between 0 and 64",JSONObject.fromObject(queryBalance).getString("data"));

        //tokenType为#位 当前无报错
        queryBalance = tokenModule.tokenGetBalance(collAddr,"#");
        assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("invalid address",JSONObject.fromObject(queryBalance).getString("data"));



        //tokenType特殊字符测试 将# %另外测试
        for (char testchar : specChar2.replaceAll("#","").replaceAll("%","").replaceAll("&","").replaceAll(";","").toCharArray()){
            String temp = String.valueOf(testchar);
            log.info("===================test spec char for get balance " + temp);
            queryBalance = tokenModule.tokenGetBalance(collAddr,temp);
            assertEquals("400",JSONObject.fromObject(queryBalance).getString("state"));
            assertEquals("The string can only be 0-9,a-z,A-Z or _",
                    JSONObject.fromObject(queryBalance).getString("data"));
        }

        //tokenType为%位 当前无报错
        queryBalance = tokenModule.tokenGetBalance(collAddr,"%");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
//        assertEquals("The string can only be 0-9,a-z,A-Z or _",JSONObject.fromObject(queryBalance).getString("data"));

        //tokenType为&位 当前无报错
        queryBalance = tokenModule.tokenGetBalance(collAddr,"&");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
//        assertEquals("The string can only be 0-9,a-z,A-Z or _",JSONObject.fromObject(queryBalance).getString("data"));

        //tokenType为;位 当前无报错
        queryBalance = tokenModule.tokenGetBalance(collAddr,";");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));

        //tokenType不存在
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken+"11111111");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(false,queryBalance.contains(issueToken));

        //tokenType为已存在的tokenType的一部分，但是是不存在的tokenType
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken.substring(3));
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(false,queryBalance.contains(issueToken));

    }

    @Test
    public void tokenGetPrivateStoreInterfaceTest()throws Exception{
        String Data = "cxTest-private" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);

        String response= tokenModule.tokenCreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String StoreHashPwd = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response1= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount1);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(Data,JSONObject.fromObject(response1).getString("data"));

        //hash为空
        String response2= tokenModule.tokenGetPrivateStore("",tokenAccount2);
        assertEquals("400",JSONObject.fromObject(response2).getString("state"));
        assertThat(JSONObject.fromObject(response2).getString("data"),
                containsString("hash should not be empty!"));

        //hash非法123
        response2= tokenModule.tokenGetPrivateStore("123",tokenAccount2);
        assertEquals("400",JSONObject.fromObject(response2).getString("state"));
        assertThat(JSONObject.fromObject(response2).getString("data"),
                containsString("invalid parameter:hash"));

        //特殊字符
        log.info("test for special char for hash");
        for(char testChar : specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            response2= tokenModule.tokenGetPrivateStore(temp,tokenAccount2);
            assertEquals("400",JSONObject.fromObject(response2).getString("state"));
            assertThat(JSONObject.fromObject(response2).getString("data"), containsString("invalid parameter:hash"));
        }


        //hash非法hash的一部分
        response2= tokenModule.tokenGetPrivateStore(StoreHashPwd.substring(8),tokenAccount2);
        assertEquals("400",JSONObject.fromObject(response2).getString("state"));
        assertThat(JSONObject.fromObject(response2).getString("data"),
                containsString("failed to find transaction"));


        //地址使用无查询权限的用户进行查询
        String response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenAccount3);
        assertThat(JSONObject.fromObject(response3).getString("data"),
                containsString("you have no permission to get this transaction !"));

        //地址为有account1生成的多签地址
        response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,tokenMultiAddr1);
        assertThat(JSONObject.fromObject(response3).getString("data"),
                containsString("address can not be multi address!"));

        log.info("test for special char for address");
        //地址特殊字符 F
        for(char testChar : specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,temp);
            assertEquals("400",JSONObject.fromObject(response3).getString("state"));
            assertThat(JSONObject.fromObject(response3).getString("data"), containsString("invalid address"));
        }

        //地址为空
        response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,"");
        assertEquals("400",JSONObject.fromObject(response3).getString("state"));
        assertThat(JSONObject.fromObject(response3).getString("data"),
                containsString("Query privacy store need incoming address"));

        //地址非法159
        response3= tokenModule.tokenGetPrivateStore(StoreHashPwd,"159");
        assertEquals("400",JSONObject.fromObject(response3).getString("state"));
        assertThat(JSONObject.fromObject(response3).getString("data"),
                containsString(errInvalidAddr));

    }

    //回收账户查询无参数 不测试 http://host:port/v1/token/balance/destroyed

    @Test
    public void gettxdetailInterfaceTest(){
        //hash为空
        String resp ="";
        resp = tokenModule.tokenGetTxDetail("");
        assertEquals(true,resp.contains("error"));

        //hashdata 为空格
        resp = tokenModule.tokenGetTxDetail(" ");
        assertEquals(true,resp.contains("error"));


        for(char testChar : specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            resp = tokenModule.tokenGetTxDetail(temp);
            assertEquals("400",JSONObject.fromObject(resp).getString("state"));
            assertEquals(true,resp.contains("error"));
        }
    }

    @Test
    public void addMintAddrInterfaceTest()throws Exception {
        String testAddr = "";
        testAddr = tokenAccount1;

        //添加发行地址
        tokenModule.tokenAddMintAddr(testAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response = "";

        //address不填写
        response = tokenModule.tokenAddMintAddr("");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("parameter length error;  address:required;"));

        //address为空格
        response = tokenModule.tokenAddMintAddr(" ");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));

        //address数据库中不存在的address
        response = tokenModule.tokenAddMintAddr("31UYzLfbx6DnwbcZR6j2rG2FJabShCbSBx7ZLqDZTCYW7LfTeE");
        assertEquals(true,response.contains("\"state\":200"));

        //address的一部分
        response = tokenModule.tokenAddMintAddr(testAddr.substring(3));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));


        //已添加过的address
        response = tokenModule.tokenAddMintAddr(testAddr);
//        assertEquals(true,response.contains("\"state\":200"));
//        String hash1 = JSONObject.fromObject(response).getString("data");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = issue address["+testAddr+"] exist!"));

        //address前后加空格
        response = tokenModule.tokenAddMintAddr(" " + testAddr);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));

        response = tokenModule.tokenAddMintAddr(testAddr + " ");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));


        for(char testChar :specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            log.info("---------------test char " + temp);
            response = tokenModule.tokenAddMintAddr(temp);
            assertEquals("400",JSONObject.fromObject(response).getString("state"));
            assertEquals(true,response.contains(errInvalidAddr));
        }

        //addr为“123”
        response = tokenModule.tokenAddMintAddr("123");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
//        response = tokenModule.tokenGetTxDetail(hash1);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("failed to find transaction"));

    }

    @Test
    public void removeMintAddrInterfaceTest()throws Exception {
        String testAddr = "";
        testAddr = tokenAccount1;

        //添加发行地址
        tokenModule.tokenAddMintAddr(testAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response = "";

        //address不填写
        response = tokenModule.tokenDelMintAddr("");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("parameter length error;  address:required;"));

        //address为空格
        response = tokenModule.tokenDelMintAddr(" ");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("del issue address[ ] not exist!"));

        //address数据库中不存在的address
        response = tokenModule.tokenDelMintAddr("31UYzLfbx6DnwbcZR6j2rG2FJabShCbSBx7ZLqDZTCYW7LfTeE");
        assertEquals(true,response.contains("\"state\":200"));
//        assertEquals("500",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("rpc error: code = Unknown desc = del issue address[31UYzLfbx6DnwbcZR6j2rG2FJabShCbSBx7ZLqDZTCYW7LfTeE] not exist!"));

        //address的一部分
        response = tokenModule.tokenDelMintAddr(testAddr.substring(3));
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del issue address["+testAddr.substring(3)+"] not exist!"));


        //已添加过的address
        response = tokenModule.tokenDelMintAddr(testAddr);
        assertEquals(true,response.contains("\"state\":200"));
        String hash1 = JSONObject.fromObject(response).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //address前后加空格
        response = tokenModule.tokenDelMintAddr(" " + testAddr);
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del issue address[ "+testAddr+"] not exist!"));

        response = tokenModule.tokenDelMintAddr(testAddr + " ");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del issue address["+testAddr+" ] not exist!"));


        for(char testChar :specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            log.info("---------------test char " + temp);
            response = tokenModule.tokenDelMintAddr(temp);
            assertEquals("500",JSONObject.fromObject(response).getString("state"));
            assertEquals(true,response.contains(" not exist!"));
        }

        //addr为“123”
        response = tokenModule.tokenDelMintAddr("123");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del issue address[123] not exist!"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        response = tokenModule.tokenGetTxDetail(hash1);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        //addr为已删除过的地址
        response = tokenModule.tokenDelMintAddr(testAddr);
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del issue address["+testAddr+"] not exist!"));
//        hash1 = JSONObject.fromObject(response).getString("data");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
//        response = tokenModule.tokenGetTxDetail(hash1);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("failed to find transaction"));

    }

    @Test
    public void addCollAddrInterfaceTest()throws Exception {
        String testAddr = "";
        testAddr = tokenAccount1;

        //添加发行地址
        tokenModule.tokenAddCollAddr(testAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response = "";

        //address不填写
        response = tokenModule.tokenAddCollAddr("");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("parameter length error;  address:required;"));

        //address为空格
        response = tokenModule.tokenAddCollAddr(" ");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));

        //address数据库中不存在的address
        response = tokenModule.tokenAddCollAddr("31UYzLfbx6DnwbcZR6j2rG2FJabShCbSBx7ZLqDZTCYW7LfTeE");
        assertEquals(true,response.contains("\"state\":200"));

        //address的一部分
        response = tokenModule.tokenAddCollAddr(testAddr.substring(3));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));


        //已添加过的address
        response = tokenModule.tokenAddCollAddr(testAddr);
//        assertEquals(true,response.contains("\"state\":200"));
//        String hash1 = JSONObject.fromObject(response).getString("data");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Unknown desc = coll address["+testAddr+"] exist!"));

        //address前后加空格
        response = tokenModule.tokenAddCollAddr(" " + testAddr);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));

        response = tokenModule.tokenAddCollAddr(testAddr + " ");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));


        for(char testChar :specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            log.info("---------------test char " + temp);
            response = tokenModule.tokenAddCollAddr(temp);
            assertEquals("400",JSONObject.fromObject(response).getString("state"));
            assertEquals(true,response.contains(errInvalidAddr));
        }

        //addr为“123”
        response = tokenModule.tokenAddCollAddr("123");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains(errInvalidAddr));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
//        response = tokenModule.tokenGetTxDetail(hash1);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("failed to find transaction"));

    }

    @Test
    public void delCollAddrInterfaceTest()throws Exception {
        String testAddr = "";
        testAddr = tokenAccount1;

        //添加发行地址
        tokenModule.tokenAddCollAddr(testAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response = "";

        //address不填写
        response = tokenModule.tokenDelCollAddr("");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("parameter length error;  address:required;"));

        //address为空格
        response = tokenModule.tokenDelCollAddr(" ");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del coll address[ ] not exist!"));

        //address数据库中不存在的address
        response = tokenModule.tokenDelCollAddr("31UYzLfbx6DnwbcZR6j2rG2FJabShCbSBx7ZLqDZTCYW7LfTeE");
        assertEquals(true,response.contains("\"state\":200"));

        //address的一部分
        response = tokenModule.tokenDelCollAddr(testAddr.substring(3));
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del coll address["+testAddr.substring(3)+"] not exist!"));


        //已添加过的address
        response = tokenModule.tokenDelCollAddr(testAddr);
        assertEquals(true,response.contains("\"state\":200"));
        String hash1 = JSONObject.fromObject(response).getString("data");

        //address前后加空格
        response = tokenModule.tokenDelCollAddr(" " + testAddr);
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del coll address[ "+testAddr+"] not exist!"));

        response = tokenModule.tokenDelCollAddr(testAddr + " ");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del coll address["+testAddr+" ] not exist!"));


        for(char testChar :specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            log.info("---------------test char " + temp);
            response = tokenModule.tokenDelCollAddr(temp);
            assertEquals("500",JSONObject.fromObject(response).getString("state"));
            assertEquals(true,response.contains(" not exist!"));
        }

        //addr为“123”
        response = tokenModule.tokenDelCollAddr("123");
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del coll address[123] not exist!"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        response = tokenModule.tokenGetTxDetail(hash1);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        //已删除过的address
        response = tokenModule.tokenDelCollAddr(testAddr);
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("rpc error: code = Unknown desc = del coll address["+testAddr+"] not exist!"));
//        assertEquals(true,response.contains("\"state\":200"));
//        hash1 = JSONObject.fromObject(response).getString("data");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
//        response = tokenModule.tokenGetTxDetail(hash1);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("failed to find transaction"));

    }

    @Test
    public void freezeTokenInterfaceTest()throws Exception{
        //tokenType为空
        String resp ="";
        resp = tokenModule.tokenFreezeToken("");
        assertEquals("400", JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("invalid parameter"));

        //tokenType为空格
        resp = tokenModule.tokenFreezeToken(" ");
        assertEquals("400", JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("The string can only be 0-9,a-z,A-Z or _"));

        //tokentype65位长度
        resp = tokenModule.tokenFreezeToken("_" + Random(64));
        assertEquals("400", JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("invalid parameter"));


        for(char testChar :specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            log.info("---------------test char " + temp);
            resp = tokenModule.tokenFreezeToken(temp);
            assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        }

        //tokenType为不存在的tokenType
        resp = tokenModule.tokenFreezeToken("tokenSo-12Gh6uQVIZ");
        String hash5 = JSONObject.fromObject(resp).getString("data");
//        assertEquals(true,resp.contains("error"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("400",JSONObject.fromObject(tokenModule.tokenGetTxDetail(hash5)).getString("state"));
    }

    @Test
    public void recoverTokenInterfaceTest()throws Exception{
        //tokenType为空
        String resp ="";
        resp = tokenModule.tokenRecoverToken("");
        assertEquals("400", JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("check the validity of interface parameters, including length and character limits"));

        //tokentype为空格
        resp = tokenModule.tokenRecoverToken(" ");
        assertEquals("400", JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("The string can only be 0-9,a-z,A-Z or _"));

        //tokentype65位长度
        resp = tokenModule.tokenRecoverToken("_" + Random(64));
        assertEquals("400", JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("check the validity of interface parameters, including length and character limits"));

        for(char testChar :specChar.toCharArray()){
            String temp = String.valueOf(testChar);
            log.info("---------------test char " + temp);
            resp = tokenModule.tokenRecoverToken(temp);
            assertEquals("400",JSONObject.fromObject(resp).getString("state"));
            assertEquals("The string can only be 0-9,a-z,A-Z or _",JSONObject.fromObject(resp).getString("data"));
        }

        //tokenType为不存在的tokenType
        resp = tokenModule.tokenRecoverToken("tokenSo_12Gh6uQVIZ");
//        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertThat(resp,anyOf(containsString("\"state\":400"),containsString("\"state\":500")));

//        assertEquals(true,resp.contains("has not been frozen"));
        assertEquals(true,resp.contains("not exist"));

        //恢复一个未冻结的token
        String tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("100",
                JSONObject.fromObject(tokenModule.tokenGetBalance(tokenAccount1,"")).getJSONObject("data").getString(tokenType));
        resp = tokenModule.tokenRecoverToken(tokenType);
        String data = JSONObject.fromObject(resp).getString("data");
        String state = JSONObject.fromObject(resp).getString("state");
//        assertEquals("400", state);
//        assertEquals(true, data.contains("has not been frozen"));

    }
}
