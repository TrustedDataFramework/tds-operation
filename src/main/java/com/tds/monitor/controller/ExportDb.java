package com.tds.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.model.Transaction;
import com.tds.monitor.service.TransactionService;
import com.tds.monitor.utils.ApiResult.APIResult;
import com.tds.monitor.utils.HttpRequestUtil;
import com.tds.monitor.utils.MapCacheUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.util.TimeZone.getTimeZone;

@RestController
public class ExportDb {

    @Autowired
    TransactionService transactionService;

//    @ResponseBody
//    @RequestMapping("/hello")
//    public String hello(Model model) {
//        model.addAttribute("name", "thymeleaf");
//        return "test-push";
//    }

    @GetMapping("/export/txrecordByPublicKey")
    public void export(HttpServletResponse response,@RequestParam(value = "json") String json){
        try {
            JSONArray list = new JSONArray();
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            String url_node = mapCacheUtil.getCacheItem("bindNode").toString();
            if (HttpRequestUtil.sendPostWb(String.format("http://%s/rpc/operations", url_node), json)!=""){
                list = JSON.parseObject(HttpRequestUtil.sendPostWb(String.format("http://%s/rpc/operations", url_node), json)).getJSONArray("data");
            }
            //创建excel文件
            HSSFWorkbook wb = new HSSFWorkbook();
            //创建sheet页
            HSSFSheet sheet = wb.createSheet("事务详情");
            //创建标题行
            HSSFRow titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("事务版本");
            titleRow.createCell(1).setCellValue("事务的类型");
            titleRow.createCell(2).setCellValue("事务构造时间");
            titleRow.createCell(3).setCellValue("事务的序号");
            titleRow.createCell(4).setCellValue("事务发送者");
            titleRow.createCell(5).setCellValue("手续费价格");
            titleRow.createCell(6).setCellValue("转账金额");
            titleRow.createCell(7).setCellValue("事务的载荷");
            titleRow.createCell(8).setCellValue("事务接收者");
            titleRow.createCell(9).setCellValue("事务的签名");
            titleRow.createCell(10).setCellValue("事务的哈希值");
            titleRow.createCell(11).setCellValue("事务的大小,单位是字节");
            titleRow.createCell(12).setCellValue("确认数,-1表示在内存池中");
            sheet.setDefaultColumnWidth(10);
            sheet.setColumnWidth(0, 16*256);
            sheet.setColumnWidth(1, 12*256);
            sheet.setColumnWidth(2, 24*256);
            sheet.setColumnWidth(3, 10*256);
            sheet.setColumnWidth(4, 80*256);
            sheet.setColumnWidth(5, 16*256);
            sheet.setColumnWidth(6, 16*256);
            sheet.setColumnWidth(7, 64*256);
            sheet.setColumnWidth(8, 64*256);
            sheet.setColumnWidth(9, 64*256);
            sheet.setColumnWidth(10, 64*256);
            sheet.setColumnWidth(11, 20*256);
            sheet.setColumnWidth(12, 20*256);
            if(list != null) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = (JSONObject) list.get(i);
                    HSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
                    dataRow.createCell(0).setCellValue(jsonObject.get("version").toString());
                    String type = "";
                    if (jsonObject.get("type").toString() == "0") {
                        type = "coin base";
                    } else if (jsonObject.get("type").toString() == "1") {
                        type = "转账";
                    } else if (jsonObject.get("type").toString() == "2") {
                        type = "合约部署";
                    } else if (jsonObject.get("type").toString() == "3") {
                        type = "合约调用";
                    } else {
                        type = "";
                    }
                    dataRow.createCell(1).setCellValue(type);
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                Date date = formatter.parse(jsonObject.get("createdAt").toString());
//                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String sDate=sdf.format(date);
                    dataRow.createCell(2).setCellValue(jsonObject.get("createdAt").toString());
                    dataRow.createCell(3).setCellValue(jsonObject.get("nonce").toString());
                    dataRow.createCell(4).setCellValue(jsonObject.get("from").toString());
                    dataRow.createCell(5).setCellValue(jsonObject.get("gasPrice").toString());
                    dataRow.createCell(6).setCellValue(jsonObject.get("amount").toString());
                    dataRow.createCell(7).setCellValue(jsonObject.get("payload").toString());
                    dataRow.createCell(8).setCellValue(jsonObject.get("to").toString());
                    dataRow.createCell(9).setCellValue(jsonObject.get("signature").toString());
                    dataRow.createCell(10).setCellValue(jsonObject.get("hash").toString());
                    dataRow.createCell(11).setCellValue(jsonObject.get("size").toString());
                    dataRow.createCell(12).setCellValue(jsonObject.get("fee").toString());
                }
            }
            // 设置下载时客户端Excel的名称
            String filename =new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-publicKey.xls";
            //设置下载的文件
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename=" + filename);
            OutputStream ouputStream = response.getOutputStream();//打开流
            wb.write(ouputStream); //在excel内写入流
            ouputStream.flush();// 刷新流
            ouputStream.close();// 关闭流
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
