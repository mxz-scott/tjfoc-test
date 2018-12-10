package com.tjfintech.common.functionTest;

import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

@Slf4j
public class ConfigurationTest {
    public static final String PEERIP = "10.1.3.246";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";

    @Test
    public void Tc691_defaultCheck() {
        Shell shell = new Shell(PEERIP, USERNAME, PASSWORD);  //连接节点
        shell.execute("uname -s -r -v");    //执行的命令
        shell.execute("uname -s -r -v");    //执行的命令
        ArrayList<String> stdout = shell.getStandardOutput();
        for (String str : stdout) {
            log.info(str);    //打印返回信息
        }

    }
}
