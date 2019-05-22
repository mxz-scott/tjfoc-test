package com.tjfintech.common.functionTest.utxoSingleSign;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfoc.base.MultiSignIssue;
import com.tjfoc.base.MultiSignTransferAccounts;
import com.tjfoc.base.SingleSignIssue;
import com.tjfoc.base.SingleSignTransferAccounts;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalSingleSignInvalidTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass = new UtilsClass();
    SingleSignIssue singleSign = new SingleSignIssue();
    MultiSignTransferAccounts multiTrans = new MultiSignTransferAccounts();
    SingleSignTransferAccounts singleTrans = new SingleSignTransferAccounts();

    /**
     * (本地签)单签发行 转账和回收，私钥不带密码，使用错误的私钥签名后，发送交易。
     */
    @Test
    public void TC1416_SoloProgress_LocalSign() throws Exception {
        String tokenType = "ST-" + UtilsClass.Random(7);
        String data = "" + "发行token: " + tokenType + " ，数量为：" + "10000";
        String issueResult = soloSign.issueTokenLocalSign(PUBKEY1, tokenType, "10000", data);
        String preSignData = JSONObject.fromObject(issueResult).getJSONObject("Data").toString();
        String signedData = singleSign.singleSignIssueMethod(preSignData, PRIKEY1);
        soloSign.sendSign(signedData);
        Thread.sleep(SLEEPTIME);  //设置等待时间等待交易上链

        //单签转账
        List<Map> transferList = soloSign.constructToken(ADDRESS7, tokenType, "100.25");
        String transferInfo2 = soloSign.TransferLocalSign(transferList, PUBKEY1, data);
        String preSignData2 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").toString();
        String signedData2 = singleTrans.singleSignTransferAccountsMethod(preSignData2, PRIKEY1);
        soloSign.sendSign(signedData2);
        Thread.sleep(SLEEPTIME);  //设置等待时间等待交易上链


        //单签回收
        String recycleResponse = soloSign.RecycleLocalSign(PUBKEY1, tokenType, "1000");
        String preSignData3 = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
        String signedData3 = singleTrans.singleSignTransferAccountsMethod(preSignData3, PRIKEY1);
        soloSign.sendSign(signedData3);
        Thread.sleep(SLEEPTIME);  //设置等待时间等待交易上链

    }
    /**
     * (本地签)单签发行 转账和回收，私钥带密码，使用错误的私钥签名后，发送交易。
     */
    @Test
    public void TC1416_SoloProgress_LocalSign_PWD() throws Exception {
        String tokenType = "ST-" + UtilsClass.Random(7);
        String data = "" + "发行token: " + tokenType + " ，数量为：" + "10000";
        String issueResult = soloSign.issueTokenLocalSign(PUBKEY6, tokenType, "10000", data);
        String preSignData = JSONObject.fromObject(issueResult).getJSONObject("Data").toString();
        String signedData = singleSign.singleSignIssueMethod(preSignData, PRIKEY6,PWD7);

        soloSign.sendSign(signedData);
        Thread.sleep(SLEEPTIME);  //设置等待时间等待交易上链


        //单签转账
        //构建token数组
        List<Map> transferList = soloSign.constructToken(MULITADD4, tokenType, "100.25"); //传入的参数：转入地址，Token类型，金额
        String transferInfo2 = soloSign.TransferLocalSign(transferList, PUBKEY6, data); //单签转账本地签名
        String preSignData2 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").toString();
        String signedData2 = singleTrans.singleSignTransferAccountsMethod(preSignData2, PRIKEY6,PWD6);
        soloSign.sendSign(signedData2);
        Thread.sleep(SLEEPTIME);  //设置等待时间等待交易上链

//
//        //单签回收
//        String recycleResponse = soloSign.RecycleLocalSign(PUBKEY1, tokenType, "1000");
//        String preSignData3 = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
//        String signedData3 = singleTrans.singleSignTransferAccountsMethod(preSignData3, PRIKEY1);
//        soloSign.sendSign(signedData3);
//        Thread.sleep(SLEEPTIME);  //设置等待时间等待交易上链
//        String queryInfo5 = multiSign.BalanceByAddr(ADDRESS1, tokenType);
//        System.out.println("查询余额"+queryInfo5);
    }
}
