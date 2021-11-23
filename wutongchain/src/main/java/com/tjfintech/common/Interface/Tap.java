package com.tjfintech.common.Interface;

import java.util.Map;

public interface Tap {

    String tapProjectInit(String TENDER_PROJECT_CODE, String TENDER_PROJECT_NAME, String BID_SECTION_NAME, String BID_SECTION_CODE,
                          String KAIBIAODATE, String BID_DOC_REFER_END_TIME, String BID_SECTION_STATUS, String TBFILE_ALLOWLIST,
                          int TBALLOWFILESIZE, String TBTOOL_ALLOWVERSION, String TBFILEVERSION, String ZBRPULICKEY, String BID_SECTION_CODE_EX, Map EXTRA);

    String tapProjectUpdate(String ORDERNO, String TENDER_PROJECT_CODE, String TENDER_PROJECT_NAME, String BID_SECTION_NAME, String BID_SECTION_CODE,
                            String KAIBIAODATE, String BID_DOC_REFER_END_TIME, String BID_SECTION_STATUS, String TBFILE_ALLOWLIST, int TBALLOWFILESIZE,
                            String TBTOOL_ALLOWVERSION, String TBFILEVERSION, String BID_SECTION_CODE_EX, Map EXTRA, String SIGN);

    String tapProjectDetail(String orderNo);

    String tapTenderVerify(String hashvalue, String sender);

    String tapTenderUpload(String orderNo, String recordId, String fileHead, String path);

    String tapTenderRevoke(String data, String orderNo);

    String tapProjectList();

    String tapTenderRecord(String orderNo, String recordId, Boolean detail, String sign);

    String tapTenderOpen(String orderNo, String sign);

}
