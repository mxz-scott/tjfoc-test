package com.tjfintech.common.utils;



import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelOperation {

    @Test
    public void test() throws Exception{
        //此处为股登监管数据sample数据的代码组合，需要对原始的监管数据excel进行写处理，删除版本sheet
        //其余每个sheet仅保留"中文名称","英文名称","数据类型","枚举值说明" 栏，并作为第一栏标题
        //此工具仅做辅助使用
        String filePath = "D://1.xlsx";
        String columns[] = {"中文名称","英文名称","数据类型","枚举值说明"};
        writeExcelDataToFileForGDCode(filePath,columns);
    }

    public void writeExcelDataToFileForGDCode(String excelPath,String[] columnsName)throws Exception{
        List<Map<String,String>> list = new ArrayList<>();
        Workbook wb = readExcel(excelPath);
        if(wb != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdTimeNow = sdf.format((new Date()).getTime()); // 时间戳转换日期
            //遍历所有sheet
            for (int k = 1; k < wb.getNumberOfSheets(); k++) {
                //用来存放表中数据
                list.clear();
                Sheet sheet = wb.getSheetAt(k);
                list = readSheetCells(sheet, columnsName);

                String destFile = UtilsClass.testResultPath + sheet.getSheetName() + "_mapcode.txt";
                File file = new File(destFile);
                if(file.exists()) file.delete();

                FileOperation fo1 = new FileOperation();

                //遍历解析出来的list
                for (Map<String, String> map : list) {
                    if (!map.get("英文名称").isEmpty()) {
                        String tempData = "map.put(" + constructDataCodeString(
                                map.get("英文名称"), map.get("数据类型"), map.get("枚举值说明")) + ");";
                        fo1.appendToFile(tempData, destFile);
                    }
                    //打印每个单元格内容 + 标题
                    // for (Map.Entry<String,String> entry : map.entrySet()) {
                    // System.out.print(entry.getKey()+":"+entry.getValue()+",");
                    // }
                    // System.out.println();

                }
            }
        }
    }

    public List<Map<String,String>> readSheetCells(Sheet sheet, String[] columns){
        List<Map<String,String>> list = new ArrayList<>();
        String cellData = null;
        Row row = null;
        System.out.print("sheet name " + sheet.getSheetName() + "\n");
        //获取最大行数
        int rownum = sheet.getPhysicalNumberOfRows();
        //获取第一行
        row = sheet.getRow(2);
        //获取最大列数
        int colnum = row.getPhysicalNumberOfCells();
        for (int i = 1; i < rownum; i++) {
            Map<String, String> map = new LinkedHashMap<String, String>();
            row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < colnum; j++) {
                    cellData = (String) getCellFormatValue(row.getCell(j));
                    map.put(columns[j], cellData);
                }
            } else {
                break;
            }
            list.add(map);
        }
        return list;
    }

    public void constructData(Map tempMap,String data1,String data2,String data3){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");
        String sdTimeNow = sdf.format((new Date()).getTime()); // 时间戳转换日期
        String sdTimeNow2 = sdf2.format((new Date()).getTime()); // 时间戳转换日期
        List<String> list1 = new ArrayList<>();
        String file1 = UtilsClass.Random(3) + ".pdf";
        String file2 = UtilsClass.Random(3) + ".pdf";
        list1.add(file1);
        list1.add(file2);

        switch (data2) {
            case "CHARACTER":
                tempMap.put(data1,"CH" + UtilsClass.Random(12));break;
            case "NUMBER":
                if(data3.isEmpty()) tempMap.put(data1,500000);
                else tempMap.put(data1,0);
                break;
            case "TIME":
                tempMap.put(data1,sdTimeNow);break;
            case "文件列表":
                tempMap.put(data1,list1);break;
            case "DATE":
                tempMap.put(data1,sdTimeNow2);break;
            case "文件":
                tempMap.put(data1,file1);break;
            case "TEXT":
                tempMap.put(data1,"text" + UtilsClass.Random(12));break;
            case "DECIMAL":
                if(data3.isEmpty()) tempMap.put(data1,500000);
                else tempMap.put(data1,0);
                break;
            case "文本":
                tempMap.put(data1,"文本" + UtilsClass.Random(12));break;
        }
    }

    public String constructDataCodeString(String data1,String data2,String data3){
        String tempData = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");
        String sdTimeNow = sdf.format((new Date()).getTime()); // 时间戳转换日期
        String sdTimeNow2 = sdf2.format((new Date()).getTime()); // 时间戳转换日期
        List<String> list1 = new ArrayList<>();
        String file1 = UtilsClass.Random(3) + ".pdf";
        String file2 = UtilsClass.Random(3) + ".pdf";
        list1.add(file1);
        list1.add(file2);

        switch (data2) {
            case "CHARACTER":
                tempData = "\"" + data1 + "\"," + "\"CH" + UtilsClass.Random(12) + "\"";break;
            case "NUMBER":
                if(data3.isEmpty()) tempData = "\"" + data1 + "\"," + 500000;
                else tempData = "\"" + data1 + "\"," + 0;
                break;
            case "TIME":
                tempData = "\"" + data1 + "\"," + "\"" + sdTimeNow + "\"";break;
            case "文件列表":
                tempData = "\"" + data1 + "\"," + list1.toString();break;
            case "DATE":
                tempData = "\"" + data1 + "\"," + "\"" + sdTimeNow2 + "\"";break;
            case "文件":
                tempData = "\"" + data1 + "\"," + "\"" + file1 + "\"";break;
            case "TEXT":
                tempData = "\"" + data1 + "\"," + "\"text" + UtilsClass.Random(10) + "\"";break;
            case "DECIMAL":
                tempData = "\"" + data1 + "\"," + 1000000;break;
            case "文本":
                tempData = "\"" + data1 + "\"," + "\"文本" + UtilsClass.Random(10) + "\"";break;
            default:
                tempData = "\"" + data1 + "\"," + "无数据类型或未覆盖数据类型";break;
        }
        return tempData;
    }

    //读取excel
    public static Workbook readExcel(String filePath) throws  Exception{
        Workbook wb = null;
        if(filePath==null){
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if(".xls".equals(extString)){
                return wb = new HSSFWorkbook(is);
            }else if(".xlsx".equals(extString)){
                return wb = new XSSFWorkbook(is);
            }else{
                return wb = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wb;
    }
    public static Object getCellFormatValue(Cell cell){
        Object cellValue = null;
        if(cell!=null){
            //判断cell类型
            switch(cell.getCellType()){
                case Cell.CELL_TYPE_NUMERIC:{
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA:{
                    //判断cell是否为日期格式
                    if(DateUtil.isCellDateFormatted(cell)){
                        //转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    }else{
                        //数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:{
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        }else{
            cellValue = "";
        }
        return cellValue;
    }
}
