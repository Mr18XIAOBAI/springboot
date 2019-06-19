/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: Base64UUIDGenerate
 * Author:   Revisit-Moon
 * Date:     2019/2/19 10:57 AM
 * Description: Base64UUIDGenerate
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/19 10:57 AM        1.0              描述
 */

package com.revisit.springboot.component.uuid;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

/**
 * 〈Base64UUIDGenerate〉
 *
 * @author Revisit-Moon
 * @create 2019/2/19
 * @since 1.0.0
 */
@Component
@Configuration
public class CustomizeUUIDGenerate implements Configurable,IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return generateBase64UUID(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {

    }

    public static String generateBase64UUID(String uuid){
        UUID uuidFix = fixUUID(uuid);
        if (uuidFix==null){
            throw new IllegalArgumentException("Invalid UUID error!");
        }
        byte[] uuidFixBytes = new byte[16];
        long most = uuidFix.getMostSignificantBits();
        long least = uuidFix.getLeastSignificantBits();
        longToBytes(most,uuidFixBytes,0);
        longToBytes(least,uuidFixBytes,8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidFixBytes);
    }
    // public static String UUID2Base64UUID(String uuidString) {
    //     UUID uuid = UUID.fromString(uuidString);
    //     return toBase64UUID(uuid);
    // }
    //
    public static String Base64UUIDToUUID(String base64uuid) {
        if (base64uuid.length() != 22) {
            throw new IllegalArgumentException("Invalid base64UUID error!");
        }
        byte[] byUuid = Base64.getUrlDecoder().decode(base64uuid + "==");
        long most = bytesToLong(byUuid, 0);
        long least = bytesToLong(byUuid, 8);
        UUID uuid = new UUID(most, least);
        return uuid.toString().replaceAll("-","");
    }
    //
    // private static String toBase64UUID(UUID uuid) {
    //     byte[] byUuid = new byte[16];
    //     long least = uuid.getLeastSignificantBits();
    //     long most = uuid.getMostSignificantBits();
    //     long2bytes(most, byUuid, 0);
    //     long2bytes(least, byUuid, 8);
    //     String compressUUID = Base64.encodeBase64URLSafeString(byUuid);
    //     return compressUUID;
    // }
    //

    private static UUID fixUUID(String uuid){
        int[] ints = {8, 13, 18, 23};
        if (StringUtils.isBlank(uuid)){
            return null;
        }
        if (uuid.length()!=32&&uuid.length()!=36) {
            return null;
        }
        UUID uuidFix = null;
        if (uuid.length()==32) {
            StringBuffer uuidWaitFix = new StringBuffer(uuid);
            for (int i = 0; i < ints.length ; i++) {
                uuidWaitFix.insert(ints[i],"-");
            }
            uuidFix = UUID.fromString(uuidWaitFix.toString());
        }
        if (uuid.length()==36&&uuid.contains("-")){
            uuidFix = UUID.fromString(uuid);
        }
        return uuidFix;
    }

    private static void longToBytes(long value, byte[] bytes, int offset) {
        for (int i = 7; i > -1; i--) {
            bytes[offset++] = (byte) ((value >> 8 * i) & 0xFF);
        }
    }
    //
    private static long bytesToLong(byte[] bytes, int offset) {
        long value = 0;
        for (int i = 7; i > -1; i--) {
            value |= (((long) bytes[offset++]) & 0xFF) << 8 * i;
        }
        return value;
    }
}