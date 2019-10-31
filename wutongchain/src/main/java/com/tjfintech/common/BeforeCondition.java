package com.tjfintech.common;


import com.sun.deploy.util.StringUtils;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static com.tjfintech.common.utils.UtilsClass.subLedger;



@Slf4j
//该类实现权限赋值999；更新证书类型，根据新的证书类型生成多签地址；注册发行地址和归集地址
public class BeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();


    //赋值权限999 区分是否主子链
    public void setPermission999()throws Exception{

        String toolPath="cd "+ ToolPATH +";";
        String exeCmd="./" + ToolTPName + " permission ";

        SDKID=getSDKID();
        String ledger ="";
        ledger=(subLedger!="")?" -z "+subLedger:"";
        String preCmd=toolPath+exeCmd+"-p "+PEER1RPCPort+" -s SDK "+ledger+" -d "+SDKID+" -m ";
        String getPerm=toolPath+"./" + ToolTPName + " getpermission -p "+PEER1RPCPort+ledger;

        Shell shellPeer1=new Shell(PEER1IP,USERNAME,PASSWD);
        int iFlag=0;
        shellPeer1.execute(getPerm);
        ArrayList<String> stdout1 = shellPeer1.getStandardOutput();
        String resp = StringUtils.join(stdout1,"\n");
        log.info(resp);
        assertEquals(resp.contains("失败"), false);
        log.info("SDK ID:"+SDKID);
        if(resp.contains(SDKID)) {
            for (String str1 : stdout1){
                if(str1.contains(SDKID))
                    break;
                iFlag++;
                //assertEquals(str.contains("FuncUpdatePeerPermission success:  true"),true);
            }
            log.info("当前SDK 权限："+stdout1.get(iFlag+1));
            if(stdout1.get(iFlag+1).contains(fullPerm)==false) {
                shellPeer1.execute(preCmd + "999");
                ArrayList<String> stdout = shellPeer1.getStandardOutput();
                resp = StringUtils.join(stdout,"\n");
                log.info(resp);
                assertEquals(resp.contains(fullPerm),true);
                Thread.sleep(SLEEPTIME);
            }
        }
        else {
            shellPeer1.execute(preCmd + "999");
            ArrayList<String> stdout2 = shellPeer1.getStandardOutput();
            resp = StringUtils.join(stdout2,"\n");
            log.info(resp);
            assertEquals(resp.contains(fullPerm),true);
            Thread.sleep(SLEEPTIME);
        }
    }

    public void clearDataSetPerm999() throws Exception{
        delDataBase();//清空sdk当前使用数据库数据
        //设置节点 清空db数据 并重启
        setAndRestartPeerList(clearPeerDB,resetPeerBase);
        //重启SDK
        setAndRestartSDK();

        setPermission999();
    }


    /**
     * 赋予权限和读取公私钥对。
     *
     */
    @Test
    public  void givePermission() throws Exception{
        setPermission999();
        updatePubPriKey();//从文件中根据配置certPath读取指定类型的公私钥对
     }


    /**
     * 添加发行地址和归集地址
     *
     */
    public  void collAddressTest() throws Exception{
        createAddresses(); //生成多签地址
        String response= multiSign.collAddress(PRIKEY1,IMPPUTIONADD);
        String response2= multiSign.collAddress(PRIKEY1,MULITADD3);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4=multiSign.collAddress(PRIKEY2,ADDRESS2);
        String response5= multiSign.collAddress(PRIKEY1,MULITADD4);
        String response6= multiSign.collAddress(PRIKEY1,MULITADD5);
        String response7= multiSign.collAddress(PRIKEY1,MULITADD7);
        String response8= multiSign.collAddress(PRIKEY1,MULITADD1);
        assertThat(response4,containsString("200"));
        assertThat(response,containsString("200"));
        assertThat(response2,containsString("200"));
        assertThat(response3,containsString("200"));
        assertThat(response5,containsString("200"));
        assertThat(response6,containsString("200"));
        assertThat(response7,containsString("200"));
        assertThat(response8,containsString("200"));
        //2.0.1版本需要添加发行地址后才可以发行
        String response11= multiSign.addissueaddress(PRIKEY1,IMPPUTIONADD);
        String response12= multiSign.addissueaddress(PRIKEY1,MULITADD3);
        String response13= multiSign.addissueaddress(PRIKEY1,ADDRESS1);
        String response14=multiSign.addissueaddress(PRIKEY2,ADDRESS2);
        String response15= multiSign.addissueaddress(PRIKEY1,MULITADD4);
        String response16= multiSign.addissueaddress(PRIKEY1,MULITADD5);
        String response17= multiSign.addissueaddress(PRIKEY1,MULITADD7);
        String response18= multiSign.addissueaddress(PRIKEY1,MULITADD1);
        assertThat(response14,containsString("200"));
        assertThat(response11,containsString("200"));
        assertThat(response12,containsString("200"));
        assertThat(response13,containsString("200"));
        assertThat(response15,containsString("200"));
        assertThat(response16,containsString("200"));
        assertThat(response17,containsString("200"));
        assertThat(response18,containsString("200"));

    }


    /**
     * 测试用例T284的前提条件。发行对应token
     */

    public  void  T284_BeforeCondition(String tokenType){
        //String tokenType = "cx-chenxu"+certPath;
        String amount="1000";
        //String amount = "1000";
        log.info(IMPPUTIONADD+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "IMPPUTIONADD" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY4);
    }

    /**
     * 创建公私钥对
     *
     */
    public void updatePubPriKey()throws Exception{
        if(certPath == "") {
            log.info("using default setting key pairs in UtilsClass");
            return;
        }
        PRIKEY1 = getKeyPairsFromFile(certPath+"/keys1/key.pem");
        PRIKEY2 = getKeyPairsFromFile(certPath+"/keys2/key.pem");
        PRIKEY3 = getKeyPairsFromFile(certPath+"/keys3/key.pem");
        PRIKEY4 = getKeyPairsFromFile(certPath+"/keys4/key.pem");
        PRIKEY5 = getKeyPairsFromFile(certPath+"/keys5/key.pem");
        PRIKEY6 = getKeyPairsFromFile(certPath+"/keys6/key.pem");
        PRIKEY7 = getKeyPairsFromFile(certPath+"/keys7/key.pem");

        PUBKEY1 = getKeyPairsFromFile(certPath+"/keys1/pubkey.pem");
        PUBKEY2 = getKeyPairsFromFile(certPath+"/keys2/pubkey.pem");
        PUBKEY3 = getKeyPairsFromFile(certPath+"/keys3/pubkey.pem");
        PUBKEY4 = getKeyPairsFromFile(certPath+"/keys4/pubkey.pem");
        PUBKEY5 = getKeyPairsFromFile(certPath+"/keys5/pubkey.pem");
        PUBKEY6 = getKeyPairsFromFile(certPath+"/keys6/pubkey.pem");
        PUBKEY7 = getKeyPairsFromFile(certPath+"/keys7/pubkey.pem");

     }

     /**
     * 创建多签地址 保存在数据库中
     * 当数据库被清，库中没多签地址信息时候调用。
     */
    public void createAddresses()throws Exception{

        ADDRESS1 =JSONObject.fromObject(soloSign.genAddress(PUBKEY1)).getJSONObject("Data").getString("Address");
        ADDRESS2 =JSONObject.fromObject(soloSign.genAddress(PUBKEY2)).getJSONObject("Data").getString("Address");
        ADDRESS3 =JSONObject.fromObject(soloSign.genAddress(PUBKEY3)).getJSONObject("Data").getString("Address");
        ADDRESS4 =JSONObject.fromObject(soloSign.genAddress(PUBKEY4)).getJSONObject("Data").getString("Address");
        ADDRESS5 =JSONObject.fromObject(soloSign.genAddress(PUBKEY5)).getJSONObject("Data").getString("Address");
        ADDRESS6 =JSONObject.fromObject(soloSign.genAddress(PUBKEY6)).getJSONObject("Data").getString("Address");
        ADDRESS7 =JSONObject.fromObject(soloSign.genAddress(PUBKEY7)).getJSONObject("Data").getString("Address");

        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        MULITADD1=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//123
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY6);
        MULITADD2=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//126
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        map.put("3", PUBKEY7);
        MULITADD3=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//167
        M = 1;
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        MULITADD4=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//12
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY3);
        MULITADD5=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//13
        map = new HashMap<>();
        map.put("1", PUBKEY3);
        map.put("2", PUBKEY4);
        MULITADD6=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//34
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        MULITADD7=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//16
        map = new HashMap<>();
        map.put("1", PUBKEY4);
        map.put("2", PUBKEY5);
        IMPPUTIONADD=JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("Data").getString("Address");//45
    }


}
