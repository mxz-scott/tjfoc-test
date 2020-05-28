package com.tjfintech.common.functionTest.utxoMultiSign;

import com.google.gson.JsonObject;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
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
import static org.junit.Assert.assertThat;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiSignInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    Contract contract = testBuilder.getContract();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    //多签地址
    private static String multiaddr1;
    private static String multiaddr2;
    private static String Tokentype;
    private static String MaxValue;
    private static String MaxValuePlus1;
    @Before
    public void createmultiaddr() throws Exception{

        if(MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME);
        }

        Map<String, Object> map =  new HashMap<>();
        //传入3个公钥
        map.put("PUBKEY1",utilsClass.PUBKEY1);
        map.put("PUBKEY2",utilsClass.PUBKEY2);
        map.put("PUBKEY6",utilsClass.PUBKEY6);
        multiaddr2 = multiSign.genMultiAddress(1, map);
        log.info("多签地址二"+ JSONObject.fromObject(multiaddr2).getJSONObject("data").getString("address"));
        Map<String, Object> map2 =  new HashMap<>();
        //传入3个公钥
        map2.put("PUBKEY1",utilsClass.PUBKEY1);
        map2.put("PUBKEY2",utilsClass.PUBKEY2);
        map2.put("PUBKEY6",utilsClass.PUBKEY3);
        multiaddr1 = multiSign.genMultiAddress(1, map2);
        log.info("多签地址一"+ JSONObject.fromObject(multiaddr1).getJSONObject("data").getString("address"));

        //随机生成一个Tokentype
        Tokentype = UtilsClass.Random(4);
        log.info("生成随机数tokentype"+Tokentype);


        if (UtilsClass.PRECISION == 10) {
            MaxValue = "1844674407";
            MaxValuePlus1 = "1844674407.1";
        }else {
            MaxValue = "18446744073709";
            MaxValuePlus1 = "18446744073709.1";
        }

    }



    /**
     * 创建多签地址
     */
    @Test
    public void TC1354_genmultiaddress() throws Exception{
        String multiAddress;
        Map<String, Object> map =  new HashMap<>();
        //传入3个公钥
        map.put("PUBKEY1",utilsClass.PUBKEY1);
        map.put("PUBKEY2",utilsClass.PUBKEY2);
        map.put("PUBKEY6",utilsClass.PUBKEY6);
        multiAddress = multiSign.genMultiAddress(0, map);
        assertThat(multiAddress, containsString("M can't be 0"));
        map.put("PUBKEY1","");
        multiAddress = multiSign.genMultiAddress(1, map);
        assertThat(multiAddress, containsString("pubkey can'not be empty"));//公钥字段存在空值
        map.put("PUBKEY1","YWJjeHg=");
        multiAddress = multiSign.genMultiAddress(1, map);
        assertThat(multiAddress, containsString("unsupport pem file"));//公钥字段存在一些没有意义的base64编码格式的字符
        map.put("PUBKEY1","abc");
        multiAddress = multiSign.genMultiAddress(1, map);
        assertThat(multiAddress, containsString("PublicKey must be base64 encoding"));//公钥字存在非base64编码的值
        Map<String, Object> map2 =  new HashMap<>();
        //传入3个公钥
        map2.put("PUBKEY1",utilsClass.PUBKEY1);
        multiAddress = multiSign.genMultiAddress(1, map2);
        assertThat(multiAddress, containsString("need more pubkey"));//Args字段只传入一个公钥

    }

    @Test
    public void testCreateMuliAddr10orMore() throws Exception{
        Map<String, Object> map =  new HashMap<>();
        certPath = "RSA";
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.updatePubPriKey();
        map.clear();
        map.put("1",PUBKEY1);
        map.put("2",PUBKEY2);
        map.put("3",PUBKEY3);
        map.put("4",PUBKEY4);
        map.put("5",PUBKEY5);
        map.put("6",PUBKEY6);
        map.put("7",PUBKEY7);
        certPath = "SM2";
        beforeCondition.updatePubPriKey();
        map.put("8",PUBKEY1);
        map.put("9",PUBKEY2);
        map.put("10",PUBKEY3);
        String respCreateMultiAddr = multiSign.genMultiAddress(10,map);
        assertEquals("200",JSONObject.fromObject(respCreateMultiAddr).getString("state"));

        map.put("11",PUBKEY4);
        respCreateMultiAddr = multiSign.genMultiAddress(10,map);
        assertEquals("400",JSONObject.fromObject(respCreateMultiAddr).getString("state"));
        assertEquals("pubkey numbers can't be greater than 10",JSONObject.fromObject(respCreateMultiAddr).getString("message"));

    }

    
    /**
     * 多签的Token发行申请(带私钥和密码)
     */
    @Test
    public void TC1335_issuetoken(){



        String issuetoken;
        issuetoken = multiSign.issueTokenCarryPri("11","test211","10000000",utilsClass.PRIKEY6,utilsClass.PWD6,"zz");//	MultiAddr字段传入无效字段
        assertThat(issuetoken, containsString("Invalid multiple address(from addr)"));
        issuetoken = multiSign.issueTokenCarryPri("4QqVU8DvcZNWQ7mAiuq8SFzZkhKW27PRAgo91Q716KqvK3jYxo",Tokentype,"10000000",UtilsClass.PRIKEY1,"zz");
        assertThat(issuetoken, containsString("Invalid multiple address(from addr)"));//	MultiAddr字段传入错误的地址
        issuetoken = multiSign.issueTokenCarryPri(multiaddr2,"","10000000",utilsClass.PRIKEY6,utilsClass.PWD6,"zz");
        assertThat(issuetoken, containsString("TokenType shouldn't be empty"));//Tokentype字段为空
        //	Amount字段为空值
        issuetoken = multiSign.issueTokenCarryPri(multiaddr2,Tokentype,"",utilsClass.PRIKEY6,utilsClass.PWD6,"zz");
        assertThat(issuetoken, containsString("Amount must be greater than 0 and less than "+MaxValue));
        //	Amount字段为负值
        issuetoken = multiSign.issueTokenCarryPri(multiaddr2,Tokentype,"-1",utilsClass.PRIKEY6,utilsClass.PWD6,"zz");
        assertThat(issuetoken, containsString("Amount must be greater than 0 and less than "+MaxValue));
        //	Amount字段超出最大值
        issuetoken = multiSign.issueTokenCarryPri(multiaddr2,Tokentype,MaxValuePlus1,utilsClass.PRIKEY6,utilsClass.PWD6,"zz");
        assertThat(issuetoken, containsString("Amount must be greater than 0 and less than "+MaxValue));

        //	PriKey传入非base64字符
        issuetoken = multiSign.issueTokenCarryPri(multiaddr2,Tokentype,"90000","abc",utilsClass.PWD6,"zz");
        assertThat(issuetoken, containsString("Private key must be base64 string"));

        issuetoken = multiSign.issueTokenCarryPri(multiaddr2,Tokentype,"90000","YWJjeHg=",utilsClass.PWD6,"zz");
        assertThat(issuetoken, containsString("Invalid multiple address(from addr)"));

        issuetoken = multiSign.issueTokenCarryPri(multiaddr2,Tokentype,"90000",utilsClass.PRIKEY6,"abc22","zz");
        assertThat(issuetoken, containsString("Invalid multiple address(from addr)"));//Pwd字段和Prikey字段不匹配

    }

    /**
     * 签名多签交易
     */
    @Test
    public void TC1356_sign(){
        String sign;
        String tx = "7b22696e223a5b7b2274784964223a226447706d62324e6659323970626d4a686332553d222c227369676e7475726573223a5b6e756c6c2c6e756c6c2c224d455143494164562b2b6e717762413538484e7765313436747131775175524a346346766868574b5267535742342f694169426b6d646c3139433163646d52304850515974727134736268347a34792b2b37547477376f496546795a6d413d3d225d2c227075626b657973223a5b223339354b69655262796b6972685657564e6f4f42414e566f626a4347585857754d39755934543663626b6931744c706d726b59333869493334466246716949375251396a59536a66317a322b2b51485452775a3736513d3d222c226e4b5a6430477532525957323638456b2b49704a6f6b6a464d42516f766e586b4275645464315274414c467a556e5a497869665944704e7465636a35314c6a72784f354178446635664456694d52315a742b524c67513d3d222c2266792b524163314d366e7a496942714a61387a746d396e356854304c4678545732486431645676456c79634772775543663866644679626230326e366b797758536c616a5637397576684a317a377134624f457630413d3d225d2c226d223a327d5d2c226f7574223a5b7b2276616c7565223a31303030303030303030303030303030302c22746f6b656e54797065223a226447567a644441774d544979222c227075626b657948617368223a2241703242544a394c6e4b2b654c6c755436546164725a4247634b6a467a7a4f6b6a6c74545039656b514c6c71227d5d2c226d756c746961646472223a2253707961364e787533674a664e587046596e38445155445572434d6676716b744e6d415272734b314776476a374a7771317132222c2264617461223a227a7a222c2268617368223a2262303666313936353434326434633238336566366163353366343337323163343637373635393461306332343537373766643462613538363463393435386230227d";
        sign = multiSign.Sign(tx, "",UtilsClass.PWD6);
        assertThat(sign, containsString("Parameter 'PriKey' is mandatory"));//Prikey字段为空
        sign = multiSign.Sign(tx, "abc",UtilsClass.PWD6);
        assertThat(sign, containsString("Private Key must be base64 string"));//	Prikey字段传入非base64编码的值
        sign = multiSign.Sign(tx, "YWJjeHg=",UtilsClass.PWD6);
        assertThat(sign, containsString("Incorrect private key or password"));//	Prikey字段传入一些没有意义的字符的base64编码的值
        sign = multiSign.Sign("", UtilsClass.PRIKEY6,UtilsClass.PWD6);
        assertThat(sign, containsString("Invalid parameter -- Tx"));//	tx字段为空值
        sign = multiSign.Sign("123", UtilsClass.PRIKEY6,UtilsClass.PWD6);
        assertThat(sign, containsString("Invalid parameter -- Tx"));//	tx字段传入没有意义的字符
        sign = multiSign.Sign(tx, UtilsClass.PRIKEY6,"");
        assertThat(sign, containsString("Incorrect private key or password"));//	Pwd字段为空
        sign = multiSign.Sign(tx, UtilsClass.PRIKEY6,"222");
        assertThat(sign, containsString("Incorrect private key or password"));//	Pwd字段不匹配

    }

    /**
     * TC282 发行Token异常测试
     * token类型长度
     * 入参金额的大小、精度
     * 未在CA中配置3/3地址/删除配置的3/3地址
     */
    @Test
    public void TC282_issueTokenInvalid() {
        String tokenType = "CX-" + UtilsClass.Random(4);
        String amount = "90";
        log.info(MULITADD2 + "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD2" + "发行" + tokenType + " token，数量为：" + amount;
        //String response = multiSign.issueToken(MULITADD2, tokenType+"123456789000000000000000000", amount, data);
        String response2 = multiSign.issueToken(MULITADD2, tokenType, MaxValuePlus1, data);
        String response4 = multiSign.issueToken("0123", tokenType, amount, data);
        // String response5 = multiSign.issueToken("0123", tokenType, amount, data);
        // assertThat(response, containsString("400"));
//        assertThat(response2, containsString("400"));
//        assertThat(response4, containsString("400"));
        assertThat(response2, containsString("Amount must be greater than 0 and less than "+MaxValue));
        assertThat(response4, containsString("Invalid multiple address"));
        //     assertThat(response5, containsString("400"));

    }

    /**
     * TC21-23多签地址回收异常测试
     */
    @Test
    public void TC21_23recycleInvalid() {
        String tokenType = "cx-8oVNI";
        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        assertThat(queryInfo, containsString("200"));
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, "abc", "1");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "0");
        String recycleInfo3 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, MaxValuePlus1);
        String recycleInfo4 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "-10");
        String recycleInfo5 = multiSign.Recycle(IMPPUTIONADD, "123", tokenType, "1");
        String recycleInfo6 = multiSign.Recycle(IMPPUTIONADD, PRIKEY3, tokenType, "1");
        String recycleInfo7 = multiSign.Recycle(IMPPUTIONADD, "0", tokenType, "1");
        String recycleInfo8 = multiSign.Recycle("0", PRIKEY4, tokenType, "1");
        String recycleInfo9 = multiSign.Recycle(MULITADD3, PRIKEY4, tokenType, "1");
//        assertThat(recycleInfo, containsString("400"));
//        assertThat(recycleInfo2, containsString("400"));
//        assertThat(recycleInfo3, containsString("400"));
//        assertThat(recycleInfo4, containsString("400"));
//        assertThat(recycleInfo5, containsString("400"));
//        assertThat(recycleInfo6, containsString("400"));
//        assertThat(recycleInfo7, containsString("400"));
//        assertThat(recycleInfo8, containsString("400"));
//        assertThat(recycleInfo9, containsString("400"));
        assertThat(recycleInfo, containsString("insufficient balance"));
        assertThat(recycleInfo2, containsString("Amount must be greater than 0 and less than "+MaxValue));
        assertThat(recycleInfo3, containsString("Token amount must be a valid number and less than "+MaxValue));
        assertThat(recycleInfo4, containsString("Token amount must be a valid number and less than "+MaxValue));
        assertThat(recycleInfo5, containsString("Private key must be base64 string"));
        assertThat(recycleInfo6, containsString("Multiaddr is not matching for the prikey"));
        assertThat(recycleInfo8, containsString("Invalid multiple address"));
        assertThat(recycleInfo9, containsString("Multiaddr is not matching for the prikey"));
    }

    /**
     * 发起多签转账交易(带密码)
     */
    @Test
    public void TC1358_transfer(){

        String transfer;
        //最内层的数组封装
        List<Map> list = new ArrayList<>();
        Map map1 = new HashMap<String, Object>();
        map1.put("TokenType","test21");
        map1.put("Amount","10000");
        list.add(map1);
        //倒数第二层的数组封装
        List<Map> tokenList = new ArrayList<>();
        Map map2 = new HashMap<String, Object>();
        map2.put("ToAddr","Spya6Nxu3gJfNXpFYn8DQUDUrCMfvqktNmARrsK1GvGj7Jwq1q2"); //接收方
        map2.put("AmountList",list);
        tokenList.add(map2);


        //最外层的数据的封装
        transfer = multiSign.Transfer("", "", multiaddr2, tokenList);//发送方
        assertThat(transfer, containsString("Parameter 'PriKey' is mandatory"));//	Prikey字段为空值
        transfer = multiSign.Transfer("abc", utilsClass.PWD6,"", multiaddr2, tokenList);
        assertThat(transfer, containsString("Private Key must be base64 string"));//	Prikey传入非base64编码字符的值
        transfer = multiSign.Transfer("YWJjeHg=",utilsClass.PWD6, "", multiaddr2, tokenList);
        assertThat(transfer, containsString("Incorrect private key or password"));//Prikey传入没有意义的base64编码字符的值
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, "","", multiaddr2, tokenList);
        assertThat(transfer, containsString("Incorrect private key or password"));//Pwd字段为空
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, "333","", multiaddr2, tokenList);
        assertThat(transfer, containsString("Incorrect private key or password"));//Pwd字段与Prikey字段不匹配
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, utilsClass.PWD6,"", "", tokenList);
        assertThat(transfer, containsString("Parameter MultiAddr is mandatory"));//MultiAddr字段为空值
         transfer = multiSign.Transfer(UtilsClass.PRIKEY6, utilsClass.PWD6,"", "122", tokenList);
        assertThat(transfer, containsString("Invalid multiple address"));//Multiaddr字段传入非法字符
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, "111","", "4QqVU8DvcZNWQ7mAiuq8SFzZkhKW27PRAgo91Q716KqvK3jYxo", tokenList);
//        assertThat(transfer, containsString("Multiaddr is not matching for the prikey"));//Multiaddr字段不匹配
        assertThat(transfer, containsString("not found multiaddress"));//Multiaddr字段不匹配
        map2.put("ToAddr",""); //接收方
        tokenList.add(map2);
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, utilsClass.PWD6,"", multiaddr2, tokenList);
        assertThat(transfer, containsString("Invalid multiple address"));//ToAddr字段为空
        map2.put("ToAddr","111"); //接收方
        tokenList.add(map2);
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, utilsClass.PWD6,"", multiaddr2, tokenList);
        assertThat(transfer, containsString("Invalid multiple address"));//ToAddr字段传入非法字符

        //Amount字段为空值
        map1.put("Amount","");
        list.add(map1);
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, utilsClass.PWD6,"", multiaddr2, tokenList);
        assertThat(transfer, containsString("Token amount must be a valid number and less than "+MaxValue));

        //Amount字段为负值
        map1.put("Amount","-1");
        list.add(map1);
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, utilsClass.PWD6,"", multiaddr2, tokenList);
        assertThat(transfer, containsString("Token amount must be a valid number and less than "+MaxValue));

        //Amount超出最大值
        map1.put("Amount",MaxValuePlus1);
        list.add(map1);
        transfer = multiSign.Transfer(UtilsClass.PRIKEY6, utilsClass.PWD6,"", multiaddr2, tokenList);
        assertThat(transfer, containsString("Token amount must be a valid number and less than "+MaxValue));
    }

    /**
     * 回收Token
     */
    @Test
    public void TC1359_recycle(){
        String recycle;
        recycle = multiSign.Recycle(multiaddr2, "abc", Tokentype, "1000");
        assertThat(recycle, containsString("Private key must be base64 string"));//PriKey字段为非base64编码格式的字符
        recycle = multiSign.Recycle(multiaddr2, "YWJjeHg=", Tokentype, "1000");
        assertThat(recycle, containsString("Incorrect private key or password"));  //Prikey传入没有意义的base64编码字符的值
        recycle = multiSign.Recycle("", UtilsClass.PRIKEY6,UtilsClass.PWD6, Tokentype, "1000");
        assertThat(recycle, containsString("unsupport pem file"));  //MultiAddr字段为空值
        recycle = multiSign.Recycle("abc", UtilsClass.PRIKEY6, UtilsClass.PWD6,Tokentype, "1000");
        assertThat(recycle, containsString("Invalid multiple address")); //MultiAddr字段为非法字符
        recycle = multiSign.Recycle(multiaddr2, UtilsClass.PRIKEY6, UtilsClass.PWD6,"", "1000");
        assertThat(recycle, containsString("Token type is mandatory")); //TokenType字段为空
        //Amount字段为空值
        recycle = multiSign.Recycle(multiaddr2, UtilsClass.PRIKEY6, UtilsClass.PWD6,Tokentype, "");
        assertThat(recycle, containsString("Token amount must be a valid number and less than "+MaxValue));
        //Amount字段为负值
        recycle = multiSign.Recycle(multiaddr2, UtilsClass.PRIKEY6, UtilsClass.PWD6,Tokentype, "-1");
        assertThat(recycle, containsString("Token amount must be a valid number and less than "+MaxValue));
        //Amount超出最大值
        recycle = multiSign.Recycle(multiaddr2, UtilsClass.PRIKEY6, UtilsClass.PWD6,Tokentype, MaxValuePlus1);
        assertThat(recycle, containsString("Token amount must be a valid number and less than "+MaxValue));

        recycle = multiSign.Recycle(multiaddr2, UtilsClass.PRIKEY6, "",Tokentype, "10100000");
        assertThat(recycle, containsString("Incorrect private key or password")); //Pwd字段为空值
        recycle = multiSign.Recycle(multiaddr2, UtilsClass.PRIKEY6, "55",Tokentype, "10100000");
        assertThat(recycle, containsString("Incorrect private key or password")); //Pwd字段与Prikey不匹配

    }
    /**
     * 冻结token
     */
    @Test
    public void TC1360_freeze(){
        String freeze;
        freeze = multiSign.freezeToken("", Tokentype);
        assertThat(freeze, containsString("200"));//	Prikey字段为空值
        freeze = multiSign.freezeToken("abc", Tokentype);
        assertThat(freeze, containsString("Duplicate transaction")); //	Prikey字段为非base64编码格式的字符
        freeze = multiSign.freezeToken("YWJjeHg=", Tokentype);
        assertThat(freeze, containsString("Duplicate transaction")); //	Prikey传入没有意义的base64编码字符的值
        freeze = multiSign.freezeToken(UtilsClass.PRIKEY1, "");
        assertThat(freeze, containsString("tokentype is mandatory")); //	Tokentype字段为空值

        String recover;
        recover = multiSign.recoverFrozenToken("", Tokentype);
        assertThat(recover, containsString("200"));//	Prikey字段为空值
        recover = multiSign.recoverFrozenToken("abc", Tokentype);
        assertThat(recover, containsString("Duplicate transaction")); //	Prikey字段为非base64编码格式的字符
        recover = multiSign.recoverFrozenToken("YWJjeHg=", Tokentype);
        assertThat(recover, containsString("Duplicate transaction")); //	Prikey传入没有意义的base64编码字符的值
        recover = multiSign.recoverFrozenToken(UtilsClass.PRIKEY1, "");
        assertThat(recover, containsString("tokentype is mandatory")); //Tokentype字段为空值


    }

    /**
     * 添加归集地址携带私钥
     */
    //@Test
    public void collAddressRemovePri(){
        String addcoll = multiSign.collAddressRemovePri(multiaddr2,multiaddr1);
        assertThat(addcoll, containsString("200"));
    }


    /**
     * 添加发行地址携带私钥
     */
    //@Test
    public void addissueaddressRemovePri(){
        String addissueaddress = multiSign.addissueaddress(utilsClass.PUBKEY6,multiaddr2);
        assertThat(addissueaddress, containsString("200"));
    }

    /**
     * 删除归集地址不携带私钥
     */
   // @Test
    public void delCollAddressRemovePri(){
        String deletecoll = multiSign.delCollAddressRemovePri(multiaddr2,multiaddr1);
        assertThat(deletecoll, containsString("200"));
    }



    /**
     * 删除发行地址不携带私钥
     */
   // @Test
    public void delissueaddressRemovePri(){
        String delissueaddress = multiSign.delissueaddressRemovePri(multiaddr2,multiaddr1);
        assertThat(delissueaddress, containsString("200"));
    }

    /**
     * 获取总发行量,总回收量,总冻结量,总解冻量
     */
    @Test
    public void TC1384_gettotal(){
        multiSign.gettotal(0,1555984609,"11");
    }


    /**
     * 获取tokentype发行量
     */
    @Test
    public void TC1404_tokenstate(){
        String tokenstate;
        tokenstate = multiSign.tokenstate("Mutitoken4");
        assertThat(tokenstate, containsString("200"));
        tokenstate = multiSign.tokenstate("");
        assertThat(tokenstate, containsString("200"));
        tokenstate = multiSign.tokenstate("99");
        assertThat(tokenstate, containsString("200"));
    }

    /**
     * 根据tokentype获取账户余额
     */
    @Test
    public void TC1405_getSDKBalance(){
        String getbalancebytt;
        getbalancebytt = multiSign.getSDKBalance("Mutitoken4", "SnEkrssxMUTcLWTcWvvYhk3FazE9W2jobrUZEHndumkCRHuLk4d");
        assertThat(getbalancebytt, containsString("200"));
        getbalancebytt = multiSign.getSDKBalance("", "SnEkrssxMUTcLWTcWvvYhk3FazE9W2jobrUZEHndumkCRHuLk4d");
        assertThat(getbalancebytt, containsString("200"));
        getbalancebytt = multiSign.getSDKBalance("**", "SnEkrssxMUTcLWTcWvvYhk3FazE9W2jobrUZEHndumkCRHuLk4d");
        assertThat(getbalancebytt, containsString("200"));
        getbalancebytt = multiSign.getSDKBalance("Mutitoken4", "");
        assertThat(getbalancebytt, containsString("addr should not be empty"));
        getbalancebytt = multiSign.getSDKBalance("Mutitoken4", "111");
        assertThat(getbalancebytt, containsString("Invalid multiple address"));
    }

}

