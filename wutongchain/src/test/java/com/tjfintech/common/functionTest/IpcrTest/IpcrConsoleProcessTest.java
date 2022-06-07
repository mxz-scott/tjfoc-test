package com.tjfintech.common.functionTest.IpcrTest;

import com.tjfintech.common.CertTool;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Ipcr;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class IpcrConsoleProcessTest {

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

    @Test
    public void crInterfaceTest() throws Exception {

    }

    /**
     * 正常流程测试
     * 初始化用户注册（经纪商1\2\3,普通用户1\2\3）,经纪商1初始化系列和变更艺术品状态
     * 用户1账户艺术品查询为空，艺术品审核信息存储，
     * 艺术品A流转（经纪商1->普通用户1），账户艺术品查询、交易历史查询、获取艺术品url
     * 订单预支付、订单取消、订单查询
     * 生成艺术品证书
     */

    @Test
    public void crProcessTest() throws Exception {

    }

    /**
     * 正常流程测试
     * 经纪商1发行系列1/2，每个系列发行艺术品1/2到普通用户1/2/3
     * 艺术品交易历史查询，账户艺术品查询
     */

    @Test
    public void crIssueMultiTest() throws Exception {

    }

    /**
     * 异常流程测试-艺术品系列合约初始化max为10，typeno为3
     * 艺术品状态更新，第4种失败
     * 发行艺术品1，循环发给用户1用户2至库存（10）结束，转给用户3失败
     */
    @Test
    public void crArtworkIssueInvalidTest() throws Exception {

    }


    /**
     * 异常流程测试-账户注册接口
     * 相同的keyid重复注册接口
     */
    @Test
    public void crDuplicateAccountRegisterTest() throws Exception {



    }

    /**
     * 临时测试-账户注册接口
     * 批量生成账户
     */
    @Test
    public void crAccountRegisterTest() throws Exception {



    }


}