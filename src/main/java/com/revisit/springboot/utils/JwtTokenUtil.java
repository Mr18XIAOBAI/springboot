// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: JwtTokenUtil
//  * Author:   Revisit-Moon
//  * Date:     2019/1/29 2:38 PM
//  * Description: JwtTokenUtil
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/29 2:38 PM        1.0              描述
//  */
//
// package com.revisit.springboot.utils;
//
// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
//
// import java.io.InputStream;
// import java.security.KeyStore;
// import java.security.PrivateKey;
// import java.security.PublicKey;
// import java.util.Date;
//
// /**
//  * 〈JwtTokenUt〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/29
//  * @since 1.0.0
//  */
//
// public class JwtTokenUtil {
//     //加载jwt.jks文件
//     private static InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jwt.jks");
//     private static PrivateKey privateKey = null;
//     private static PublicKey publicKey = null;
//
//     static {
//         try {
//             KeyStore keyStore = KeyStore.getInstance("JKS");
//             keyStore.load(inputStream, "revisit".toCharArray());
//             privateKey = (PrivateKey) keyStore.getKey("jwt", "revisit".toCharArray());
//             publicKey = keyStore.getCertificate("jwt").getPublicKey();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
//
//     public static String generateToken(String subject, int expirationSeconds) {
//         return Jwts.builder()
//                 .setClaims(null)
//                 .setSubject(subject)
//                 .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
//                 .signWith(SignatureAlgorithm.RS256, privateKey)
//                 .compact();
//     }
//
//     public static String parseToken(String accesstoken) {
//         String subject = null;
//         try {
//             Claims claims = Jwts.parser()
//                     .setSigningKey(publicKey)
//                     .parseClaimsJws(accesstoken).getBody();
//             subject = claims.getSubject();
//         } catch (Exception e) {
//         }
//         return subject;
//     }
//
// }
