package com.tjfintech.common.tokenTest;

import net.sf.json.JSONObject;
import com.tjfintech.common.GoToken;
import com.tjfintech.common.utils.UtilsClass;
import org.junit.Test;
import org.junit.Before;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import static com.tjfintech.common.utils.UtilsClass.SHORTMEOUT;
import static org.hamcrest.Matchers.containsString;

public class SingleAccount {

    GoToken token = new GoToken();

    Map<String,Object> map1=new HashMap<>();
    Map<String,Object> map2=new HashMap<>();
    Map<String,Object> map3=new HashMap<>();

    @Before
    public void beforeConfig() throws Exception {

        map1.put("tags","");

        map2.put("tags","标签1");

        map3.put("tags","标签1");
        map3.put("tags","标签2");
        map3.put("tags","标签3");
        map3.put("tags","标签4");
        map3.put("tags","标签5");
    }


    @Test
    public void TC1909_CreateGroup() throws Exception {

        //第一组
        String id = "g_" + UtilsClass.Random(8);
        String name = "group_" + UtilsClass.Random(8);

        String response= token.createGroup(id, name,"", map1);
        Thread.sleep(SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String state = jsonObject.get("State").toString();
        String message = jsonObject.get("Message").toString();
        String data = jsonObject.get("Data").toString();

        assertThat(state, containsString("200"));
        assertThat(message,containsString("success"));
        assertThat(data, containsString("create group success"));

        //第二组
        id = "g_" + UtilsClass.Random(9);
        name = "group_" + UtilsClass.Random(9);

        response= token.createGroup(id, name,"第二组", map2);
        Thread.sleep(SHORTMEOUT);

        jsonObject=JSONObject.fromObject(response);
        state = jsonObject.get("State").toString();
        message = jsonObject.get("Message").toString();
        data = jsonObject.get("Data").toString();

        assertThat(state, containsString("200"));
        assertThat(message,containsString("success"));
        assertThat(data, containsString("create group success"));

        //第三组
        id = "g_" + UtilsClass.Random(10);
        name = "group_" + UtilsClass.Random(10);

        response= token.createGroup(id, name,"第三组第三组第三组第三组第三组第三组第三组第三组", map3);
        Thread.sleep(SHORTMEOUT);

        jsonObject=JSONObject.fromObject(response);
        state = jsonObject.get("State").toString();
        message = jsonObject.get("Message").toString();
        data = jsonObject.get("Data").toString();

        assertThat(state, containsString("200"));
        assertThat(message,containsString("success"));
        assertThat(data, containsString("create group success"));
    }
}
