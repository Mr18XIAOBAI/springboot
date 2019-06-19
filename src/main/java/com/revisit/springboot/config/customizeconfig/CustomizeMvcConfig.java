/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: customizeMvcConfig
 * Author:   Revisit-Moon
 * Date:     2019/1/29 6:16 PM
 * Description: customizeMvcConfig
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/29 6:16 PM        1.0              描述
 */

package com.revisit.springboot.config.customizeconfig;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revisit.springboot.component.ajaxhandler.CustomizeResponseBody;
import com.revisit.springboot.component.appconstant.AppConstant;
import com.revisit.springboot.config.fastJson.FastJSONConfig;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.poi.ss.formula.functions.T;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.bouncycastle.jcajce.provider.keystore.PKCS12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 〈customizeMvcConfig〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
@Configuration
public class CustomizeMvcConfig implements WebMvcConfigurer{

    private final Logger logger = LoggerFactory.getLogger(CustomizeMvcConfig.class);

    //1.这个为解决中文乱码
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converter.setWriteAcceptCharset(false);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.ALL);
        converter.setSupportedMediaTypes(mediaTypeList);
        return converter;
    }

    //2.1：解决中文乱码后，返回json时可能会出现No converter found for return value of type: xxxx
    //或这个：Could not find acceptable representation
    //解决此问题如下
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    //2.2：解决No converter found for return value of type: xxxx
    public MappingJackson2HttpMessageConverter messageConverter() {
        MappingJackson2HttpMessageConverter converter=new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(getObjectMapper());
        return converter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CustomizeResponseBody()).addPathPatterns("/**");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // super.configureMessageConverters(converters);

        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
        while(iterator.hasNext()){
            HttpMessageConverter<?> converter = iterator.next();
            if(converter instanceof MappingJackson2HttpMessageConverter){
                iterator.remove();
            }
            if(converter instanceof StringHttpMessageConverter){
                iterator.remove();
            }
            if(converter instanceof FastJsonHttpMessageConverter){
                iterator.remove();
            }
        }

        //解决中文乱码
        converters.add(responseBodyConverter());

        //解决： 添加解决中文乱码后的配置之后，返回json数据直接报错 500：no convertter for return value of type
        //或这个：Could not find acceptable representation
        // converters.add(messageConverter());

        //fastJson格式日期时间
        converters.add(FastJSONConfig.customizeFastJsonConverter());

    }



    // @Override
    // public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    //     /* 是否通过请求Url的扩展名来决定media type */
    //     configurer.favorPathExtension(true)
    //             /* 不检查Accept请求头 */
    //             .ignoreAcceptHeader(true)
    //             .parameterName("mediaType")
    //             /* 设置默认的media type */
    //             .defaultContentType(MediaType.TEXT_HTML)
    //             /* 请求以.html结尾的会被当成MediaType.TEXT_HTML*/
    //             .mediaType("html", MediaType.TEXT_HTML)
    //             /* 请求以.json结尾的会被当成MediaType.APPLICATION_JSON*/
    //             .mediaType("json", MediaType.APPLICATION_JSON);
    // }

    /**
     * 〈跨域配置〉
     *
     * @param registry
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/1 5:21 PM
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedMethods("*")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // try {
        //     registry.addResourceHandler("/**").addResourceLocations("classpath:/upload/user/"
        //             ,"classpath:/upload/user/images/"
        //             ,"classpath:/upload/user/musics/"
        //             ,"classpath:/upload/user/videos/"
        //             ,"classpath:/upload/user/files/");
        // }catch (Exception e){
        //     e.printStackTrace();
        // }
        try {
            String path = AppConstant.FILE_FIND_PATH + AppConstant.FILE_SAVE_PATH;
            logger.info("初始化映射地址:" + path);
            registry.addResourceHandler("/**").addResourceLocations("file:"+path
                    ,"file:"+path+"images/"
                    ,"file:"+path+"musics/"
                    ,"file:"+path+"videos/"
                    ,"file:"+path+"files/");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 〈解决内置tomcat RFC7230〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019-06-17 11:49
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]");//允许的特殊字符
            }
        });
        return factory;
    }

}
