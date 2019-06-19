/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: FileUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/3 1:12 PM
 * Description: FileUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/3 1:12 PM        1.0              描述
 */

package com.revisit.springboot.utils;

import com.revisit.springboot.component.appconstant.AppConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.util.ArrayUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;

/**
 * 〈FileUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/3
 * @since 1.0.0
 */
@Component
public class FileUtil{
    //文件后续名分类
    private static String[] imageType = new String[]{"jpg","png","jpeg","svg","icon",
            "bmp","pcx","tiff","gif","tga",
            "exif","fpx","psd","cdr","pcd",
            "dxf","ufo","eps","ai","raw",};

    private static String[] musicsType = new String[]{"mp3","wma","acm","aac","mmf",
            "awr","m4a","asp","au",
            "asx","acm","aif","svx","snd",
            "mid","voc","wav"};

    private static String[] videoType = new String[]{"mp4","mov","asf","avi","rm",
            "mpeg","mpg","dat","ram","viv",
            "ra","rmvb","dvd","flv"};
    private static String[] fileType = new String[]{"txt","ppt","xml","doc","pdf",
            "xls","docx","wps","xlsx"};

    //文件上传工具类服务方法
    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception{

        File targetFile = new File(filePath);
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath+fileName);
        out.write(file);
        out.flush();
        out.close();
    }

    public static String inputStreamToImage(InputStream inputStream,String imageName){
        FileOutputStream fos = null;
        try {
            String imagePath = getUploadPath("images");
            // 设置文件最终路径
            String path = imagePath + imageName;
            // 检测是否存在目录
            File dest = new File(path);
            if (!dest.getParentFile().exists()) {
                // 新建文件夹
                dest.getParentFile().mkdirs();
            }
            fos =new FileOutputStream(dest);
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes,0,1024)) != -1) {
                fos.write(bytes, 0, len);
            }
            fos.flush();
            return "/images/"+imageName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos!=null) fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String getFileType(String suffixName){
        if(ArrayUtils.contains(imageType,suffixName.toLowerCase())){
            return "images";
        }
        else if(ArrayUtils.contains(musicsType,suffixName.toLowerCase())){
            return "musics";
        }
        else if(ArrayUtils.contains(videoType,suffixName.toLowerCase())){
            return "videos";
        }
        else if(ArrayUtils.contains(fileType,suffixName.toLowerCase())){
            return "files";
        } else{
            return "";
        }
    }

    public static String getUploadPath(String fileType){
        try {
            HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
            String realPath = request.getServletContext().getRealPath("/");
            File upload = new File(realPath+AppConstant.FILE_SAVE_PATH+fileType);
            //获取绝对路径
            return upload.getAbsolutePath()+"/";
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static FileItem createFileItem(String filePath) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "textField";
        int num = filePath.lastIndexOf(".");
        String extFile = filePath.substring(num);
        FileItem item = factory.createItem(textFieldName, "text/plain", true,
                "MyFileName" + extFile);
        File newFile = new File(filePath);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try
        {
            FileInputStream fis = new FileInputStream(newFile);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192))
                    != -1)
            {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return item;
    }

    public static String getFileAbsolutePath(String fileName){
        if (fileName.startsWith("/")){
            fileName = fileName.replaceFirst("/","");
        }
        String fileType = fileName.substring(fileName.lastIndexOf("."),fileName.length());
        String fileFatherPath = getFileType(fileType);
        String uploadPath = getUploadPath(fileFatherPath);
        return uploadPath +fileName;

    }

    public static void deleteOldFileListByNewFileList(String oldFilePath,String newFilePath){
        List<String> newProductFilePathList = MoonUtil.getStringListByComma(oldFilePath);
        List<String> oldProductFilePathList = MoonUtil.getStringListByComma(newFilePath);
        for (String s : oldProductFilePathList) {
            if (!newProductFilePathList.contains(s)) {
                String videoPath = FileUtil.getFileAbsolutePath(s);
                FileUtil.deleteOneFile(videoPath);
            }
        }
    }

    public static void deleteFileList(List<String> pathList){
        if (pathList!=null&&!pathList.isEmpty()) {
            for (String s : pathList) {
                String imagePath = FileUtil.getFileAbsolutePath(s);
                //删除原图片
                File file = new File(imagePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    public static String copyFileList(List<String> pathList){
        String resultFileName = "";
        if (pathList!=null&&!pathList.isEmpty()) {
            for (String s : pathList) {
                String imagePath = FileUtil.getFileAbsolutePath(s);
                // 复制原图片
                File file = new File(imagePath);
                if (file.exists()) {
                    String newFileNamePath = StringUtils.substring(imagePath, 0, imagePath.lastIndexOf("/") + 1);
                    String newFileName = "COPY-";
                    newFileName += file.getName();
                    FileInputStream input = null;
                    FileOutputStream output = null;
                    try {
                        input = new FileInputStream(file);
                        File newFile = new File(newFileNamePath + newFileName);
                        if (!newFile.exists()) {
                            newFile.createNewFile();
                        }
                        output = new FileOutputStream(newFile);
                        byte[] b = new byte[1024 * 5];
                        int len;
                        while ((len = input.read(b)) != -1) {
                            output.write(b, 0, len);
                        }
                        output.flush();

                        resultFileName += "/"+getFileType(newFileName.substring(newFileName.lastIndexOf(".")+1,newFileName.length()))+"/" + newFileName + ",";
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (output != null) {
                                output.close();
                            }
                            if (input != null) {
                                input.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    resultFileName += s+",";
                }
            }
        }
        if (StringUtils.isNotBlank(resultFileName)) {
            resultFileName = resultFileName.substring(0, resultFileName.length() - 1);
        }
        return resultFileName;
    }

    public static void deleteOneFile(String path){
        String imagePath = FileUtil.getFileAbsolutePath(path);
        //删除原图片
        File file = new File(imagePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
