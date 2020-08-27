package com.tjfintech.common.Interface;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GuDeng {

    String GDEnterpriseResister(String contractAddress,Map basicInfo, Map businessInfo, Map legalPersonInfo, String extend);
    String GDCreateAccout(String contractAddress,Map investorInfo);

}
