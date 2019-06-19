/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: imageUtils
 * Author:   Revisit-Moon
 * Date:     2019/2/13 6:30 PM
 * Description: imageUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/13 6:30 PM        1.0              描述
 */

package com.revisit.springboot.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.name.Rename;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 〈imageUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/13
 * @since 1.0.0
 */

public class ImageUtil {

    private final static Logger logger = LoggerFactory.getLogger(ImageUtil.class);
    /**
     * 使用给定的图片生成指定大小的图片
     */
    public static void generateFixedSizeImage(File file){
        try {
            Thumbnails.of(file).size(80,80).toFile(file);
        } catch (IOException e) {
            logger.error("图片工具异常,异常原因",e.getCause());
        }
    }

    /**
     * 对原图加水印,然后顺时针旋转90度,最后压缩为80%保存
     */
    public static void generateRotationWatermark(File file){
        try {
            Thumbnails.of(file).
                    // 缩放大小
                            size(1600,1600).
                    // 顺时针旋转90度
                            rotate(90).
                    //水印位于右下角,半透明
                            watermark(Positions.BOTTOM_RIGHT, ImageIO.read(file),1f).
                    // 图片压缩80%质量
                            outputQuality(0.8).
                    toFile(file);
        } catch (IOException e) {
            logger.error("图片工具异常,异常原因",e.getCause());
        }
    }

    /**
     * 转换图片格式,将流写入到输出流
     */
    public static void generateOutputStream(File file){
        try(OutputStream outputStream = new FileOutputStream(file)) {
            Thumbnails.of(file).
                    size(500,500).
                    // 转换格式
                            outputFormat("jpg").
                    // 写入输出流
                            toOutputStream(outputStream);
        } catch (IOException e) {
            logger.error("图片工具异常,异常原因",e.getCause());
        }
    }

    /**
     * 按比例缩放图片
     */
    public static void generateScale(File file){
        try {
            Thumbnails.of(file).
                    scalingMode(
                            ScalingMode.BICUBIC).
                    // 图片缩放80%, 不能和size()一起使用
                            scale(1).
                    // 图片质量压缩80%
                            outputQuality(0.5).
                    toFile(file);
        } catch (IOException e) {
            logger.error("图片工具异常,异常原因",e.getCause());
        }
    }

    /**
     * 生成缩略图到指定的目录
     */
    public static void generateThumbnail2Directory(File file){
        try {
            Thumbnails.of(file).
                    // 图片缩放80%, 不能和size()一起使用
                            scale(0.8).
                    //指定的目录一定要存在,否则报错
                            toFiles(file, Rename.NO_CHANGE);
        } catch (IOException e) {
            logger.error("图片工具异常,异常原因",e.getCause());
        }
    }

    /**
     * 将指定目录下所有图片生成缩略图
     */
    public static void generateDirectoryThumbnail(String filePath){
        try {
            Thumbnails.of(
                    new File(filePath).listFiles()).
                    scale(0.8).
                    toFiles(new File(filePath), Rename.SUFFIX_HYPHEN_THUMBNAIL);
        } catch (IOException e) {
            logger.error("图片工具异常,异常原因",e.getCause());
        }
    }

    /**
     * 〈修复富文本中的Base64图片〉
     *
     * @param editDetail
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/9 11:50 AM
     */
    public static String fixBase64Edit(String editDetail){
        if (editDetail.contains("<img src=")){
            String[] strings = StringUtils.substringsBetween(editDetail, "src=\"", "\"");
            for (String s :strings) {
                //判断是否Base64格式
                int end = s.indexOf(";base64,");
                if(end < 11){
                    logger.info("Base64图片数据错误！");
                    continue;
                }
                try {
                    MultipartFile multipartFiles = BASE64DecodedMultipartFile.base64ToMultipart(s);
                    MultipartFile[] files = new MultipartFile[]{multipartFiles};
                    for (MultipartFile multipartFile : files) {

                        // 获取文件名
                        String fileName = multipartFile.getOriginalFilename().trim();

                        // 获取文件的后缀名
                        String suffixName = fileName.substring(fileName.lastIndexOf("."));

                        // 设置文件存储路径
                        String fileType = FileUtil.getFileType(StringUtils.remove(suffixName,"."));

                        if (StringUtils.isBlank(fileType)){
                            continue;
                        }
                        //获取上传目录
                        String filePath = FileUtil.getUploadPath(fileType);

                        //转换文件名,防止重复
                        fileName = UUID.randomUUID().toString()+suffixName;

                        // 设置文件最终路径
                        String path = filePath + fileName;

                        // 检测是否存在目录
                        File dest = new File(path);
                        if (!dest.getParentFile().exists()) {
                            // 新建文件夹
                            dest.getParentFile().mkdirs();
                        }
                        logger.info("[文件类型] - [{}]", fileType);
                        logger.info("[文件名称] - [{}]", fileName);
                        logger.info("[文件大小] - [{}]", multipartFile.getSize());
                        logger.info("[文件后缀名] - [{}]", suffixName);
                        logger.info("[保存目录] - [{}]", filePath);

                        // 文件写入
                        multipartFile.transferTo(dest);

                        if (fileType.equals("images")){
                            String uuidFileName = UUID.randomUUID().toString()+".jpg";
                            String newPath = filePath + uuidFileName;
                            File newFile = new File(newPath);
                            Thumbnails.of(dest)
                                    .scale(1f)
                                    .outputQuality(0.5f)
                                    .outputFormat("jpg")
                                    .toFile(newFile);
                            dest.delete();
                            fileName = getWebServerUrlName()+"/images/"+newFile.getName();
                            editDetail = StringUtils.replace(editDetail,s,fileName);
                        }else{
                            fileName += getWebServerUrlName()+"/"+fileType+"/"+fileName;
                            editDetail = StringUtils.replace(editDetail,s,fileName);
                        }
                    }
                } catch (Exception e) {
                    Result.fail(110,"系统异常","上传失败,请稍后重试");
                }
            }
        }
        return editDetail;
    }

    private static String getWebServerUrlName(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestUrl = request.getScheme() //当前链接使用的协议
                +"://" + request.getServerName()//服务器地址
                // + ":" + request.getServerPort() //端口号
                + request.getContextPath(); //应用名称
        return requestUrl;
    }
}
