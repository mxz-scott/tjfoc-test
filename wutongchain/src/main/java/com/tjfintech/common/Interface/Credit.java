package com.tjfintech.common.Interface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Credit {

    String creditIdentityAdd(String name, String code, String type, String contractAddress, String pubKey, String address, String description);

    String creditIdentityQuery(String code);

    String creditIdentityQueryAll();

    String creditCreditdataAdd(List<Map> creditDataList, String name);

    String creditCreditdataQuery(String EnterpriseCode);

    String creditAuthorizationAdd(ArrayList<String> orgID, List<Map> authorizationList);

    String creditAuthorizationQuery(String key);

    String creditViewhistoryAdd(ArrayList<String> orgID, List<Map> viewHistoryList);

    String creditViewhistoryQuery(String key);


}
