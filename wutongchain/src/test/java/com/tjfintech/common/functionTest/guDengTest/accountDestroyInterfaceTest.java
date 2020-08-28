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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class accountDestroyInterfaceTest {

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
    public void TC03_accoutDestroyInterfaceMustParamTest() throws Exception {

        log.info(" ************************ test contractAddress must ************************ ");

        String response = gd.GDAccountDestroy("","testcliI1300001");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test clientNo must ************************ ");

        response = gd.GDAccountDestroy(contractAddr,"");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test all must ************************ ");

        response = gd.GDAccountDestroy("","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数:Key: 'AccountDestroy.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag"));
        assertEquals(true,response.contains("Key: 'AccountDestroy.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag"));


    }
}
