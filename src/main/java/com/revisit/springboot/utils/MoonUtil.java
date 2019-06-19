/**
 * Copyright (C), 2015-2018, 美果科技有限公司
 * FileName: MoonUtils
 * Author:   Revisit-Moon
 * Date:     2018/10/26 11:43 AM
 * Description: MoonUtils
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2018/10/26 11:43 AM        1.0              描述
 */

package com.revisit.springboot.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.utils.EncodingUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 〈MoonUtils〉
 *
 * @author Revisit-Moon
 * @create 2018/10/26
 * @since 1.0.0
 */

public class MoonUtil {

    private final static Logger logger = LoggerFactory.getLogger(MoonUtil.class);

    public static void conversionFolderAllFileFormats(String conversionSrcFolder,String readCoding, String conversionCoding, String fileFormat, boolean OverwriteSourceFile) {
        logger.info("当前遍历的文件夹: " + conversionSrcFolder);
        File srcFolder = new File(conversionSrcFolder);
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        // 获取该目录下所有的文件或者文件夹的File数组
        File[] fileArray = srcFolder.listFiles();
        // 遍历该File数组，得到每一个File对象
        for (File file : fileArray) {
            // 继续判断是否以.java结尾,不是的话继续调用getAllFilePaths()方法
            if (file.isDirectory()) {
                conversionFolderAllFileFormats(file.getAbsolutePath(),readCoding,conversionCoding,fileFormat,OverwriteSourceFile);
            } else {
                if (file.getName().endsWith(fileFormat)) {
                    // 以传入的编码格式,读取文件
                    try {
                        //获取该文件的编码
                        String code = EncodingUtil.detect(file.getAbsoluteFile());
                        //如果获取到
                        boolean flag = false;
                        if (code != null) {
                            logger.info("当前文件: ["+file.getName()+"] 编码: [" + code+"]");
                            //判断是否与传入的读取字节编码一致,避免读入文件字节时乱码
                            if (!code.equals(readCoding)) {
                                //如果与读取字节编码一致,则判断是否与输出字节编码一致
                                if (!code.equals(conversionCoding)) {
                                    flag =true;
                                    //如果与输出字节编码不一致,则认为需要新写文件.转读取字节编码转换,避免读入文件字节乱码
                                    logger.info("当前文件: ["+file.getName()+"] ,读取字节编码 ["+readCoding+"] 与文件实际字节编码 ["+code+"] 不一致,而且文件实际字节编码与输出字节编码["+conversionCoding+"]不一致,应该为需要转换的文件,开始转换读取字节编码....转换后的读取字节编码: " + code);
                                }else{
                                    //如一致则跳过该文件不重新写入
                                    logger.info("当前文件: ["+file.getName()+"] ,读取字节编码 ["+readCoding+"] 与文件实际字节编码 ["+code+"] 不一致,但文件实际字节编码与输出字节编码["+conversionCoding+"]一致,应该为不需要转换的文件,开始重新遍历下一个文件");
                                    continue;
                                }
                            }else{
                                //如果与读取字节编码一致,则判断是否与输出字节编码一致
                                if (!code.equals(conversionCoding)) {
                                    flag =true;
                                    //如果与输出字节编码不一致,则认为需要新写文件.转读取字节编码转换,避免读入文件字节乱码
                                    logger.info("当前文件:["+file.getName()+"] ,读取字节编码 ["+readCoding+"] 与文件实际字节编码["+code+"] 一致,但文件实际字节编码与输出字节编码["+conversionCoding+"] 不一致,应该为需要转换的文件,开始转换");
                                }else{
                                    //如一致则跳过该文件不重新写入
                                    logger.info("当前文件:["+file.getName()+"] ,读取字节编码 ["+readCoding+"] 与文件实际字节编码["+code+"] 一致,而且文件实际字节编码与输出字节编码["+conversionCoding+"] 一致,应该为不需要转换的文件,开始重新遍历下一个文件");
                                    continue;
                                }
                            }
                        }
                        fis = new FileInputStream(file);
                        if (!flag) {
                            isr = new InputStreamReader(fis, readCoding);
                        }else{
                            isr = new InputStreamReader(fis, code);
                        }
                        br = new BufferedReader(isr);
                        String str = null;
                        // 创建StringBuffer字符串缓存区
                        StringBuffer sb = new StringBuffer();
                        // 通过readLine()方法遍历读取文件
                        while ((str = br.readLine()) != null) {
                            // 使用readLine()方法无法进行换行,需要手动在原本输出的字符串后面加"\n"或"\r"
                            str += "\n";
                            sb.append(str);
                        }
                        String gbkStr = sb.toString();
                        // 以传入的转换编码格式写入文件,file.getAbsolutePath()即该文件的绝对路径,false代表不追加直接覆盖,true代表追加文件
                        fos = new FileOutputStream(file.getAbsolutePath(), !OverwriteSourceFile);
                        osw = new OutputStreamWriter(fos, conversionCoding);
                        osw.write(gbkStr);
                        osw.flush();
                        logger.info("当前文件: ["+file.getName()+"],转换完成,当前编码: ["+EncodingUtil.detect(file.getAbsoluteFile())+"]");
                    } catch (IOException e) {
                        logger.info("读写错误: " +e.getCause());
                        e.printStackTrace();
                    } finally {
                        try {
                            if (osw!=null) {
                                osw.close();
                            }
                            if (fos!=null) {
                                fos.close();
                            }
                            if (br!=null) {
                                br.close();
                            }
                            if (isr!=null) {
                                isr.close();
                            }
                            if (fis!=null) {
                                fis.close();
                            }
                        } catch (Exception e) {
                            logger.info("关闭流错误: " +e.getCause());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 〈得到当前项目的绝对路径〉
     *
     * @param
     * @return
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static String getAbsolutePath(){
        File file = new File("");
        return file.getAbsolutePath();
    }

    /**
     * 〈检查对象所有的属性是否存在空值〉
     *
     * @param
     * @return
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static boolean checkObjAllFieldIsNull(Object obj) {
        try {
            for (Field f : obj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (isNull(f.get(obj))) {
                    return false;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    /**
     * 〈得到对象所有属性名〉
     *
     * @param obj
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static Map<String,Object> outAllObjField(Object obj) {
        Map<String, Object> field = new HashMap<>();
        String type = "";
        try {
            for (Field f : obj.getClass().getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    if (isNull(f.get(obj))) {
                        if (StringUtils.indexOf(StringUtils.substring(f.getGenericType().toString(), StringUtils.lastIndexOf(f.getGenericType().toString(), ".") + 1), ">") != -1) {
                            type = f.getGenericType().toString();
                        } else {
                            type = StringUtils.substring(f.getGenericType().toString(), StringUtils.lastIndexOf(f.getGenericType().toString(), ".") + 1);
                        }
                        field.put("属性: " + f.getName(),"null或空, 类型: " + type);
                    } else {
                        if (StringUtils.indexOf(StringUtils.substring(f.getGenericType().toString(), StringUtils.lastIndexOf(f.getGenericType().toString(), ".") + 1), ">") != -1) {
                            type = f.getGenericType().toString();
                        } else {
                            type = StringUtils.substring(f.getGenericType().toString(), StringUtils.lastIndexOf(f.getGenericType().toString(), ".") + 1);
                        }
                        field.put("属性: " + f.getName(),(f.get(obj).toString() + " 类型: " + type));
                    }
                } catch (Exception e) {
                    field.put(f.getName(), " = " + e.getCause());
                }
            }
        }catch (Exception e){
            field.put(obj+"",e.getCause());
            return field;
        }
        return field;
    }


    /**
     * 〈返回Date转yyyy-MM-dd HH:mm:ss格式时间字符串〉
     *
     * @param data
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static String dataToyMdHms(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(data.getTime()));
    }

    public static String dataToyMdHmsNotSymbol(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date(data.getTime()));
    }

    /**
     * 〈返回yyyy-MM-dd HH:mm:ss格式时间字符串转Date〉
     *
     * @param yMdHms
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static Date yMdHmsToData(String yMdHms) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(yMdHms);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 〈检查两个对象的值是否完全相同〉
     *
     * @param objOne,objTow,...excludedValue
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static boolean checkSameObject(Object objOne, Object objTow,String ...excludedValue)throws Exception {
        Class<?> aClass = objOne.getClass();
        Class<?> bClass = objTow.getClass();
        if (aClass!=null&&bClass!=null){
            Field[] fieldOne = aClass.getDeclaredFields();
            Field[] fieldTow = bClass.getDeclaredFields();
            if (fieldOne.length==fieldTow.length){
                for (int i = 0; i < fieldOne.length ; i++) {
                    Field aField = fieldOne[i];
                    Field bField = fieldTow[i];
                    aField.setAccessible(true);
                    bField.setAccessible(true);
                    Object aVal = aField.get(objOne);
                    Object bVal = bField.get(objTow);
                    if (aVal != null && bVal != null) {
                        if (!excludedValue.toString().contains(aVal.getClass().getName())&& !excludedValue.toString().contains(bVal.getClass().getName())) {
                            if (aVal != bVal) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 〈返回比较时间后的大的时间〉
     *
     * @param
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static String contrastTime(String time,String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (sdf.parse(time).getTime()>sdf.parse(time2).getTime()){
                return time;
            }else {
                return time2;
            }
        } catch (ParseException e) {
            return "时间转换异常: "+e.getCause();
        }
    }

    public static List<Date> getTimeRangeDate(String timeRange){
        List<Date> result = new LinkedList<>();
        try {
            if (StringUtils.isNotBlank(timeRange)) {
                if (timeRange.contains("~")) {
                    String[] timeRangeStr = StringUtils.split(timeRange, "~");
                    result.add(yMdHmsToData(timeRangeStr[0] + " 00:00:00"));
                    result.add(yMdHmsToData(timeRangeStr[1] + " 23:59:59"));
                    return result;
                }else{
                    result.add(yMdHmsToData(timeRange + " 00:00:00"));
                    result.add(null);
                    return result;
                }
            }else{
                result = new LinkedList<>();
                result.add(null);
                result.add(null);
                return result;
            }
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 〈返回比较时间后的大的时间〉
     *
     * @param
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static Date contrastTime(Date time,Date time2) {
        long timeLong = time.getTime()/1000;
        long timeLong2 = time2.getTime()/1000;
        if (timeLong>timeLong2){
            return time;
        }else {
            return time2;
        }
    }

    /**
     * 〈返回当前日期yyyy-MM-dd HH:mm:ss格式时间字符串〉
     *
     * @param
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static String getNowTimeToyMdHms() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 〈返回当前日期yyyy-MM-dd HH:mm:ss格式时间字符串〉
     *
     * @param
     * @return String
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static Date getNowTimeSecondPrecision() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 〈发送参数为JSON类型的POST请求〉
     *
     * @param
     * @return
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static JSONObject httpClientPostJsonObject(String url, JSONObject json){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //配置超时时间
        RequestConfig requestConfig = RequestConfig.custom().
                setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000).setRedirectsEnabled(true).build();
        HttpPost httpPost = new HttpPost(url);
        //设置超时时间
        httpPost.addHeader("Content-Type","application/json");
        httpPost.setConfig(requestConfig);
        logger.info("请求地址: "+ url + " 传递参数: "+json.toJSONString());
        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
        for (JSONObject.Entry e:json.entrySet()) {
            list.add(new BasicNameValuePair(e.getKey()+"" ,e.getValue()+""));  //请求参数
        }
        JSONObject result = new JSONObject();
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,"UTF-8");
            //设置post请求参数
            httpPost.setEntity(entity);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if(httpResponse != null){
                result = postRestToJson(httpResponse.getEntity().getContent());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (httpClient != null) {
                    httpClient.close(); //释放资源
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 〈发送参数为字符串类型的POST请求〉
     *
     * @param
     * @return
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static JSONObject httpClientPostStr(String url,String data){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //配置超时时间
        RequestConfig requestConfig = RequestConfig.custom().
                setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000).setRedirectsEnabled(true).build();
        HttpPost httpPost = new HttpPost(url);
        //设置超时时间
        httpPost.setConfig(requestConfig);;
        JSONObject result = new JSONObject();
        try {
            //设置post求情参数
            httpPost.addHeader("Content-Type","application/json");
            httpPost.setEntity(new StringEntity(data,Charset.forName("UTF-8")));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            logger.info("请求地址: "+ url + " 传递参数: "+data);
            if(httpResponse != null){
                result = postRestToJson(httpResponse.getEntity().getContent());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (httpClient != null) {
                    httpClient.close(); //释放资源
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 〈发送get请求〉
     *
     * @param
     * @return
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static JSONObject httpClientGet(String url){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //配置超时时间
        RequestConfig requestConfig = RequestConfig.custom().
                setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000).setRedirectsEnabled(true).build();
        HttpGet httpGet = new HttpGet(url);
        logger.info("请求地址: "+ url );
        // HttpPost httpPost = new HttpPost(url);
        //设置超时时间
        httpGet.setConfig(requestConfig);
        JSONObject result = new JSONObject();
        try {
            //设置post求情参数
            // httpPost.setEntity(new StringEntity(data,Charset.forName("UTF-8")));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if(httpResponse != null){
                result = postRestToJson(httpResponse.getEntity().getContent());
                logger.info("结果: "+result.toJSONString());
            }
            return result;
        } catch (Exception e) {
            logger.info("执行异常: "+ e);
            e.printStackTrace();
        }finally {
            try {
                if (httpClient != null) {
                    httpClient.close(); //释放资源
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("异常: "+ e.getCause());
            }
        }
        return result;
    }

    /**
     * 〈转换xml为Map〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 4:38 PM
     */
    public static Map<String,String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            Document doc = DocumentHelper.parseText(xml);//将xml转为dom对象
            Element root = doc.getRootElement();//获取根节点
            List<Element> elements = root.elements();
            for (Object obj : elements) {  //遍历子元素
                root = (Element) obj;
                map.put(root.getName(), root.getTextTrim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 〈转换微信返回的xmlMap为字符串〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 4:38 PM
     */
    public static String mapToXml(Map<String, String> xmlMap) {
        StringBuffer xml = new StringBuffer();
        xml.append("<xml>");
        for (Map.Entry entry: xmlMap.entrySet()){
            if (entry.getKey().equals("CreateTime")){
                xml.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
            }else {
                xml.append("<" + entry.getKey() + "><![CDATA[" + entry.getValue() + "]]></" + entry.getKey() + ">");
            }
        }
        xml.append("</xml>");
        return xml.toString();
    }

    /**
     * 〈微信公众号的收发对象调转〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 4:38 PM
     */
    public static Map<String,String> ChangeSendAndReceive(Map<String, String> xmlMap) {
        if (StringUtils.isNotBlank(xmlMap.get("ToUserName"))&&StringUtils.isNotBlank(xmlMap.get("FromUserName"))){
            String temp = xmlMap.get("FromUserName");
            xmlMap.put("FromUserName",xmlMap.get("ToUserName"));
            xmlMap.put("ToUserName",temp);
        }
        return xmlMap;
    }

    /**
     * 〈JSON类型POST请求后的返回结果转为JSON对象〉
     *
     * @param
     * @return JSONObject
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static JSONObject postRestToJson(InputStream postRest){
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        JSONObject resultJson = new JSONObject();
        try {
            while ((length = postRest.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            resultJson = JSONObject.parseObject(result.toString("UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultJson;
    }



    /**
     * 〈返回Date在多久之后转yyyy-MM-dd HH:mm:ss格式时间字符串〉
     *
     * @param
     * @return Date
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static Date yMdHmsToAfterTime(String NowTimeStr,Long year,Long month,Long day,Long hour,Long minute,Long second){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        Long entryTime = 0L;
        try {
            entryTime = sdf.parse(NowTimeStr).getTime() / 1000;
            if (year!=null&&year!=0){
                entryTime = entryTime+(year*31536000);
            }
            if (month!=null&&month!=0){
                calendar.setTime(new Time(entryTime));
                entryTime = entryTime+(month*(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)*86400));
            }
            if (day!=null&&day!=0){
                entryTime = entryTime+(day*86400);
            }
            if (hour!=null&&hour!=0){
                entryTime = entryTime+(hour*3600);
            }
            if (minute!=null&&minute!=0){
                entryTime = entryTime+(minute*60);
            }
            if (second!=null&&second!=0){
                entryTime = entryTime+second;
            }
            return new Date(entryTime*1000L);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 〈获取用户真实IP地址〉
     *
     * @param request
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/25 3:44 PM
     */
    public static String getRealIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if( ip.indexOf(",")!=-1 ){
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        logger.info("获取到的IP地址:" + ip);
        return ip;
    }

    /**
     * 〈判断字符串是否有Emoji表情〉
     *
     * @param content
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/25 5:39 PM
     */
    public static String fixEmoji(String content){

        Pattern pattern = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]");
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            logger.info(content+" 含有emoji表情,开始去除");
            content = matcher.replaceAll("");
            logger.info("去除结果: "+content);
            return content;
        }
        return content;
    }

    /**
     * 〈限制字符串字节数长度〉
     *
     * @param content
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/25 5:39 PM
     */
    public static String limitStringLength (String content,int wantCharLength){
        try {
            byte[] bytes = content.getBytes();
            if (bytes.length>=wantCharLength) {
                content = content.substring(0, content.length() - 3);
                content = limitStringLength(content, wantCharLength);
            }
        }catch (Exception e){
            logger.info("字符串处理失败");
        }
        return content;
    }

    /**
     * 〈返回Date在多久之后转yyyy-MM-dd HH:mm:ss格式时间字符串〉
     *
     * @param
     * @return Date
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static Date yMdHmsToAfterTime(Date date,Object howLong,String timeAbbreviation){
        timeAbbreviation = getTimeAbbreviationFixString(timeAbbreviation);
        ZoneId zoneId = ZoneId.systemDefault();
        if(StringUtils.isBlank(timeAbbreviation)){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        Long time = new BigDecimal(howLong.toString()).longValue();
        LocalDateTime localDateTime = date.toInstant().atZone(zoneId).toLocalDateTime();
        // Long entryTime = date.getTime() / 1000;
        try{
            switch (timeAbbreviation){
                case "year":{
                    localDateTime = localDateTime.plusYears(time);
                    break;
                }
                case "month":{
                    localDateTime = localDateTime.plusMonths(time);
                    break;
                }
                case "day":{
                    localDateTime = localDateTime.plusDays(time);
                    break;
                }
                case "hour":{
                    localDateTime = localDateTime.plusHours(time);
                    break;
                }
                case "minute":{
                    localDateTime = localDateTime.plusMinutes(time);
                    break;
                }
                case "second":{
                    localDateTime = localDateTime.plusSeconds(time);
                    break;
                }
                default:{
                    break;
                }
            }
            return Date.from(localDateTime.atZone(zoneId).toInstant());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String getTimeAbbreviationFixString(String timeAbbreviation){
        if ((timeAbbreviation.equals("年")
                ||timeAbbreviation.equals("y"))){
            return "year";
        }
        if(timeAbbreviation.equals("月")
                ||timeAbbreviation.equals("M")){
            return "month";
        }
        if (timeAbbreviation.equals("日")
                ||timeAbbreviation.equals("d")
                ||timeAbbreviation.equals("天")){
            return "day";
        }
        if (timeAbbreviation.equals("时")
                ||timeAbbreviation.equals("H")
                ||timeAbbreviation.equals("小时")){
            return "hour";
        }
        if (timeAbbreviation.equals("分")
                ||timeAbbreviation.equals("m")){
            return "minute";
        }
        if (timeAbbreviation.equals("秒")
                ||timeAbbreviation.equals("m")){
            return "second";
        }
        return null;
    }

    public static List<String> getStringListByComma(String str){
        if (StringUtils.isBlank(str)){
            return null;
        }
        List<String> list = new ArrayList<>();

        if (StringUtils.contains(str,",")){
            for (String s : StringUtils.split(str, ",")) {
                list.add(s);
            }
        }else{
            list.add(str);
        }
        return list;
    }

    public static Set<String> getStringSetByComma(String str){
        if (StringUtils.isBlank(str)){
            return null;
        }
        Set<String> list = new HashSet<>();

        if (StringUtils.contains(str,",")){
            for (String s : StringUtils.split(str, ",")) {
                list.add(s);
            }
        }else{
            list.add(str);
        }
        return list;
    }

    /**
     * 〈前端导出Excel表格〉
     *
     * @param
     * @return Date
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static int exportToExcelUtil(OutputStream outputStream, String title, String[] rowName , List<Object> data) {
        //文档对象
        HSSFWorkbook wb = new HSSFWorkbook();
        int rowNum = 0;
        Sheet sheet = wb.createSheet(title);
        Row row0 = sheet.createRow(rowNum++);
        for (int i=0;i<rowName.length;i++){
            row0.createCell(i).setCellValue(rowName[i]);
        }
        if (data != null && data.size() > 0) {
            String s = JSONObject.toJSONString(data);
            JSONArray array = JSONArray.parseArray(s);
            for (int i = 0;i<array.size();i++){
                Row row = sheet.createRow(rowNum++);
                String[] split = StringUtils.split(array.get(i).toString(), "[,||\"||]||[]");
                for (int j=0;j<split.length;j++) {
                    try {
                        if (j==split.length-1){
                            row.createCell(j).setCellValue(dataToyMdHms(new Date(Long.valueOf(split[j]))));
                        }else {
                            row.createCell(j).setCellValue(URLDecoder.decode(split[j], "utf-8"));
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            wb.write(outputStream);
            logger.info("写入excel表成功,一共写入了"+(rowNum - 1)+"条数据");
            outputStream.close();
        } catch (IOException e) {
            logger.error("流关闭异常！", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("流关闭异常！", e);
                }
            }
        }
        return rowNum - 1;
    }

    /**
     * 〈下划线命名转驼峰命名〉
     *
     * @param underlineName
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 2:46 PM
     */
    public static String underlineToHump(String underlineName){
        StringBuilder result=new StringBuilder();
        String a[]=underlineName.split("_");
        for(String s:a){
            if (!underlineName.contains("_")) {
                result.append(s);
                continue;
            }
            if(result.length()==0){
                result.append(s.toLowerCase());
            }else{
                result.append(s.substring(0, 1).toUpperCase());
                result.append(s.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 〈驼峰命名转下划线命名〉
     *
     * @param humpName
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 2:46 PM
     */
    public static String humpToUnderline(String humpName){
        StringBuilder sb = new StringBuilder(humpName);
        int temp=0;//定位
        if (!humpName.contains("_")) {
            for(int i=0;i<humpName.length();i++){
                if(Character.isUpperCase(humpName.charAt(i))){
                    sb.insert(i+temp, "_");
                    temp+=1;
                }
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 〈判断店铺是否在营业时间〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/28 4:23 PM
     */
    public static boolean canBuy(String shopRunTime){
        Integer startDay = judgmentRuntime(StringUtils.substring(shopRunTime, 0, 2));
        String mark = StringUtils.substring(shopRunTime, 2,3);
        Integer endDay = judgmentRuntime(StringUtils.substring(shopRunTime, 3, 5));
        String[] split = StringUtils.split(StringUtils.substring(shopRunTime, 6), "-");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK)-1;
        String dayOfHour = new SimpleDateFormat("HH:mm").format(new Date());
        String[] hourMinute =  StringUtils.split(dayOfHour, ":");
        logger.info("购买时间:星期 "+dayOfWeek+" "+dayOfHour+"分");
        boolean isContinuous = false;
        if (mark.equals("到")||mark.equals("至")){
            isContinuous = true;
        }

        if (mark.equals("和")||mark.equals("与")){
            isContinuous = false;
        }
        // boolean isBuyTime = false;
        boolean start = false;
        boolean end = false;
        for (int i = 0; i <split.length ; i++) {
            String[] time = StringUtils.split(split[i], ":");
            int buyTime = Integer.parseInt(hourMinute[0] + hourMinute[1]);
            int runTime = Integer.parseInt(time[0]+time[1]);
            if (i%2==0) {
                if (buyTime>=runTime){
                    start = true;
                }else {
                    start = false;
                }
            }else{
                if (buyTime<=runTime){
                    end = true;
                }else {
                    end = false;
                }
            }
        }


        boolean canBuyDay[] = new boolean[7];
        int can = endDay - 1;
        if (isContinuous) {
            if (startDay > endDay) {
                for (int i = startDay - 1; i < canBuyDay.length; i++) {
                    canBuyDay[i] = true;
                }
                for (int i = 0; i <= can; i++) {
                    canBuyDay[i] = true;
                }
            } else {
                for (int i = startDay - 1; i < canBuyDay.length; i++) {
                    canBuyDay[i] = true;
                }
            }
        }

        if(!isContinuous){
            canBuyDay[startDay-1]=true;
            canBuyDay[endDay-1]=true;
        }

        if (canBuyDay[dayOfWeek]&&(start&&end)){
            return true;
        }else{
            return false;

        }
    }

    public static String generateUUIDToBase64(String uuid){
        // StringBuilder resultBuilder = new StringBuilder();
        String[] components = uuid.split("-");
        if (components.length != 5) {
            components = new String[5];
            uuid = uuid.replace("-", "");
            components[0] = uuid.substring(0, 8);
            components[1] = uuid.substring(8, 12);
            components[2] = uuid.substring(12, 16);
            components[3] = uuid.substring(16, 20);
            components[4] = uuid.substring(20, 32);
        }

        for (int i=0; i<5; i++)
            components[i] = "0x"+components[i];

        long msb = Long.decode(components[0]).longValue();
        msb <<= 16;
        msb |= Long.decode(components[1]).longValue();
        msb <<= 16;
        msb |= Long.decode(components[2]).longValue();

        long lsb = Long.decode(components[3]).longValue();
        lsb <<= 48;
        lsb |= Long.decode(components[4]).longValue();

        byte[] b = new byte[16];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (msb >>> (8 * (7-i)) & 0xff);
            b[i+8] = (byte) (lsb >>> (8 * (7-i)) & 0xff);
        }
        return Base64.getEncoder().withoutPadding().encodeToString(b);
    }

    public static String decompressBase64UUID(String uuidBase64) {
        byte[] b = Base64.getDecoder().decode(uuidBase64);

        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++) {
            msb = (msb << 8) | (b[i] & 0xff);
            lsb = (lsb << 8) | (b[i+8] & 0xff);
        }

        return new UUID(msb, lsb).toString().replaceAll("-","");
    }

    private static Integer judgmentRuntime(String day){
        switch (day){
            case "周一":{
                return 1;
            }
            case "周二":{
                return 2;
            }
            case "周三":{
                return 3;
            }
            case "周四":{
                return 4;
            }
            case "周五":{
                return 5;
            }
            case "周六":{
                return 6;
            }
            case "周日":{
                return 7;
            }
            default:{
                return 0;
            }
        }
    }

    /**
     * 〈中文转Unicode〉
     *
     * @param
     * @return Date
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static String gbEncoding(final String gbString) {   //gbString = "测试"
        char[] utfBytes = gbString.toCharArray();   //utfBytes = [测, 试]
        String unicodeBytes = "";
        for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {
            String hexB = Integer.toHexString(utfBytes[byteIndex]);   //转换为16进制整型字符串
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        // System.out.println("unicodeBytes is: " + unicodeBytes);
        return unicodeBytes;
    }

    /**
     * 〈Unicode转中文〉
     *
     * @param
     * @return Date
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }

    /**
     * 〈得到两个数组不同的元素〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/8 9:40 AM
     */
    public static String getCollectionDifferent(Collection collmax, Collection collmin) {
        long startTime = System.currentTimeMillis();
        //使用LinkeList防止差异过大时,元素拷贝
        Collection csReturn = new LinkedList();
        Collection max = collmax;
        Collection min = collmin;
        //先比较大小,这样会减少后续map的if判断次数
        if(collmax.size()<collmin.size())
        {
            max = collmin;
            min = collmax;
        }
        //直接指定大小,防止再散列
        Map<Object,Integer> map = new HashMap<Object,Integer>(max.size());
        for (Object object : max) {
            map.put(object, 1);
        }
        for (Object object : min) {
            if(map.get(object)==null)
            {
                csReturn.add(object);
            }else{
                map.put(object, 2);
            }
        }
        for (Map.Entry<Object, Integer> entry : map.entrySet()) {
            if(entry.getValue()==1)
            {
                csReturn.add(entry.getKey());
            }
        }

        long endTime=System.currentTimeMillis();

        float excTime=(float)(endTime-startTime)/1000;
        logger.info("getCollectionDifferent work done time : "+excTime+"秒");
        String different = getCollectionToString(csReturn);
        return different;
    }
    /**
     * 〈得到所有元素逗号分割〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/8 9:40 AM
     */
    public static String getCollectionToString(Collection csReturn){
        StringBuffer result = new StringBuffer();
        Object[] objects = csReturn.toArray();
        int i = 0;
        for (Object obj:objects){
            if (i!=objects.length-1) {
                result.append(obj.toString()+",");
            }else {
                result.append(obj.toString());
            }
        }
        return result+"";
    }
    /**
     * 〈删除重复元素〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/8 9:41 AM
     */
    public static List<Object> removeDuplicate(List<Object> list) {
        LinkedHashSet<Object> set = new LinkedHashSet<Object>(list.size());
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    /**
     * 〈获取字符串List集合中重复最多的值〉
     *
     * @param strList
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/6 7:04 PM
     */
    public static String getMoreRepeatValue(List<String> strList){
        String result = "";
        HashMap<String,Integer> map = new HashMap<>();

        //利用map数组不能添加重复数据
        //当不存在时添加1 重复存在值+1
        for (String str : strList) {
            if (map.containsKey(str)){
                int temp = map.get(str);
                map.put(str,temp+1);
            }else{
                map.put(str,1);
            }
        }
        Collection<Integer> count = map.values();
        //出现最多的次数
        int cs = Collections.max(count);

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            // logger.info("当前: "+entry.getKey()+"-"+entry.getValue());
            if (cs == entry.getValue()){
                result = entry.getKey();
            }
        }
        return result;
    }

    /**
     * 〈升序排列〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/12/11 1:50 PM
     */
    public static<K,V> String ascendingArray(Map<K,V> waitSortMap,
                                             Integer isLowercaseOrUppercase,
                                             String encodeType,
                                             String keySymbolValue,
                                             String separator,
                                             String fistEncodeSecret,String endEncodeSecret){
        List<Map.Entry<K,V>> itmes = new ArrayList<Map.Entry<K,V>>(waitSortMap.entrySet());
        try {
            Collections.sort(itmes, new Comparator<Map.Entry<K,V>>() {
                @Override
                public int compare(Map.Entry<K,V> o1, Map.Entry<K,V> o2) {
                    // TODO Auto-generated method stub
                    return (o1.getKey().toString().compareTo(o2.getKey().toString()));
                }
            });
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<K, V> item : itmes) {
                if (StringUtils.isNotBlank(item.getKey().toString())) {
                    String key = item.getKey().toString();
                    String val = item.getValue().toString();
                    if (isLowercaseOrUppercase==0){
                        sb.append(key + keySymbolValue + val);
                    }
                    if (isLowercaseOrUppercase==1) {
                        sb.append(key.toLowerCase() + keySymbolValue + val);
                    }
                    if (isLowercaseOrUppercase==2) {
                        sb.append(key.toUpperCase() + keySymbolValue + val);
                    }
                    sb.append(separator);
                }
            }
            logger.info("待解密签名字符串: "+sb.toString());
            logger.info("签名类型: "+ encodeType.toUpperCase());
            if (encodeType.toUpperCase().equals("MD5")){
                fistEncodeSecret = fistEncodeSecret+sb.append(endEncodeSecret).toString();
                String md5 = encodeByMD5(fistEncodeSecret);
                logger.info("签名结果: "+ md5);
                return md5;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 通过经纬度获取距离(单位：米)
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return 距离
     */
    public static double getLineDistance(double lng1,double lat1, double lng2,double lat2
    ) {
        double earthRadius = 6378.137;
        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat2 * Math.PI / 180.0;
        double a = radLat1 - radLat2;
        double b = (lng1* Math.PI / 180.0) - (lng2* Math.PI / 180.0);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * earthRadius;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s/1000;
    }

    /**
     * 〈创建微信订单号〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/16 8:01 PM
     */

    public static String createWeChatOrderNum(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String newDate = sdf.format(new Date());
        String orderNum = newDate + getRandomNumber(3);

        return orderNum;
    }

    /**
     * 获取随机数字字符串
     * @param length 表示生成字符串的长度
     * @return
     */
    public static String getRandomNumber(int length) {
        String base = "0123456789";
        Random random = new Random();
        int baseStrLength = base.length();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(baseStrLength);
            sb.append(base.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 坐标转换，百度地图坐标转换成腾讯地图坐标
     * @param lat  百度坐标纬度
     * @param lon  百度坐标经度
     * @return 返回结果：纬度,经度
     */
    public static double[] bdMapToTxMap(double lat, double lon){
        double tx_lat;
        double tx_lon;
        double x_pi=3.14159265358979324;
        double x = lon - 0.0065, y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        tx_lon = z * Math.cos(theta);
        tx_lat = z * Math.sin(theta);
        return new double[]{tx_lat,tx_lon};
    }

    /**
     * 〈SHA256加密〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/26 5:16 PM
     */

    public static final String encodeBySHA256(String waitEncode){
        MessageDigest messageDigest;
        String encodeStr = "";
        if (StringUtils.isBlank(waitEncode)){
            return null;
        }
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(waitEncode.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return encodeStr;
    }

    /**
     * 〈SHA256加密〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/26 5:16 PM
     */

    public static final String encodeByMD5(String waitEncode){
        MessageDigest messageDigest;
        String encodeStr = "";
        if (StringUtils.isBlank(waitEncode)){
            return null;
        }
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(waitEncode.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return encodeStr;
    }

    /**
     * 〈订单修改状态〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/12/6 6:58 PM
     */
    public static String fixStatus(String nowStatus) {
        Map<String,String> statusMap = new HashMap<>();
        statusMap.put("新建","待付款");
        statusMap.put("已付款","待发货");
        statusMap.put("已发货","待签收");
        statusMap.put("已签收","待评价");
        statusMap.put("已评价","完结");
        statusMap.put("申请退款","退款审核中");
        statusMap.put("同意退款","已退款");
        statusMap.put("确认接单","已接单");
        return statusMap.get(nowStatus);
    }

    /**
     * 〈元分转换保留2位小数〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/11/26 5:16 PM
     */

    public static final Integer floatToInteger(Float waitChangeFloat){
        int pointNow = 0;
        String s = "";
        try {
            s = Float.toString(waitChangeFloat);
        }catch (Exception e){
            return null;
        }
        logger.info("Float类型分单位金额转换为字符串后 "+s);
        if (s.contains(".")) {
            //小数点当前的位置
            pointNow = s.indexOf(".");
        }
        int pointLast = s.length() - (pointNow + 1);
        if (pointLast>2&&pointLast>0){
            s = s.substring(0,pointNow+3);
            s = StringUtils.replace(s,".","");
            logger.info("小数点后的数字大于2,进行转换,转换结果: "+s);
        }
        if (pointLast<2&&pointLast>0){
            s = s+"0";
            s = StringUtils.replace(s,".","");
            logger.info("小数点后的数字小于2,进行转换,转换结果: "+s);
        }
        if (pointLast==2){
            s = StringUtils.replace(s,".","");
            logger.info("小数点后的数字等于2,结果: "+s);
        }
        if (pointNow == 0){
            logger.info("不存在小数点,结果: "+s);
            return Integer.parseInt(s);
        }
        return Integer.parseInt(s);
    }

    /**
     　　* 将byte转为16进制
     　　* @param bytes
     　　* @return
     　　*/
    public static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 〈返回两个时间相差的天数〉
     *
     * @param timeA,timeB
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/6 7:31 PM
     */
    public static Long getDifferenceDay(Date timeA, Date timeB){
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        try {
            if (timeA.getTime() > timeB.getTime()) {
                startDate = timeB.toInstant().atZone(zoneId).toLocalDateTime();
                endDate = timeA.toInstant().atZone(zoneId).toLocalDateTime();
            } else {
                startDate = timeA.toInstant().atZone(zoneId).toLocalDateTime();
                endDate = timeB.toInstant().atZone(zoneId).toLocalDateTime();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return Duration.between(startDate, endDate).toDays();
    }
    // public static boolean tokenCheck(AccessTokenService accessTokenService, UserService userService, String accesstoken){
    //     if (!accessTokenService.valid(accesstoken)||!userService.isSuperAdmin(accesstoken)){
    //         return false;
    //     }
    //     return true;
    // }

    public static BigDecimal mathematical(Object numOne,String symbol,Object numTwo,Integer keepDecimalPlaces){
        BigDecimal decimalOne = new BigDecimal(numOne.toString());
        BigDecimal decimalTwo = new BigDecimal(numTwo.toString());
        BigDecimal result = new BigDecimal(0);
        switch (symbol){
            case "+":{
                result = decimalOne.add(decimalTwo).setScale(keepDecimalPlaces,BigDecimal.ROUND_DOWN);
                break;
            }
            case "-":{
                result = decimalOne.subtract(decimalTwo).setScale(keepDecimalPlaces,BigDecimal.ROUND_DOWN);
                break;
            }
            case "*":{
                result = decimalOne.multiply(decimalTwo).setScale(keepDecimalPlaces,BigDecimal.ROUND_DOWN);
                break;
            }
            case "/":{
                result = decimalOne.divide(decimalTwo,keepDecimalPlaces,BigDecimal.ROUND_DOWN);
                break;
            }
        }
        return result;
    }

    public static boolean isZero(Object numOne){
        BigDecimal decimalOne = new BigDecimal(numOne.toString());
        if (decimalOne.compareTo(new BigDecimal(0))==0){
            return true;
        }
        return false;
    }

    public static void sendConsoleLog(String desc,String msg){
        logger.info(desc+": "+msg);
    }

    /**
     * 〈判断对象是否为空〉
     *
     * @param
     * @return: boolean
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 3:48 PM
     */
    public final static boolean isNull(Object[] objs) {
        if (objs == null || objs.length == 0)
            return true;
        return false;
    }
    public final static boolean isNull(Object obj) {
        if (obj == null)
            return true;

        if (obj instanceof CharSequence)
            return ((CharSequence) obj).length() == 0;

        if (obj instanceof Collection)
            return ((Collection) obj).isEmpty();

        if (obj instanceof Map)
            return ((Map) obj).isEmpty();

        if (obj instanceof Object[]) {
            Object[] object = (Object[]) obj;
            if (object.length == 0) {
                return true;
            }
            boolean empty = true;
            for (int i = 0; i < object.length; i++) {
                if (!isNull(object[i])) {
                    empty = false;
                    break;
                }
            }
            return empty;
        }

        return false;
    }
    public final static boolean isNull(Integer integer) {
        if (integer == null || integer == 0)
            return true;
        return false;
    }
    public final static boolean isNull(Collection collection) {
        if (collection == null || collection.size() == 0)
            return true;
        return false;
    }
    public final static boolean isNull(Map map) {
        if (map == null || map.size() == 0)
            return true;
        return false;
    }
    public final static boolean isNull(String str) {
        return str == null || "".equals(str.trim())
                || "null".equals(str.toLowerCase());
    }
    public final static boolean isNull(Long longs) {
        if (longs == null || longs == 0)
            return true;
        return false;
    }
    public final static boolean isNotNull(Long longs) {
        return !isNull(longs);
    }
    public final static boolean isNotNull(String str) {
        return !isNull(str);
    }
    public final static boolean isNotNull(Collection collection) {
        return !isNull(collection);
    }
    public final static boolean isNotNull(Map map) {
        return !isNull(map);
    }
    public final static boolean isNotNull(Integer integer) {
        return !isNull(integer);
    }
    public final static boolean isNotNull(Object[] objs) {
        return !isNull(objs);
    }
    public final static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

}
