/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: UploadFile
 * Author:   Revisit-Moon
 * Date:     2019/2/3 12:26 PM
 * Description: upload.UploadFile
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/3 12:26 PM        1.0              描述
 */

package com.revisit.springboot.controller.upload;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.utils.BASE64DecodedMultipartFile;
import com.revisit.springboot.utils.FileUtil;
import com.revisit.springboot.utils.Result;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.UUID;

/**
 * 〈upload.UploadFile〉
 *
 * @author Revisit-Moon
 * @create 2019/2/3
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/upload")
public class UploadFile {
    private static final Logger logger = LoggerFactory.getLogger(UploadFile.class);

    @Autowired
    private HttpServletRequest request;

    @PostMapping
    public JSONObject upload(@RequestParam(value = "file",required = false) MultipartFile[] files){
        String uploadPath = "";
        try {
            if (files==null||files.length==0){
                return Result.fail(102,"参数错误","缺少必填参数");
            }
            for (MultipartFile multipartFile : files) {
                // 获取文件名
                String fileName = multipartFile.getOriginalFilename().trim();
                // 获取文件的后缀名
                String suffixName = fileName.substring(fileName.lastIndexOf("."));
                // 设置文件存储路径
                String fileType = FileUtil.getFileType(StringUtils.remove(suffixName,"."));
                if (StringUtils.isBlank(fileType)){
                    return Result.fail(102,"参数异常","上传失败,格式不支持");
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
                    uploadPath += uploadImage(filePath,dest)+",";
                }else{
                    uploadPath += "/"+fileType+"/"+fileName+",";
                }
            }
        }catch (Exception e){
            return Result.fail(110,"系统异常","上传失败,请稍后重试");
        }
        uploadPath = StringUtils.removeEnd(uploadPath,",");
        return Result.success(200,"上传成功",uploadPath);
    }

    @PostMapping(value = "/base64")
    public JSONObject uploadBase64File(@RequestBody JSONObject param){
        String base64File = param.getString("base64File");
        if (StringUtils.isBlank(base64File)){
            return Result.fail(102,"参数错误","缺少必填参数");
        }
        try {
            MultipartFile multipartFile = BASE64DecodedMultipartFile.base64ToMultipart(base64File);
            MultipartFile[] files = new MultipartFile[]{multipartFile};
            return (JSONObject) JSON.toJSON(upload(files));
        } catch (Exception e) {
            return Result.fail(110,"系统异常","上传失败,请稍后重试");
        }
    }

    private String uploadImage(String filePath,File oldFile){
        try {
            String fileName = UUID.randomUUID().toString()+".jpg";
            String path = filePath + fileName;
            File newFile = new File(path);
            Thumbnails.of(oldFile)
                    .scale(1f)
                    .outputQuality(0.5f)
                    .outputFormat("jpg")
                    .toFile(newFile);
            oldFile.delete();
            return "/images/"+fileName;
        }catch (Exception e){
            logger.info("图片压缩异常: "+e.getCause().toString());
            return "图片压缩异常: "+e.getCause().toString();
        }
    }
}
