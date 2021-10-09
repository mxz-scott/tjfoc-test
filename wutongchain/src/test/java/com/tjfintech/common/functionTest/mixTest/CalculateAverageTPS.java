package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.utils.ExcelOperation.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.FileOperation.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CalculateAverageTPS {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();


    /**
     计算链上TPS
     */
    @Test
    public void CalculateAverageTPS() throws Exception {

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        int startBlockHeight;

        if (blockHeight > 100) {
            startBlockHeight = blockHeight - 99;
        }else {
            startBlockHeight = 1;
        }
        //手动修改起始高度
//        startBlockHeight = 1090;
        int endBlockHeight = blockHeight;   //手动修改结束高度

        int diff = endBlockHeight - startBlockHeight + 1;
        int count = 0, total = 0;

        for (int i = startBlockHeight; i <= endBlockHeight; i++) {


            //获取区块中的交易个数
            String[] txs = commonFunc.getTxsArray(i);
            count = txs.length;

            total = total + count;

        }


            String timestamp = JSONObject.fromObject(store.GetBlockByHeight(startBlockHeight)).getJSONObject("data").getJSONObject("header").getString("timestamp");
            long blkTimeStamp1 = Long.parseLong(timestamp);
            timestamp = JSONObject.fromObject(store.GetBlockByHeight(endBlockHeight)).getJSONObject("data").getJSONObject("header").getString("timestamp");
            long blkTimeStamp2 = Long.parseLong(timestamp);

            long timeDiff = (blkTimeStamp2 - blkTimeStamp1) / 1000;

        log.info("区块数：" + diff);
        log.info("交易总数：" + total);
        log.info("测试时长：" + timeDiff + "秒");
        log.info("链上TPS：" + total / timeDiff);

    }

    /**
     计算链上TPS
     */
    @Test
    public void CalculateAverageTPS2() throws Exception {

        SDKADD = "http://121.229.39.12:38080";
        int startBlockHeight = 1057;

        //手动修改起始高度
//        startBlockHeight = 1090;
        int endBlockHeight = 1328;   //手动修改结束高度

        int diff = endBlockHeight - startBlockHeight + 1;
        int count = 0, total = 0;

        for (int i = startBlockHeight; i <= endBlockHeight; i++) {


            //获取区块中的交易个数
            String[] txs = commonFunc.getTxsArray(i);
            count = txs.length;

            total = total + count;

        }


        String timestamp = JSONObject.fromObject(store.GetBlockByHeight(startBlockHeight-1)).getJSONObject("data").getJSONObject("header").getString("timestamp");
        long blkTimeStamp1 = Long.parseLong(timestamp);
        timestamp = JSONObject.fromObject(store.GetBlockByHeight(endBlockHeight)).getJSONObject("data").getJSONObject("header").getString("timestamp");
        long blkTimeStamp2 = Long.parseLong(timestamp);

        long timeDiff = (blkTimeStamp2 - blkTimeStamp1) / 1000;

        log.info("区块数：" + diff);
        log.info("交易总数：" + total);
        log.info("测试时长：" + timeDiff + "秒");
        log.info("链上TPS：" + total / timeDiff);

    }

    @Test
    public void calTPSFromTestLog()throws Exception {
        String procFile = "TPS\\TPS2.txt"; //性能测试节点日志文件 需要下载到本地 默认从...\src\github.com\tjfoc\tjfoc-test\wutongchain目录下获取
        makeupTPSToExcel(procFile);  //将从日志文件解析的数据存入excel  仅计算单个区块TPS 整体TPS 需另外计算
    }

    /**
     * 将结果存入txt 默认目录...\src\github.com\tjfoc\tjfoc-test\wutongchain
     * * "区块起始时间"  日志log中包含In AddBlockToChain行中的时间
     *      * "起始时间戳"  上述时间转为时间戳
     *      * "区块结束时间" 日志log中包含Exit AddBlockToChain行中的时间
     *      * "结束时间戳"  上述时间转为时间戳
     *      * "区块高度"  日志log中包含In AddBlockToChain行中的height:后的信息
     *      * "区块交易数" 日志log中包含In AddBlockToChain行中的txs:后的信息
     *      * "区块内交易处理时间" 日志log中包含In AddBlockToChain行中时间与包含Exit AddBlockToChain行中的时间间隔
     *      * "IETPS"  以上个参数“区块内交易处理时间”作为“出块间隔”的单个区块tps计算结果 比较理想 仅供参考
     *      * "出块时间"  前一个包含Exit AddBlockToChain行中的时间与后一个包含Exit AddBlockToChain行中的时间间隔
     *      * "单个区块TPS" 以上个参数"出块时间"作为“出块间隔”的单个区块tps计算结果 相对来说比较符合实际TPS
     * @param fileName
     * @throws Exception
     */
    public void makeupTPSFile(String fileName)throws Exception {
        File fin = new File(fileName);
        FileInputStream fis = new FileInputStream(fin);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = null;


        String tpsCal = "TPS\\tpsCal.txt";
        FileOperation fo = new FileOperation();
        fo.appendToFile("区块起始时间\t\t\t\t起始时间戳\t\t\t区块结束时间\t\t\t\t结束时间戳\t\t\t区块高度\t区块交易数\t\t" +
                "区块内交易处理时间\t\tIETPS\t\t\t出块时间\t\t单个区块TPS", tpsCal);
        String timeIn = "";
        long timeInStmp = 0;
        String height = "";
        String txs = "";
        String timeExit = "";
        long timeExitStmp = 0;
        boolean bwrite = false;
        Date dStmp = new Date();
        long timeExitStmpLast = 0;
        long timePrePostIv = 0;
        long timeInExitIv = 0;
        long IEtps = 0;
        long PPtps = 0;

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        while ((line = br.readLine()) != null) {
            if (line.contains("in AddBlock")) {
                timeIn = line.substring(0, 23).trim();
                timeInStmp = sf.parse(timeIn).getTime();
                height = line.substring(line.indexOf("height:") + 7, line.indexOf("pre:") - 1).trim();
                txs = line.substring(line.indexOf("txs:") + 4, line.indexOf("height:") - 1).trim();
                bwrite = false;
            } else if (line.contains("exit AddBlock")) {
                timeExit = line.substring(0, 23).trim();
                timeExitStmp = sf.parse(timeExit).getTime();
                timePrePostIv = timeExitStmp - timeExitStmpLast;
                timeInExitIv = timeExitStmp - timeInStmp;
                timeExitStmpLast = timeExitStmp;

                bwrite = true;
            }
            if (bwrite) {
                IEtps = Long.parseLong(txs) * 1000 / timeInExitIv;
                PPtps = Long.parseLong(txs) * 1000 / timePrePostIv;
                fo.appendToFile(timeIn + "\t\t" + timeInStmp + "\t\t" + timeExit + "\t\t" + timeExitStmp
                        + "\t\t" + height + "\t\t\t" + txs + "\t\t\t\t" + timeInExitIv + "\t\t\t\t" + IEtps
                        + "\t\t\t\t" + timePrePostIv + "\t\t\t" + PPtps, tpsCal);
            }
        }
    }

    /**
     * 将结果存入excel 默认目录...\src\github.com\tjfoc\tjfoc-test\wutongchain
     * "区块起始时间"  日志log中包含In AddBlockToChain行中的时间
     * "起始时间戳"  上述时间转为时间戳
     * "区块结束时间" 日志log中包含Exit AddBlockToChain行中的时间
     * "结束时间戳"  上述时间转为时间戳
     * "区块高度"  日志log中包含In AddBlockToChain行中的height:后的信息
     * "区块交易数" 日志log中包含In AddBlockToChain行中的txs:后的信息
     * "区块内交易处理时间" 日志log中包含In AddBlockToChain行中时间与包含Exit AddBlockToChain行中的时间间隔
     * "IETPS"  以上个参数“区块内交易处理时间”作为“出块间隔”的单个区块tps计算结果 比较理想 仅供参考
     * "出块时间"  前一个包含Exit AddBlockToChain行中的时间与后一个包含Exit AddBlockToChain行中的时间间隔
     * "单个区块TPS" 以上个参数"出块时间"作为“出块间隔”的单个区块tps计算结果 相对来说比较符合实际TPS
     * @param fileName
     * @throws Exception
     */
    public void makeupTPSToExcel(String fileName)throws Exception {
        File fin = new File(fileName);
        FileInputStream fis = new FileInputStream(fin);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = null;


        String tpsCal = "TPS\\tpsCal.xls";
        createExcel(tpsCal);
        String[] title = new String[]{"区块起始时间","起始时间戳","区块结束时间","结束时间戳","区块高度","区块交易数",
                "区块内交易处理时间","IETPS","出块时间","单个区块TPS"};
        makeDataForTPSExcel(tpsCal,0,title);

        String timeIn = "";
        long timeInStmp = 0;
        String height = "";
        String txs = "";
        String timeExit = "";
        long timeExitStmp = 0;
        boolean bwrite = false;
        Date dStmp = new Date();
        long timeExitStmpLast = 0;
        long timePrePostIv = 0;
        long timeInExitIv = 0;
        long IEtps = 0;
        long PPtps = 0;
        int count = 0;

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        while ((line = br.readLine()) != null) {
            if (line.contains("in AddBlock")) {
                timeIn = line.substring(0, 23).trim();
                timeInStmp = sf.parse(timeIn).getTime();
                height = line.substring(line.indexOf("height:") + 7, line.indexOf("pre:") - 1).trim();
                txs = line.substring(line.indexOf("txs:") + 4, line.indexOf("height:") - 1).trim();
                bwrite = false;
            } else if (line.contains("exit AddBlock")) {
                timeExit = line.substring(0, 23).trim();
                timeExitStmp = sf.parse(timeExit).getTime();
                timePrePostIv = timeExitStmp - timeExitStmpLast;
                timeInExitIv = timeExitStmp - timeInStmp;
                timeExitStmpLast = timeExitStmp;

                bwrite = true;
            }
            if (bwrite) {
                count ++;//从第二行开始插入数据
                IEtps = Long.parseLong(txs) * 1000 / timeInExitIv;
                PPtps = Long.parseLong(txs) * 1000 / timePrePostIv;

                String[] data = new String[]{timeIn,String.valueOf(timeInStmp),timeExit,String.valueOf(timeExitStmp),height,txs,
                        String.valueOf(timeInExitIv),String.valueOf(IEtps),String.valueOf(timePrePostIv),String.valueOf(PPtps)};
                makeDataForTPSExcel(tpsCal,count,data);
            }
        }
    }
//    @Test
    public void makeDataForTPSExcel(String excelFileName,int lineNo,String ... strlist)throws Exception{
        Map<String, String> dataMap = new HashMap<String, String>();
        writeExcelByRow(strlist,lineNo,excelFileName);
    }
}
