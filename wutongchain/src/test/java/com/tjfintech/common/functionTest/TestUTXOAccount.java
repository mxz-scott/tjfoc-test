package com.tjfintech.common.functionTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestUTXOAccount {

    public   final static int   SLEEPTIME=20*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    ContractTest ct =new ContractTest();
    UtilsClass utilsClass=new UtilsClass();

    long time1,time2,time3,time4,time5,time6,time7,time8,time9,time10,
            time11,time12,time13,time14,time15,time16,time17,time18,time19,time20,
            time21,time22,time23=0;
    /*
            1.时间1无任何发行（清空数据库）
            2.时间2地址1发行token1*1000
            3.时间3地址1发行token2*88
            4.时间4地址2发行token3*200
            5.时间5转账token1、2（转给多人：A-token1*200 ，B-token2*10）
            6.时间6转账token3（转给多人：A-token3*20，C-token3*25）
            7.时间7冻结token1（冻结量应为1000）
            8.时间8冻结token3（冻结量应为200）
            9.时间9转账token1（转给多人：A-token1*100，B-token1*100）
            10.时间10转账token2（余额充足转给多人：B-token2*15，C-token2*18）
            11.时间11转账token2（余额不足转给多人：B-token2*15，C-token2*50）
            12.时间12转账token3（（转给多人：A-token3*10，C-token3*5）
            13.时间13回收地址1token1（一部分：100）
            14.时间14回收账户Atoken2（一部分:5）
            15.时间15回收地址2token3（一部分:10）
            16.时间16解除冻结token1（单次解除量应为1000）
            17.时间17解除冻结token2（单次解除量应为0）
            18.时间18解除冻结token3（单次解除量应为200）
            19.时间19转账token1、2（转给多人：A-token1*200 ，B-token2*10）
            20.时间20转账token3（转给多人：A-token3*10，C-token3*15）
            21.时间21回收token1（一部分：100）
            22.时间22回收地址1token2（剩余所有：35）
            23.时间23回收token3（一部分：10）
            */


    @Before
    public void beforeConfig() throws Exception {
        if(certPath!=""&& bReg==false) {
            setAndRestartPeerList("cd "+ PTPATH + "peer/conf/basePkTm500ms.toml "+ PTPATH +"peer/conf/base.toml");
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(SLEEPTIME);
            bReg=true;
        }
    }



    @Test
    public void checkUTXOTx()throws Exception{
        //UTXO类交易 Type 1 SubType 10 11 12
        //单签发行
        String tokenTypeS = "TxTypeSOLOTC-"+ UtilsClass.Random(6);
        String siData="单签"+ADDRESS1+"发行token "+tokenTypeS;
        String amount="10000";
        log.info(siData);
        String response3= soloSign.issueToken(PRIKEY1,tokenTypeS,amount,siData,ADDRESS1);


        //多签发行
        String tokenTypeM = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount1 = "50000";
        String  mulData= "多签"+MULITADD3 + "发行给自己" + tokenTypeM + " token，数量为：" + amount1;
        log.info(mulData);
        String response51 = multiSign.issueToken(IMPPUTIONADD,tokenTypeM, amount1, mulData);
        assertThat(response51, containsString("200"));
        String Tx1 = JSONObject.fromObject(response51).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response52 = multiSign.Sign(Tx1, PRIKEY5);

        String tokenTypeM2 = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount12 = "50000";
        String  mulData2= "多签"+MULITADD3 + "发行给"+MULITADD7 +" "+ tokenTypeM2 + " token，数量为：" + amount12;
        log.info(mulData2);
        String response53 = multiSign.issueToken(IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12,mulData2);
        String Tx13 = JSONObject.fromObject(response53).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response54 = multiSign.Sign(Tx13, PRIKEY5);
        Thread.sleep(8000);

        //单签转账
        assertEquals(JSONObject.fromObject(soloSign.Balance(PRIKEY1,tokenTypeS)).getJSONObject("Data").getString("Total"),amount);
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenTypeS,"3000");
        String amountTransfer="3000";
        String tranferdata="transfer to "+ADDRESS3+" with amount "+amountTransfer;
        String response4= soloSign.Transfer(list,PRIKEY1,"transfer to "+ADDRESS3+" with amount 3000");

        //多签转账
        assertEquals(JSONObject.fromObject( multiSign.Balance(IMPPUTIONADD,PRIKEY4,tokenTypeM)).getJSONObject("Data").getString("Total"),amount1);
        String tranferAmount="3000";
        String transferData = IMPPUTIONADD+" 向 " + ADDRESS5 + " 转账 " + tranferAmount + " " +tokenTypeM;
        List<Map> listInit = utilsClass.constructToken(ADDRESS5, tokenTypeM, "3000");
        log.info(transferData);
        String response6 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, listInit);


        Thread.sleep(5000);
        String resp3=multiSign.freezeToken(PRIKEY1,tokenTypeM);

        //单签回收
        String recySoloAmount="600";
        log.info("单签回收");
        String RecycleSoloInfo = multiSign.Recycle( PRIKEY1, tokenTypeS, recySoloAmount);


        //多签回收
        log.info("多签回收");
        String recyMultiAmount="70";
        String RecycleMultiInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenTypeM, recyMultiAmount);

        Thread.sleep(6000);
        //解除冻结token
        String resp=multiSign.recoverFrozenToken(PRIKEY1,tokenTypeM);
    }


}
