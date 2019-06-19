/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CustomizeCodeGenerator
 * Author:   Revisit-Moon
 * Date:     2019/2/25 5:10 PM
 * Description: codegenerator.CustomizeCodeGenerator
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/25 5:10 PM        1.0              描述
 */

package com.revisit.springboot.component.codegenerator;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.RevisitSpringBootApplication;
import com.revisit.springboot.utils.FreeMarkerTemplateUtil;
import freemarker.template.Template;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈codegenerator.CustomizeCodeGenerator〉
 *
 * @author Revisit-Moon
 * @create 2019/2/25
 * @since 1.0.0
 */
@Component
public class CustomizeCodeGenerator {
    public void beginGenerator(String dbName,String dbPwd,String dbUrl,String dbDriver,String dbTableName){
        //获取数据库连接
        CodeGeneratorConnection connection = new CodeGeneratorConnection(dbName,dbPwd,dbUrl,dbDriver);

        //获取数据库元数据
        Map<String, List<Map<String, Object>>> metadataList = connection.getDataBaseMetadata(connection,dbTableName);

        //遍历元数据
        for (Map.Entry entry : metadataList.entrySet()) {
            //初始化生成实体类
            CodeGeneratorEntity generatorEntity = new CodeGeneratorEntity();
            //设置实体类名类名
            generatorEntity.setEntityName(CodeGeneratorDataBaseNameUtil.translate(entry.getKey().toString(), true));
            //设置实体类备注作者
            generatorEntity.setAuthor("Revisit-Zhang");
            //设置实体类备注日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            generatorEntity.setDate(sdf.format(new Date()));
            //设置实体类引用的表名
            generatorEntity.setTableName(entry.getKey().toString());
            //获取数据库元数据表中的列
            List<Map<String, Object>> entityColumnList = (List<Map<String, Object>>) entry.getValue();
            //设置实体类的属性列表
            generatorEntity.setEntityColumnList(entityColumnList);
            try {
                System.out.println(JSONObject.toJSONString(generatorEntity));
                //生成实体类=>entity包下的实体类
                generateEntityFile(generatorEntity);
                //生成controller类=>controller包下的实体controller类
                generateControllerFile(generatorEntity);
                //生成service接口类=>service包下的实体service接口类
                generateServiceFile(generatorEntity);
                //生成service实现类=>service包下的实体service实现类
                generateServiceImplFile(generatorEntity);
                //生成Repository接口类=>repository包下的实体Repository接口类
                generateRepositoryFile(generatorEntity);
                // generateDaoImplFile(generatorEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 〈生成实体类〉
     *
     * @param generatorEntity
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/27 3:24 PM
     */
    private void generateEntityFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = ".java";
        String path = localPath() +"/entity/" + generatorEntity.getEntityName().toLowerCase() + "/" + generatorEntity.getEntityName() + suffix;
        String templateName = "Entity.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        // generatorBasicClass(packageName());
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }

    /**
     * 〈生成controller实体类〉
     *
     * @param generatorEntity
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/27 3:24 PM
     */
    private void generateControllerFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "Controller.java";
        String path = localPath() +"/controller/" + generatorEntity.getEntityName().toLowerCase() + "/" + generatorEntity.getEntityName() + suffix;
        String templateName = "Controller.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }

    /**
     * 〈生成Service接口类〉
     *
     * @param generatorEntity
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/27 3:47 PM
     */
    private void generateServiceFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "Service.java";
        String path = localPath() +"/service/" + generatorEntity.getEntityName().toLowerCase() + "/" + generatorEntity.getEntityName() + suffix;
        String templateName = "Service.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }
    /**
     * 〈生成Service实现类〉
     *
     * @param generatorEntity
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/27 6:36 PM
     */
    private void generateServiceImplFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "ServiceImpl.java";
        String path = localPath() +"/service/"+generatorEntity.getEntityName().toLowerCase() + "/impl/" + generatorEntity.getEntityName() + suffix;
        String templateName = "ServiceImpl.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }

    /**
     * 〈生成JPA接口类〉
     *
     * @param generatorEntity
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/27 6:37 PM
     */
    private void generateRepositoryFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "Repository.java";
        String path = localPath() +"/repository/" + generatorEntity.getEntityName().toLowerCase()+ "/" + generatorEntity.getEntityName() + suffix;
        String templateName = "Repository.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }

    private void generatorBasicClass(String packageName)throws Exception{
        String suffix = ".java";
        String path = localPath() +"/entity/BasicEntity" +suffix;
        String templateName = "BasicEntity.ftl";
        File generatorFile = new File(path);
        if (!generatorFile.exists()){
            File file = new File(localPath() + "/entity");
            file.mkdirs();
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("packageName",packageName);
            generateFileByTemplate(templateName,generatorFile,dataMap);
        }
        File controllerFile = new File(localPath() + "/controller");
        if (!controllerFile.exists()) {
            controllerFile.mkdirs();
        }
        File serviceFile = new File(localPath() + "/service");
        if (!serviceFile.exists()) {
            serviceFile.mkdirs();
        }
        File serviceImplFile = new File(localPath() + "/service/impl");
        if (!serviceImplFile.exists()) {
            serviceImplFile.mkdirs();
        }
        File daoFile = new File(localPath() + "/dao");
        if (!daoFile.exists()) {
            daoFile.mkdirs();
        }
        File daoImplFile = new File(localPath() + "/dao/impl");
        if (!daoImplFile.exists()) {
            daoImplFile.mkdirs();
        }
    }

    private String localPath(){
        //获取项目路径
        String prjPath = System.getProperty("user.dir");
        //添加该路径下IDEA工具默认的包名
        prjPath = prjPath+"/src/main/java";
        //获取启动类的包名
        String packAge = RevisitSpringBootApplication.class.getPackage().getName();
        // packAge = packAge.substring(0,packAge.lastIndexOf("."));
        //将包名转换为路径
        packAge = packageConvertPath(packAge);
        //得到启动类的包路径
        String classPath = prjPath+packAge;
        //转换为文件对象
        File file = new File(classPath);
        //判断文件夹是否存在
        if (file.exists()){
            //获取文件夹的绝对路径
            return file.getAbsolutePath();
        }else{
            //如果不存在,则创建该文件夹目录
            boolean isCreate = file.mkdirs();
            //如果创建成功则返回该文件夹的绝对路径,否则返回空字串
            if (isCreate){
                return file.getAbsolutePath();
            }else{
                return "";
            }
        }
    }

    private String packageConvertPath(String packageName) {
        //转换包文为路径
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }

    private String packageName(){
        //获取包名
        String packAge = RevisitSpringBootApplication.class.getPackage().getName();
        // packAge = packAge.substring(0,packAge.lastIndexOf("."));
        //获取到则返回包名,否则返回空字串
        if (packAge!=null||!packAge.equals("")){
            return packAge;
        }else{
            return "";
        }
    }

    private Map<String,Object> getEntityColumnMap(CodeGeneratorEntity generatorEntity){
        Map<String,Object> entityColumnMap = new HashMap<>();
        entityColumnMap.put("entityColumnList",generatorEntity.getEntityColumnList());
        entityColumnMap.put("entityName",generatorEntity.getEntityName());
        entityColumnMap.put("author",generatorEntity.getAuthor());
        entityColumnMap.put("date",generatorEntity.getDate());
        entityColumnMap.put("packageName",packageName());
        entityColumnMap.put("tableName",generatorEntity.getTableName());
        return entityColumnMap;
    }

    private void generateFileByTemplate(String templateName,File file,Map<String,Object> dataMap){
        try {
            Object entityName = dataMap.get("entityName");
            if (entityName!=null){
                if (entityName.toString().equals("RoleAuthoritiesRelation") || entityName.toString().equals("UserRoleRelation")) {
                    return;
                }
            }
            if (!file.exists()){
                String parent = file.getParent();
                File fatherFile = new File(parent);
                if (!fatherFile.exists()){
                    fatherFile.mkdirs();
                }
                file.createNewFile();
                Template template = FreeMarkerTemplateUtil.getTemplate(templateName);
                FileOutputStream fos = new FileOutputStream(file);
                Writer out = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"), 10240);
                template.process(dataMap, out);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
