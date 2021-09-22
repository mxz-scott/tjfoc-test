package com.tjfintech.common.functionTest.JmlTest;

import com.alibaba.fastjson.JSON;

import com.tjfintech.common.GoJml;
import com.tjfintech.common.Interface.Jml;
import com.tjfintech.common.CommonFunc;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassJml.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Store.*;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassJml;
import com.tjfintech.common.utils.UtilsClassScf;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bson.assertions.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j

public class JmlTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Jml jml = testBuilder.getJml();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();

    @Test
    /**
     * 获取银行列表
     */
    public void Test001_JmlBankList() {
        String response1 = jml.BankList();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    @Test
    public void Test002_JmlAuthorizeAdd() throws Exception {
        String[] receiverPubkeys = {"LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRTk0bmxNQVVBY2ZiVE5RSXN4ZGdGQ0t2dlFpMlQNCmt5dHBNMTFIK1N0aFJnZzQ5ZXdpQ0ZnRjM5Wlg3ZG5rR0lBS2c5RFZuemprYTFPUlFoa2dvR0Z1SVE9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t"};
        System.out.println("receiverPubkeys" + receiverPubkeys[0]);
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

        //查询数据接口
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

        //上传授信额度
        Map results = UtilsClassJml.results("03833002155", "accepted", "信用良好，授信批准", "王春勇", "321022195706281518", 30000, 36, "7.8", "xxx助农贷");
        String response3 = jml.CreditloanFeedback(authId, receiverPubkeys, results);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));
        //获取上传授信额度上链信息
        String txId1 = gettxId(response3);
        System.out.println("txId = " + txId1);
        commonFunc.sdkCheckTxOrSleep(txId1, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking2 = store.GetTxDetail(txId1);
        assertThat(checking2, containsString("200"));
        assertThat(checking2, containsString("success"));

    }

    @Test
    public void Test003_JmlAuthorizeAdd() throws Exception {
        //新增授权用户
        Map subject = UtilsClassJml.subject("321088198905290028", "张琪");
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

        //查询数据接口
        String authId = getValueByKey(response1);
        System.out.println("authId = " + authId);

//        String[] names = new String["小杜","xiaozhou"];
//        String[] ids = new String[];



//        for (int k = 0; k < names.length ; k++)
//
//
//        String birthday = jsonObject2.getJSONObject("itemValue").getString("birthday");


        for (int i = 0; i < 3; i++) {
            String response2 = jml.CreditdataQuery(requestId, authId, personId, personName, purpose);
            assertThat(response2, containsString("200"));
            assertThat(response2, containsString("success"));
            assertThat(response2, containsString("data"));
            JSONObject jsonObject = JSONObject.fromObject(response2);
            JSONArray jsonArray2 = jsonObject.getJSONObject("data").getJSONObject("identity").getJSONArray("items");
            for (int j = 0; j < jsonArray2.size(); j++) {
                JSONObject jsonObject2 = JSONObject.fromObject(jsonArray2.get(j).toString());
                switch (jsonObject2.getString("itemName")) {
                    case "网格数据":
                        assertEquals(jsonObject2.getString("itemValue"), "null");
                        System.out.println(jsonObject2);
                        break;
                    case "户籍信息":
//                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("birthday"), birthday);
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("ethnicity"), "01");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getInt("gender"), 2);
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("id_no"), "321088198905290028");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("name"), "张琪");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("present_address"), "新村路1号1幢201室");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("residence_address"), "新村路1号1幢201室");
                        System.out.println(jsonObject2);
                        break;

                    case "婚姻信息":
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("cert_num_man"), "321088198911035090");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("cert_num_woman"), "321088198905290028");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("folk_man"), "01");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("folk_woman"), "01");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("name_man"), "赵方芳");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("name_woman"), "张琪");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("nation_man"), "156");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("name_woman"), "156");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("op_date"), "2014-05-29 00:00:00");
                        assertEquals(jsonObject2.getJSONObject("itemValue").getString("op_type"), "IA");

                        System.out.println(jsonObject2);
                        break;
                    default:
                }

                JSONArray jsonArray3 = jsonObject.getJSONObject("data").getJSONObject("asset").getJSONArray("items");
                for (int i1 = 0; i1 < jsonArray3.size(); i1++) {
                    JSONObject jsonObject3 = JSONObject.fromObject(jsonArray3.get(i1).toString());
                    switch (jsonObject3.getString("itemName")) {

                        case "用气信息":
                            System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbb");
                            break;

                        case "用水信息":
                            System.out.println("cccccccccccccccccccccccccc");
                            break;
                        case "车辆登记信息":
                            System.out.println("ddddddddddddddddddddddddddd");
                            break;

                        case "社保缴纳":
                            assertEquals(jsonObject3.getJSONObject("itemValue").getString("agency"), "江都人社局");
                            assertEquals(jsonObject3.getJSONObject("itemValue").getString("agency"), "江都人社局");
                            assertEquals(jsonObject3.getJSONObject("itemValue").getInt("continuous_total_month"), 38);
                            assertEquals(jsonObject3.getJSONObject("itemValue").getString("latest_pay_date"), "2021-07-20 00:00:00");
                            assertEquals(jsonObject3.getJSONObject("itemValue").getString("latest_pay_month"), "202107");
                            assertEquals(jsonObject3.getJSONObject("itemValue").getString("name"), "张琪");
                            System.out.println(jsonObject3);
                            break;

                        case "房屋权属":
                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("area"), "宗地面积943.05㎡/房屋建筑面积46.79㎡");
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("holders"), 2);
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("is_sealup"), false);
                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("location"), "扬州市江都区龙川北路西侧广源世纪花园130幢B610室");
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("mortgage_list"), 0);
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("seq"), 1);
                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("status"), "现势");
                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("type"), "土地、房屋");
                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("usage"), "城镇住宅用地/高档公寓");
                            System.out.println(jsonObject3);

                        default:
                    }
                    JSONArray jsonArray4 = jsonObject.getJSONObject("data").getJSONObject("asset").getJSONArray("items");
                    for (int i2 = 0; i2 < jsonArray3.size(); i2++) {
                        JSONObject jsonObject4 = JSONObject.fromObject(jsonArray4.get(i2).toString());
                        switch (jsonObject4.getString("itemName")) {
                            case "严重失信":
                                System.out.println("jsonObject4");

                        }
                    }

                }
            }
        }
     }
}
