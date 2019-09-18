package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Token {

    String createGroup(String id, String name, String comments, Map tags);

    String createAccount(String entityID, String entityName, String groupID, String comments, Map tags);

    String createMultiAddrAccount(Map addresses, String name, String minSignatures, String groupID,
                                  String comments, Map tags);



}
