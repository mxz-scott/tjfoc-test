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
        assertThat(response,containsString("Data"));
        assertThat(response,containsString("Figure"));
    }



    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC002_createStore() throws Exception {

        String Data = "{\"testJson1\":\"json"+UtilsClass.Random(6)+"\"}";
        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));
        assertThat(response,containsString("Figure"));
    }


    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC003_createStore() throws Exception {

        String Data = "{\"testJson1\":\"json"+UtilsClass.Random(6)+"\"," +
                "\"City\":\"Suzhou\"}";
        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));
    }

    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC004_createStore() throws Exception {

        String Data = "{\"testJson2\":\"json"+UtilsClass.Random(6)+"\"," +
                "\"City\":\"Suzhou\"," +
                "\"Name\":\"Sam\"}";
        String response= store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));
        assertThat(response,containsString("Figure"));
    }

    /**
     * 普通存证，内容为json
     * 数量为1
     */
    @Test
    public void TC005_createStore() throws Exception {

        JSONObject result = new JSONObject();
        result.put("testJson3", UtilsClass.Random(6));
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
        assertThat(response,containsString("Data"));
        assertThat(response,containsString("Figure"));
    }

    /**
     * 普通存证，内容为大数据
     * 数量为2
     */
    @Test
    public void TC010_createBigSizeStore() throws Exception {

        String Data = UtilsClass.Random(10) + UtilsClass.readStringFromFile(resourcePath +
                "bigsize1.txt");
        String response = store.CreateStore(Data);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("Data"));


        String Data2 = UtilsClass.Random(10) + UtilsClass.readStringFromFile(resourcePath
                +  "bigsize2.txt");
        String response2 = store.CreateStore(Data2);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("Data"));
    }

}
