package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetSDKStartWithApi {
   @Test
    public void set()throws Exception{
       startSDKCmd = "sh "+ destShellScriptDir +"startWithParam.sh \""+ tmuxSessionSDK + "\" " + SDKPATH + " " + SDKTPName + " api";
    }

}
