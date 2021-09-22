package com.tjfintech.common.functionTest.JmlTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Jml;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassJml;
import org.junit.Test;

import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClassJml.*;
import static com.tjfintech.common.utils.UtilsClassJml.gettxId;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class JmlInterfaceTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Jml jml = testBuilder.getJml();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();

    @Test
    public void Test001_JmlAuthorizeAdd () {
        //新增授权用户,id为空
        Map subject = UtilsClassJml.subject("", "尹平");
        String response1 = jml.AuthorizeAdd(subjectType, bankId, endTime, fileHash, subject);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("invalid parameter"));
        assertThat(response1, containsString("error:Key: 'Individual.Subject.Id"));
    }

    @Test
    public void Test002_JmlAuthorizeAdd () {
        //新增授权用户,name为空
        Map subject = UtilsClassJml.subject("321027197508106015", "");
        String response1 = jml.AuthorizeAdd(subjectType, bankId, endTime, fileHash, subject);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("主体名称不可以为空"));
        assertThat(response1, containsString("null"));
    }

    @Test
    public void Test003_JmlAuthorizeAdd () {
        //新增授权用户,SubjectType为空
        Map subject = UtilsClassJml.subject("321027197508106015", "尹平");
        String response1 = jml.AuthorizeAdd("", bankId, endTime, fileHash, subject);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("invalid parameter"));
        assertThat(response1, containsString("error:Key: 'Individual.SubjectType' Error:Field validation for 'SubjectType'"));
        //新增授权用户,SubjectType为错误字段
        String response2 = jml.AuthorizeAdd("13213", bankId, endTime, fileHash, subject);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("invalid parameter"));
        assertThat(response2, containsString("error:Key: 'Individual.SubjectType' Error:Field validation for 'SubjectType'"));
        //新增授权用户,endTime为今日以前日期
        String response3 = jml.AuthorizeAdd(subjectType, bankId, "2021-08-30 23:59:59", fileHash, subject);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("日期[2021-08-30 23:59:59]不可以小于当前时间"));
        assertThat(response3, containsString("null"));
        //新增授权用户,endTime为空
        String response4 = jml.AuthorizeAdd(subjectType, bankId, "", fileHash, subject);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("invalid parameter"));
        assertThat(response4, containsString("Error:Field validation for 'EndTime' failed on the 'required' tag\""));
        //新增授权用户,fileHash为空
        String response5 = jml.AuthorizeAdd(subjectType, bankId, endTime, "", subject);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("invalid parameter"));
        assertThat(response5, containsString("error:Key: 'Individual.FileHash'"));
    }

    @Test
    public void Test004_CreditdataQuery () throws Exception {
        //新增授权用户
        Map subject = UtilsClassJml.subject("321027197508106015", "尹平");
        String response1 = jml.AuthorizeAdd(subjectType, bankId, endTime, fileHash, subject);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        String txId = gettxId(response1);
        System.out.println("txId = " + txId);
        //获取新增授权用户上链信息
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking = store.GetTxDetail(txId);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //查询数据接口,requestId为空
        String authId = getValueByKey(response1);
        System.out.println("authId = " + authId);
        String response2 = jml.CreditdataQuery("", authId, personId, personName, purpose);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("invalid parameter"));
        assertThat(response2, containsString("error:Key: 'QueryCreditLoan.RequestId'"));
        //查询数据接口,authId为空
        String response3 = jml.CreditdataQuery(requestId, "", personId, personName, purpose);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("invalid parameter"));
        assertThat(response3, containsString("Error:Field validation for 'AuthId' failed on the 'required' tag\""));
        //查询数据接口,authId为错误
        String response4 = jml.CreditdataQuery(requestId, "32132132121", personId, personName, purpose);
        assertThat(response4, containsString("403"));
        assertThat(response4, containsString("没有权限：该AuthId对应的用户还未授权"));
        assertThat(response4, containsString("null"));
        //查询数据接口,personId为错误
        String response5 = jml.CreditdataQuery(requestId, authId, "15165115231516", personName, purpose);
        assertThat(response5, containsString("403"));
        assertThat(response5, containsString("没有权限：IdHash校验出错"));
        assertThat(response5, containsString("null"));
        //查询数据接口,personId为空
        String response6 = jml.CreditdataQuery(requestId, authId, "", personName, purpose);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("自然人的身份证号码和姓名不可以为空"));
        assertThat(response6, containsString("null"));
        //查询数据接口,personName为空
        String response7 = jml.CreditdataQuery(requestId, authId, personId, "", purpose);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("自然人的身份证号码和姓名不可以为空"));
        assertThat(response7, containsString("null"));
        //查询数据接口,purpose为空
        String response8 = jml.CreditdataQuery(requestId, authId, personId, personName, "");
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("invalid parameter"));
        assertThat(response8, containsString("Error:Field validation for 'Purpose' failed on the 'required' tag\""));
        //查询数据接口,purpose为错误
        String response9 = jml.CreditdataQuery(requestId, authId, personId, personName, "115161");
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("查询用途只能为【放款审查、贷后审查、担保审查】"));
        assertThat(response9, containsString("null"));

    }

    @Test
    public void Test005_CreditloanFeedback () throws Exception {
        //新增授权用户
        Map subject = UtilsClassJml.subject("321027197508106015", "尹平");
        String response1 = jml.AuthorizeAdd(subjectType, bankId, endTime, fileHash, subject);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        String txId = gettxId(response1);
        System.out.println("txId = " + txId);
        //获取新增授权用户上链信息
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking = store.GetTxDetail(txId);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //查询数据接口,requestId为空
        String authId = getValueByKey(response1);
        System.out.println("authId = " + authId);
        String response2 = jml.CreditdataQuery(requestId, authId, personId, personName, purpose);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("\"itemName\":\"网格数据\""));
        assertThat(response2, containsString("\"itemName\":\"婚姻数据\""));
        assertThat(response2, containsString("\"itemName\":\"户籍信息\""));
        assertThat(response2, containsString("\"itemName\":\"用气信息\""));
        assertThat(response2, containsString("\"itemName\":\"用水信息\""));
        assertThat(response2, containsString("\"itemName\":\"车辆信息\""));
        assertThat(response2, containsString("\"itemName\":\"社保缴纳\""));
        assertThat(response2, containsString("\"itemName\":\"房屋权属\""));
        assertThat(response2, containsString("\"itemName\":\"严重失信\""));
    }


}
