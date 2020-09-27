package com.tjfintech.common.functionTest.CreditTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.MysqlOperation;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.util.*;

import static com.tjfintech.common.utils.FileOperation.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class CreditCommonFunc {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Token tokenModule = testBuilder.getToken();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    MgToolCmd mgToolCmd = new MgToolCmd();
    //获取所有地址账户与私钥密码信息
    JSONObject jsonObjectAddrPri;

    //征信模块相关通用函数
    //-----------------------------------------------------------------------------------------------------------
    //组装征信数据
    public List<Map> constructCreditData(String enterpriseName, String enterpriseCode, String creditName,
                                         String creditCode, String hash, String catalogue, String makeTime, String accessInterface, String description) {

        Map<String, Object> map = new HashMap<>();
        map.put("enterpriseName", enterpriseName);
        map.put("enterpriseCode", enterpriseCode);
        map.put("creditName", creditName);
        map.put("creditCode", creditCode);
        map.put("hash", hash);
        map.put("catalogue", catalogue);
        map.put("makeTime", makeTime);
        map.put("accessInterface", accessInterface);
        map.put("description", description);

        List<Map> creditDataList = new ArrayList<>();
        creditDataList.add(map);
        return creditDataList;
    }

    //组装授权数据
    public List<Map> constructAuthorizationData(String enterpriseName, String enterpriseCode, String creditName,
                                         String creditCode, String bankName, String bankCode, String remoteCreditName, String remoteCreditCode,
                                                String localRemote, String authorizedUrl, String authorizedHash, String authType,
                                                String authStartTime,int authDays) {

        Map<String, Object> map = new HashMap<>();
        map.put("enterpriseName", enterpriseName);
        map.put("enterpriseCode", enterpriseCode);
        map.put("creditName", creditName);
        map.put("creditCode", creditCode);
        map.put("bankName", bankName);
        map.put("bankCode", bankCode);
        map.put("remoteCreditName", remoteCreditName);
        map.put("remoteCreditCode", remoteCreditCode);
        map.put("localRemote", localRemote);
        map.put("authorizedUrl", authorizedUrl);
        map.put("authorizedHash", authorizedHash);
        map.put("authType", authType);
        map.put("authStartTime", authStartTime);
        map.put("authDays", authDays);

        List<Map> authorizationDataList = new ArrayList<>();
        authorizationDataList.add(map);
        return authorizationDataList;
    }

    //组装查询记录数据
    public List<Map> constructViewData(String enterpriseName, String enterpriseCode, String creditName,
                                                String creditCode, String bankName, String bankCode, String remoteCreditName, String remoteCreditCode,
                                                String localRemote, String queryOperator, String queryReason, String queryTime) {

        Map<String, Object> map = new HashMap<>();
        map.put("enterpriseName", enterpriseName);
        map.put("enterpriseCode", enterpriseCode);
        map.put("creditName", creditName);
        map.put("creditCode", creditCode);
        map.put("bankName", bankName);
        map.put("bankCode", bankCode);
        map.put("remoteCreditName", remoteCreditName);
        map.put("remoteCreditCode", remoteCreditCode);
        map.put("localRemote", localRemote);
        map.put("queryOperator", queryOperator);
        map.put("queryReason", queryReason);
        map.put("queryTime", queryTime);

        List<Map> viewDataList = new ArrayList<>();
        viewDataList.add(map);
        return viewDataList;
    }

    //-----------------------------------------------------------------------------------------------------------

    public boolean mapCompare(Map<String, Object> map1, Map<String, Object> map2) {
        boolean isChange = false;
        for (Map.Entry<String, Object> entry1 : map1.entrySet()) {
            Object m1value = entry1.getValue() == null ? "" : entry1.getValue();
            Object m2value = map2.get(entry1.getKey()) == null ? "" : map2.get(entry1.getKey());
            if (!m1value.equals(m2value)) {
                isChange = true;
            }
        }
        return isChange;
    }
}
