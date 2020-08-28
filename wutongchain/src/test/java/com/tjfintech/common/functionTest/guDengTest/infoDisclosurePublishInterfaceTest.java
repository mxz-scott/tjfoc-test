package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class infoDisclosurePublishInterfaceTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String contractAddr = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
    String platformKeyID = "bt45k19pgfltc7nnqn50";
    String companyId = "companyI1100001";
    String clientNo = "cI1100001";
    String equityCode = "SZI1100001";



    @Test
    public void infoDisclosurePublishInterfaceMustParamTest() throws Exception {

        String type = "公告";
        String subType = "企业公告";
        String title = "挂牌企业登记信息";
        String fileHash = "dfhafdd1111111651575452";
        String fileURL = "test/publish/company0001info";
        String hashAlgo = "sha256";
        String publisher = "上海股权托管登记交易所";
        String publishTime = "20200828 10:43";
        String enterprise = "201804152125222515";


        log.info(" ************************ test type must ************************ ");
        String response= gd.GDInfoPublish("",subType,title,fileHash,fileURL,hashAlgo,publisher,publishTime,enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test subType must ************************ ");
        response= gd.GDInfoPublish(type,"",title,fileHash,fileURL,hashAlgo,publisher,publishTime,enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test title must ************************ ");

        response= gd.GDInfoPublish(type,subType,"",fileHash,fileURL,hashAlgo,publisher,publishTime,enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test fileHash must ************************ ");

        response= gd.GDInfoPublish(type,subType,title,"",fileURL,hashAlgo,publisher,publishTime,enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test hashAlgo must ************************ ");

        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,"",publisher,publishTime,enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test publisher must ************************ ");

        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,hashAlgo,"",publishTime,enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test publishTime must ************************ ");

        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,hashAlgo,publisher,"",enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test enterprise must ************************ ");

        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,hashAlgo,publisher,publishTime,"");

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test all must ************************ ");

        response= gd.GDInfoPublish("","","","","","","","","");

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


    }

    @Test
    public void infoDisclosurePublishInterfaceNotNecessaryParamTest() throws Exception {

        String type = "公告";
        String subType = "企业公告";
        String title = "挂牌企业登记信息";
        String fileHash = "dfhafdd1111111651575452";
        String hashAlgo = "sha256";
        String publisher = "上海股权托管登记交易所";
        String publishTime = "20200828 10:43";
        String enterprise = "201804152125222515";


        log.info(" ************************ test fileURL must ************************ ");
        String response= gd.GDInfoPublish(type,subType,title,fileHash,"",hashAlgo,publisher,publishTime,enterprise);

//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
    }
}
