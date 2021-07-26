package com.tjfintech.common.Interface;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Jml {
    String BankList ();

    String AuthorizeAdd (String subjectType, String bankId, String endTime, String fileHash, Map subject);

    String CreditdataQuery (String requestId, String authId, String personId, String personName, String purpose);

    String CreditloanFeedback (String authId, String[] receiverPubkeys, Map results);
}
