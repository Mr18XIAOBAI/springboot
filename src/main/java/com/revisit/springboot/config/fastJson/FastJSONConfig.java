/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: FastJsonConfig
 * Author:   Revisit-Moon
 * Date:     2019/1/28 4:15 PM
 * Description: fastJson.FastJsonConfig
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/28 4:15 PM        1.0              描述
 */

package com.revisit.springboot.config.fastJson;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈fastJson.FastJsonConfig〉
 *
 * @author Revisit-Moon
 * @create 2019/1/28
 * @since 1.0.0
 */
@Configuration
public class FastJSONConfig {

    @Bean
    public static FastJsonHttpMessageConverter customizeFastJsonConverter() {

        // 1.需要先定义一个convert 转换消息的对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        // 2.添加fastJson的配置信息,比如，是否需要格式化返回的json数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();

        // 空值特别处理
        // WriteNullListAsEmpty 将Collection类型字段的字段空值输出为[]
        // WriteNullStringAsEmpty 将字符串类型字段的空值输出为空字符串 ""
        // WriteNullNumberAsZero 将数值类型字段的空值输出为0
        // WriteNullBooleanAsFalse 将Boolean类型字段的空值输出为false
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                SerializerFeature.DisableCircularReferenceDetect,
                // SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty
        );

        // fastJsonConfig.setSerializeFilters((ValueFilter) (o, s, source) -> {
        //     if (source == null) {
        //         return "";  //此处是关键,如果返回对象的变量为null,则自动变成""
        //     }
        //     return source;
        // });

        // 处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();

        // 处理日期格式
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");

        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);

        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        // 3.在convert中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);

        return fastConverter;
    }
}
