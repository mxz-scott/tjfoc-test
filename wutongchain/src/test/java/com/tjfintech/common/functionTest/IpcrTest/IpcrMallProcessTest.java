package com.tjfintech.common.functionTest.IpcrTest;

import com.tjfintech.common.CertTool;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Ipcr;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassIpcr.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class IpcrMallProcessTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Ipcr ipcr = testBuilder.getIpcr();
    CertTool certTool = new CertTool();
    Store store = testBuilder.getStore();
    Kms kms = testBuilder.getKms();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    IpcrCommonFunc ipcrCommonFunc = new IpcrCommonFunc();


    @BeforeClass
    public static void init() throws Exception {

        IpcrCommonFunc ipcrCommonFunc = new IpcrCommonFunc();

    }

    /**
     * 正常流程测试
     * Get获取数据接口：经纪商、艺术品、轮播图、系列、文件、用户、邀请码、积分、文章
     */

    @Test
    public void ipcrMallGetProcessTest()  {

        String response = ipcr.ipcrMallBroker(BROKERNO);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(BROKERNO));

        response = ipcr.ipcrMallArtworkStock(ARTWORKNO);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallArtworkSeries(SERIESID);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(ARTWORKNO));

        response = ipcr.ipcrMallArtwork(ARTWORKNO);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(ARTWORKNO));

        response = ipcr.ipcrMallArtworkSeriesUser(SERIESID);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(ARTWORKNO));

        response = ipcr.ipcrMallCarouselUrls();
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallSeriesUp(SERIESID);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(SERIESID));

        response = ipcr.ipcrMallSeriesUser(1,10);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(SERIESID));

        response = ipcr.ipcrMallSeriesPage(1,10,"",0);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallFileGetToken();
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallFile(Integer.parseInt(SERIESID));
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallUserInfo();
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallSpreadInvite();
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        SPREADCODE = JSONObject.fromObject(response).getString("data");

        response = ipcr.ipcrMallSpreadValidate(SPREADCODE);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallSpreadInviteNum();
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallPointBalance();
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallPointRecords(POINTCODE,1,10);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        response = ipcr.ipcrMallUserPenInfo(PENID);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(String.valueOf(PENID)));

        response = ipcr.ipcrMallUserPenList();
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true, response.contains(String.valueOf(PENID)));

    }

    /**
     * 正常流程测试
     * Post接口：订单创建、订单取消、订单支付、订单状态、订单详情、用户的订单列表
     */

    @Test
    public void ipcrMallOrderTest() throws Exception {

        //创建订单
        String response = ipcr.ipcrMallOrder(ARTWORKNO,1);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        String orderNo = JSONObject.fromObject(response).getString("data");

        //订单状态，创建中
        response = ipcr.ipcrMallOrderStatus(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals("null", JSONObject.fromObject(response).getString("data"));

        //订单状态，创建成功
        sleepAndSaveInfo(3000);
        response = ipcr.ipcrMallOrderStatus(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals("1", JSONObject.fromObject(response).getString("data"));

        //订单详情
        response = ipcr.ipcrMallOrder(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(orderNo, JSONObject.fromObject(response).getJSONObject("data").getString("no"));

        //用户的订单列表
        response = ipcr.ipcrMallOrderUser(1,10,0);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true,response.contains(orderNo));

        //订单取消
        response = ipcr.ipcrMallOrderCancel(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

        //用户的订单列表
        response = ipcr.ipcrMallOrderUser(1,10,0);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(false,response.contains(orderNo));

        response = ipcr.ipcrMallOrderUser(1,10,3);
        assertEquals("200", JSONObject.fromObject(response).getString("status"));
        assertEquals(true,response.contains(orderNo));

    }

    /**
     * 正常流程测试
     * Post接口：实名认证
     */

    @Test
    public void ipcrMallUserTest() throws Exception {

        String response = ipcr.ipcrMallRealName("李璐","342221199207226028");
        assertEquals("200", JSONObject.fromObject(response).getString("status"));

    }


}