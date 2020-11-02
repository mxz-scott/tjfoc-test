package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONObject;

import static com.tjfintech.common.utils.UtilsClass.testResultPath;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

public class TestCreditDataContract {
    WVMContractTest wvmContractTest = new WVMContractTest();
    UtilsClass utilsClass = new UtilsClass();
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();

    @org.junit.Test
    public void InstallCreditData()throws Exception{
        String filePath = "D:\\GoWorks\\src\\github.com\\tjfoc\\zxdemo\\contract\\creditdata.wlang";
        String file = utilsClass.readInput(filePath).toString().trim();
        String Prikey ="LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ3Y2R0NWb0NMcVp2SkpjYW4KRDQvMDRYUTF1WEJSZk80aHRNT3p6L2Q5VXFPZ0NnWUlLb0VjejFVQmdpMmhSQU5DQUFUQkdibGhmQVJIZDk0OApDYlYxUDkxT3ZyVmxKNHBtS21KcFZFLzFsQmcxS2kyZEtVOUMxK2xlTnVyM1hiZTliK3U1VDd0RUkrYWxDU0V5CkI2QXZSL1ZpCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
        String data = utilsClass.encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
        String response=contract.InstallWVM(data,"wvm",Prikey);
        assertEquals("200",JSONObject.fromObject(response).getString("State"));

    }


    @org.junit.Test
    public void InstallAuthorization()throws Exception{
        String filePath = "D:\\GoWorks\\src\\github.com\\tjfoc\\zxdemo\\contract\\authorization.wlang";
        String file = utilsClass.readInput(filePath).toString().trim();
        String Prikey ="LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ3Y2R0NWb0NMcVp2SkpjYW4KRDQvMDRYUTF1WEJSZk80aHRNT3p6L2Q5VXFPZ0NnWUlLb0VjejFVQmdpMmhSQU5DQUFUQkdibGhmQVJIZDk0OApDYlYxUDkxT3ZyVmxKNHBtS21KcFZFLzFsQmcxS2kyZEtVOUMxK2xlTnVyM1hiZTliK3U1VDd0RUkrYWxDU0V5CkI2QXZSL1ZpCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
        String data = utilsClass.encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
        String response=contract.InstallWVM(data,"wvm",Prikey);
        assertEquals("200",JSONObject.fromObject(response).getString("State"));

    }
//    @org.junit.Test
    public void InvokeTest()throws Exception{
        String sign = "3045022100ac8c5674f4eaab179e645ae7ddaa259317008e316faa0b5b8f13a87a9884da8402206510431c54d06a5ab905d81d56a2aabec5f327dc9abcf0ef9200918a9e0b5b40";
        String name = "76b24a2b5cb79b4ea3e971f53379aafea7c79e2fabec43cdb505a865c3bdb408";

        String usccSaveFile = testResultPath + "uscc.txt";
        FileOperation fo = new FileOperation();

        for(int i=0;i<1500000;i++) {
            String uscc = "uscc" + UtilsClass.Random(21);
            System.out.println("current test count:" + i);

            String resp = wvmContractTest.invokeNew(name, "AddCreditData", sign,
                    "\"{\"USCC\":\"" + uscc + "\",\"TimeStamp\":\"1587344781\",\"DataList\":[{\"Level1\":\"BSOE\",\"Level2\":\"ID\",\"Data\":\"112346\",\"Description\":\"aabbcc\"},{\"Level1\":\"BSOE\",\"Level2\":\"SHIS\",\"Data\":\"12347\",\"Description\":\"bbccdd\"},{\"Level1\":\"BSOE\",\"Level2\":\"DSS\",\"Data\":\"12345\",\"Description\":\"ccddee\"},{\"Level1\":\"SSFD\",\"Level2\":\"SSFDDetails\",\"Data\":\"457899\",\"Description\":\"eeffgg\"}]}\"");

            //assertEquals("200", JSONObject.fromObject(resp).getString("State"));
            fo.appendToFile(uscc, usccSaveFile);
        }
    }
}
