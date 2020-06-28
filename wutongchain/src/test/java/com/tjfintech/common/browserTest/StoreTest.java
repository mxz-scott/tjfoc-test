package com.tjfintech.common.browserTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.resourcePath;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class StoreTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();

    /**
     * 普通存证，内容为text
     * 数量为1
     */
    @Test
    public void TC001_createStore() throws Exception {

        String Data = "text"+UtilsClass.Random(4);
        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));
        assertThat(response,containsString("figure"));
    }



    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC002_createStore() throws Exception {

        String Data = "{\"testJson1\":\"json"+utilsClass.Random(6)+"\"}";
        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));
        assertThat(response,containsString("figure"));
    }


    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC003_createStore() throws Exception {

        String Data = "{\"testJson1\":\"json"+utilsClass.Random(6)+"\"," +
                "\"City\":\"Suzhou\"}";
        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));
    }

    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC004_createStore() throws Exception {

        String Data = "{\"testJson2\":\"json"+utilsClass.Random(6)+"\"," +
                "\"City\":\"Suzhou\"," +
                "\"Name\":\"Sam\"}";
        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));
        assertThat(response,containsString("figure"));
    }

    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC005_createStore() throws Exception {

        JSONObject result = new JSONObject();
        result.put("testJson3", utilsClass.Random(6));
        result.put("City", "Suzhou");
        result.put("Name", "Sam");
        result.put("Address", "Gao tie xin cheng");
        result.put("Company", "Tongji");
        result.put("Position", "TestEngineer");
        String Data = result.toString();
        log.info(Data);

        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));
        assertThat(response,containsString("figure"));
    }

    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC006_createStore() throws Exception {

        for (int i = 0; i < 30; i++) {
            JSONObject fileInfo = new JSONObject();
            JSONObject data = new JSONObject();

            fileInfo.put("fileName", "201911041058.jpg");
            fileInfo.put("fileSize", "298KB");
            fileInfo.put("fileModel", utilsClass.Random(6));
            fileInfo.put("fileLongitude", 123.45784545);
            fileInfo.put("fileStartTime", "1571901219");
            fileInfo.put("fileFormat", "jpg");
            fileInfo.put("fileLatitude", 31.25648);

            data.put("projectCode", utilsClass.Random(10));
            data.put("waybillId", "1260");
            data.put("fileInfo", fileInfo);
            data.put("fileUrl", "/var/mobile/containers/data/111");
            data.put("projectName", "钰翔供应链测试005");
            data.put("projectId", "1234");
            data.put("fileType", "2");
            data.put("fileId", "");
            data.put("waybillNo", "y201911041032");
            String Data = data.toString();
            log.info(Data);

            String response= store.CreateStore(Data);
            Thread.sleep(1000);
            assertThat(response, containsString("200"));
            assertThat(response,containsString("data"));
            assertThat(response,containsString("figure"));


        }

    }


    /**
     * 普通存证，内容为大数据
     * 数量为2
     */
    @Test
    public void TC010_createBigSizeStore() throws Exception {

        String Data = UtilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath +
                "bigsize1.txt");
        String response = store.CreateStore(Data);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));


        String Data2 = UtilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath
                +  "bigsize2.txt");
        String response2 = store.CreateStore(Data2);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
    }

}
