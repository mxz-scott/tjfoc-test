package com.tjfintech.common.performanceTest;


import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.TOKENADD;


@Slf4j
public class CreateAccount {
    static TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();

    @BeforeClass
    public static void init()throws Exception {
        SDKADD = TOKENADD;
    }

    @Test
    public void createTokenAccount()throws Exception{
        ArrayList<String> listTag = new ArrayList<>();

        listTag.add("test");
        listTag.add("test02");
        listTag.add("test03");

        ArrayList<String> accounts = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            String userid = UtilsClass.Random(10);

            String account = JSONObject.fromObject(
                    tokenModule.tokenCreateAccount(userid,userid,"","",listTag)).getString("data");

            accounts.add(account);
            Thread.sleep(50);
            //log.info(account);
        }

        Array2CSV(accounts,"accounts.csv");


    }

    public static void Array2CSV(ArrayList<String> data, String path)
    {
        try {
            BufferedWriter out =new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));
            for (int i = 0; i < data.size(); i++)
            {
                String onerow = data.get(i);
                     out.write(onerow);
                     out.write(",");
                     out.newLine();
            }
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}


