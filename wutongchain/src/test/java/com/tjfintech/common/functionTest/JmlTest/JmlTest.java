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

import javax.naming.Name;
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

    /**
     * 循环查询数据
     * @throws Exception
     */
    @Test
    public void Test003_JmlAddQueryRecord() throws Exception {
        //新增授权用户

        String[] names = {"曾嵩", "张琪", "陈海波", "裴志河"};
        String[] ids = {"360202198807090038", "321088198905290028", "320925197409095416", "321027196902016013"};
        for (int a = 0; a < names.length; a++) {
            Map subject = UtilsClassJml.subject(ids[a], names[a]);
            String response = jml.AuthorizeAdd(subjectType, bankId, endTime, fileHash, subject);
            assertThat(response, containsString("200"));
            assertThat(response, containsString("success"));
            assertThat(response, containsString("data"));
            String txId = gettxId(response);
//            System.out.println("txId = " + txId);
            //获取新增授权用户上链信息
            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailType, SLEEPTIME);
            String checking = store.GetTxDetail(txId);
            assertThat(checking, containsString("200"));
            assertThat(checking, containsString("success"));
            //获取authid
            String authId = getValueByKey(response);
//            System.out.println("authId = " + authId);
            String response1 = jml.CreditdataQuery(requestId, authId, ids[a], names[a], purpose);
            assertThat(response1, containsString("200"));
            assertThat(response1, containsString("success"));
            assertThat(response1, containsString("data"));
            JSONObject jsonObject = JSONObject.fromObject(response1);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("identity").getJSONArray("items");
            JSONArray jsonArray1 = jsonObject.getJSONObject("data").getJSONObject("asset").getJSONArray("items");

            //第一次查询

            //初始化参数
            String itemValue = null;
            String cert_num_man = "";
            String cert_num_woman = "";
            String folk_man = "";
            String folk_woman = "";
            String name_man = "";
            String name_woman = "";
            String nation_man = "";
            String nation_woman = "";
            String op_date = "";
            String op_type = "";
            String birthday = "";
            String ethnicity = "";
            String gender = "";
            String id_no = "";
            String name = "";
            String present_address = "";
            String residence_address = "";
            String marriage = "";


            for (int b = 0; b < jsonArray.size(); b++) {
                JSONObject jsonObject1 = JSONObject.fromObject(jsonArray.get(b).toString());

                switch (jsonObject1.getString("itemName")) {
                    case "网格数据":
                        itemValue = jsonObject1.getString("itemValue");
                        log.info("aa===============================" + itemValue);
                        break;
                    case "婚姻登记":
                        marriage = jsonObject1.getJSONArray("itemValue").toString();
//                    cert_num_man = jsonObject3.getJSONObject("itemValue").getString("cert_num_man");
                        log.info("bb===============================" + marriage);
//                    cert_num_woman = jsonObject3.getJSONObject("itemValue").getString("cert_num_woman");
//                    folk_man = jsonObject3.getJSONObject("itemValue").getString("folk_man");
//                    folk_woman = jsonObject3.getJSONObject("itemValue").getString("folk_woman");
//                    name_man = jsonObject3.getJSONObject("itemValue").getString("name_man");
//                    name_woman = jsonObject3.getJSONObject("itemValue").getString("name_woman");
//                    nation_man = jsonObject3.getJSONObject("itemValue").getString("nation_man");
//                    nation_woman = jsonObject3.getJSONObject("itemValue").getString("nation_woman");
//                    op_date = jsonObject3.getJSONObject("itemValue").getString("op_date");
//                    op_type = jsonObject3.getJSONObject("itemValue").getString("op_type");
                        break;
                    case "户籍信息":
                        birthday = jsonObject1.getJSONObject("itemValue").getString("birthday");
                        log.info("cc===============================" + birthday);
                        ethnicity = jsonObject1.getJSONObject("itemValue").getString("ethnicity");
                        gender = jsonObject1.getJSONObject("itemValue").getString("gender").trim();
                        id_no = jsonObject1.getJSONObject("itemValue").getString("id_no");
                        name = jsonObject1.getJSONObject("itemValue").getString("name");
                        present_address = jsonObject1.getJSONObject("itemValue").getString("present_address");
                        residence_address = jsonObject1.getJSONObject("itemValue").getString("residence_address");
                        break;
                    default:
                }
            }

//            String itemValue1 = "";
//            String address = "";
//            String householder = "";
//            String id_no1 = "";
//            String usage = "";

//            JSONArray jsonArray1 = jsonObject.getJSONObject("data").getJSONObject("asset").getJSONArray("items");
//            for (int c = 0; c < jsonArray1.size(); c++) {
//                JSONObject jsonObject2 = JSONObject.fromObject(jsonArray1.get(c).toString());
//                log.info("ee===============================" + jsonObject2);
//                switch (jsonObject2.getString("itemName")) {
//                    case "用气信息":
//                        itemValue1 = jsonObject2.getString("itemValue");
//                        log.info("dd===============================" + itemValue1);
//                        break;
//                    case "用水信息":
//                        address = jsonObject2.getJSONObject("itemValue").getString("address");
//                        householder = jsonObject2.getJSONObject("itemValue").getString("householder");
//                        id_no1 = jsonObject2.getJSONObject("itemValue").getString("id_no");
//                        usage = jsonObject4.getString("usage");
//                        log.info("ee===============================" + usage);
//                        break;
//                    default:
//
//                }
//            }

                //查询数据接口，循环查询与第一次查询做对比
                for (int i = 0; i < 1; i++) {
                    String response2 = jml.CreditdataQuery(requestId, authId, ids[a], names[a], purpose);
                    assertThat(response2, containsString("200"));
                    assertThat(response2, containsString("success"));
                    assertThat(response2, containsString("data"));
                    JSONObject jsonObject1 = JSONObject.fromObject(response2);
                    JSONArray jsonArray2 = jsonObject1.getJSONObject("data").getJSONObject("identity").getJSONArray("items");
                    for (int j = 0; j < jsonArray2.size(); j++) {
                        JSONObject jsonObject2 = JSONObject.fromObject(jsonArray2.get(j).toString());

                        switch (jsonObject2.getString("itemName")) {
                            case "网格数据":
//                                    System.out.println(jsonObject2.getJSONObject("itemValue"));
                                assertEquals(jsonObject2.getString("itemValue"), itemValue);
                                break;
                            case "婚姻登记":
                                assertEquals(jsonObject2.getJSONArray("itemValue").toString(), marriage);
//                                    System.out.println(jsonObject2.getJSONObject("itemValue"));
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("cert_num_man"), cert_num_man);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("cert_num_woman"), cert_num_woman);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("folk_man"), folk_man);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("folk_woman"), folk_woman);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("name_man"), name_man);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("name_woman"), name_woman);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("nation_man"), nation_man);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("nation_woman"), nation_woman);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("op_date"), op_date);
//                                    assertEquals(jsonObject2.getJSONObject("itemValue").getString("op_type"), op_type);
                                break;
                            case "户籍信息":
                                System.out.println(jsonObject2.getJSONObject("itemValue"));
                                assertEquals(jsonObject2.getJSONObject("itemValue").getString("birthday"), birthday);
                                assertEquals(jsonObject2.getJSONObject("itemValue").getString("ethnicity"), ethnicity);
                                assertEquals(jsonObject2.getJSONObject("itemValue").getString("gender").trim(), gender);
                                assertEquals(jsonObject2.getJSONObject("itemValue").getString("id_no"), id_no);
                                assertEquals(jsonObject2.getJSONObject("itemValue").getString("name"), name);
                                assertEquals(jsonObject2.getJSONObject("itemValue").getString("present_address"), present_address);
                                assertEquals(jsonObject2.getJSONObject("itemValue").getString("residence_address"), residence_address);
                                break;
                            default:
                        }

//                            JSONArray jsonArray3 = jsonObject.getJSONObject("data").getJSONObject("asset").getJSONArray("items");
//                            for (int i1 = 0; i1 < jsonArray3.size(); i1++) {
//                                JSONObject jsonObject3 = JSONObject.fromObject(jsonArray3.get(i1).toString());
//                                switch (jsonObject3.getString("itemName")) {
//
//                                    case "用气信息":
//                                        assertEquals(jsonObject3.getString("itemValue"), itemValue1);
//                                        break;
//
//                                    case "用水信息":
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getString("address"), address);
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getString("householder"), householder);
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getString("id_no"), id_no1);
//                                        assertEquals(jsonObject2.getString("itemValue"), usage);
//                                        break;
//                                    case "车辆登记信息":
//                                        System.out.println("ddddddddddddddddddddddddddd");
//                                        break;
//
//                                    case "社保缴纳":
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getString("agency"), agency);
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getInt("continuous_total_month"), continuous_total_month);
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getString("latest_pay_date"), latest_pay_date);
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getString("latest_pay_month"), latest_pay_month);
//                                        assertEquals(jsonObject3.getJSONObject("itemValue").getString("name"), name1);
//                                        System.out.println(jsonObject3);
//                                        break;

//                                    case "房屋权属":
//                                        assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("area"), area);
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("holders"), 2);
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("is_sealup"), false);
//                                        assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("location"), location);
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("mortgage_list"), 0);
//                            assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("seq"), 1);
//                                        assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("status"), status);
//                                        assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("type"), type);
//                                        assertEquals(jsonObject3.getJSONArray("itemValue").getJSONObject(0).getString("usage"), usage);
//                                        System.out.println(jsonObject3);

//                                    default:
//                                }
//                                JSONArray jsonArray4 = jsonObject.getJSONObject("data").getJSONObject("asset").getJSONArray("items");
//                                for (int i2 = 0; i2 < jsonArray3.size(); i2++) {
//                                    JSONObject jsonObject4 = JSONObject.fromObject(jsonArray4.get(i2).toString());
//                                    switch (jsonObject4.getString("itemName")) {
//                                        case "严重失信":
//                                            System.out.println("jsonObject4");

//                                    }
//                                }

                            }
                    }
                }
//            }
//        }

            }
        }
    }
}