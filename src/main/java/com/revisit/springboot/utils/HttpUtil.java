/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: HttpClientUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/18 1:23 AM
 * Description: HttpClientUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/18 1:23 AM        1.0              描述
 */

package com.revisit.springboot.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * 〈HttpClientUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/18
 * @since 1.0.0
 */

public class HttpUtil {

    private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);


    public static RestTemplate getRestTemplate(){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10*1000);
        requestFactory.setReadTimeout(10*1000);
        RestTemplate client = new RestTemplate(requestFactory);
        return client;
    }

    public static String httpFormPost(String url, MultiValueMap<String, String> params) throws IOException {
        RestTemplate client = getRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        // 指定表单方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        //  执行HTTP请求
        ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
    }

    public static JSONObject httpJsonPost(String url, JSONObject requestData) throws IOException {
        RestTemplate client = getRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        // 指定application/json方式提交
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);
        //  执行HTTP请求
        ResponseEntity<JSONObject> response = client.exchange(url,HttpMethod.POST, entity, JSONObject.class);
        return response.getBody();
    }

    public static String httpXmlPost(String url, String requestData) throws IOException {
        RestTemplate client = getRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        // 指定text/xml方式提交
        headers.setContentType(MediaType.parseMediaType("text/xml; charset=UTF-8"));
        headers.setContentLength(requestData.length());
        HttpEntity<String> entity = new HttpEntity<>(requestData, headers);
        //  执行HTTP请求
        ResponseEntity<byte[]> response = client.exchange(url,HttpMethod.POST, entity,byte[].class);
        InputStream inputStream = new ByteArrayInputStream(response.getBody());
        byte[] xml = new byte[inputStream.available()];
        inputStream.read(xml);
        // 转成字符串
        return new String(xml);
    }

    public static InputStream httpJsonPostReturnInputStream(String url, JSONObject requestData) throws IOException {
        RestTemplate client = getRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        // 指定application/json方式提交
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);
        //  执行HTTP请求
        ResponseEntity<byte[]> response = client.exchange(url,HttpMethod.POST, entity,byte[].class);
        if (response.getHeaders().getContentType().equals(MediaType.APPLICATION_JSON_UTF8)){
            JSONObject result = JSONObject.parseObject(new String(response.getBody(),"UTF-8"));
            logger.info("生成二维码错误: "+ result.toJSONString());
            return null;
        }
        InputStream inputStream = new ByteArrayInputStream(response.getBody());
        return inputStream;
    }

    public static String httpXmlPostSSL(String url, String requestData) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream instream = classLoader.getResourceAsStream("apiclient_cert.p12");
        keyStore.load(instream, WeChatUtil.getWeChatMchId().toCharArray());
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, WeChatUtil.getWeChatMchId().toCharArray())
                .build();
        // Allow TLSv1 protocol only
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,  new String[]{"TLSv1"},
                null,hostnameVerifier);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpclient);

        RestTemplate client = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        // 指定text/xml方式提交
        headers.setContentType(MediaType.parseMediaType("text/xml; charset=UTF-8"));
        headers.setContentLength(requestData.length());
        HttpEntity<String> entity = new HttpEntity<>(requestData, headers);
        //  执行HTTP请求
        ResponseEntity<byte[]> response = client.exchange(url,HttpMethod.POST, entity,byte[].class);
        InputStream inputStream = new ByteArrayInputStream(response.getBody());
        byte[] xml = new byte[inputStream.available()];
        inputStream.read(xml);
        // 转成字符串
        return new String(xml);
    }
}
