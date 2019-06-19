/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: codeGeneratorConnection
 * Author:   Revisit-Moon
 * Date:     2019/2/25 5:20 PM
 * Description: codeGeneratorConnection
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/25 5:20 PM        1.0              描述
 */

package com.revisit.springboot.component.codegenerator;

import com.revisit.springboot.RevisitSpringBootApplication;
import com.revisit.springboot.utils.FreeMarkerTemplateUtil;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈codeGeneratorConnection〉
 *
 * @author Revisit-Moon
 * @create 2019/2/25
 * @since 1.0.0
 */

public class CodeGeneratorConnection {
    private String userName;
    private String passWord;
    private String dataBaseUrl;
    private String driver;
    private static Connection connection;
    private static DatabaseMetaData databaseMetaData;

    public CodeGeneratorConnection() {
    }

    //构造器
    public CodeGeneratorConnection(String userName, String passWord, String dataBaseUrl, String driver) {
        this.userName = userName;
        this.passWord = passWord;
        this.dataBaseUrl = dataBaseUrl;
        this.driver = driver;
        getConnection();
    }

    //获取数据库连接
    private Connection getConnection() {
        try {
            if (connection==null||connection.isClosed()){
                Class.forName(driver);
                setConnection(DriverManager.getConnection(dataBaseUrl,userName,passWord));
                setDatabaseMetaData(connection.getMetaData());
            }
        }catch (Exception e) {
            System.out.println("数据库连接失败");
            e.printStackTrace();
        }
        return connection;
    }
    private void setConnection(Connection connection) {
        this.connection = connection;
    }

    //获取数据库库元数据
    private DatabaseMetaData getDatabaseMetaData() {
        if (connection!=null&&databaseMetaData==null){
            try {
                databaseMetaData = connection.getMetaData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return databaseMetaData;
    }

    private void setDatabaseMetaData(DatabaseMetaData databaseMetaData) {
        this.databaseMetaData = databaseMetaData;
    }

    //获取数据库表
    private List<String> getTableNameList(DatabaseMetaData databaseMetaData){
        ResultSet rs=null;
        List<String> nameList=new ArrayList<>();
        try {
            if (databaseMetaData==null) {
                getConnection();
            }
            rs = databaseMetaData.getTables(connection.getCatalog(), userName, null, new String[]{"TABLE"});

            while (rs.next()){
                String tName=rs.getString("TABLE_NAME");
                nameList.add(tName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return nameList;
    }

    //根据某个实体类获取表数据
    private List<String> getTableNameListBySomeOneEntity(DatabaseMetaData databaseMetaData,String tableName){
        ResultSet rs=null;
        List<String> nameList=new ArrayList<>();
        try {
            if (databaseMetaData==null) {
                getConnection();
            }
            rs = databaseMetaData.getTables(connection.getCatalog(), userName, tableName.toUpperCase(), new String[]{"TABLE"});

            while (rs.next()){
                String tName=rs.getString("TABLE_NAME");
                nameList.add(tName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return nameList;
    }

    /**
     * 列信息数组的集合。List中每个元素是一个数组，代表一个列的信息；
     * 每个数组的元素1是列名，元素2是注释，元素3是类型
     * @return
     */
    private List<Map<String,String[]>> getTableColumnsInfo(List<String> tableNameList,DatabaseMetaData databaseMetaData){
        try {
            if (databaseMetaData==null) {
                getConnection();
            }
            List<Map<String,String[]>> columnInfoList=new ArrayList<>();
            for (String tableName : tableNameList) {
                ResultSet rs = databaseMetaData.getColumns(connection.getCatalog(), userName, tableName, "%");
                while (rs.next()) {
                    Map<String,String[]> tableColumns = new HashMap<>();
                    String[] colInfo = new String[4];
                    colInfo[0] = rs.getString("COLUMN_NAME");
                    if (!rs.getString("COLUMN_NAME").equals("")) {
                        colInfo[0] = rs.getString("COLUMN_NAME");
                    }else {
                        colInfo[0] = "null";
                    }
                    if (!rs.getString("REMARKS").equals("")) {
                        colInfo[1] = rs.getString("REMARKS");
                    }else {
                        colInfo[1] = "null";
                    }
                    if (!rs.getString("TYPE_NAME").equals("")) {
                        colInfo[2] = CodeGeneratorDataBaseNameUtil.dbTypeChangeJavaType(rs.getString("TYPE_NAME"));
                    }else {
                        colInfo[2] = "null";
                    }
                    if (!rs.getString("COLUMN_NAME").equals("")) {
                        colInfo[3] = rs.getString("COLUMN_NAME");
                    }else {
                        colInfo[3] = "null";
                    }
                    tableColumns.put(tableName,colInfo);
                    columnInfoList.add(tableColumns);
                }
            }
            return columnInfoList;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 〈获取数据库元数据〉
     *
     * @param codeGeneratorConnection,tableName
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/25 6:10 PM
     */
    public Map<String,List<Map<String,Object>>> getDataBaseMetadata(CodeGeneratorConnection codeGeneratorConnection,String tableName){
        DatabaseMetaData databaseMetaData = codeGeneratorConnection.getDatabaseMetaData();
        if (StringUtils.isBlank(tableName)){
            List<String> tableNameList = codeGeneratorConnection.getTableNameList(databaseMetaData);
            List<String> entityName = new ArrayList<>();
            for (String s : tableNameList) {
                String translate = CodeGeneratorDataBaseNameUtil.translate(s, true);
                entityName.add(translate);
            }
            List<Map<String, String[]>> tableColumnsInfo = codeGeneratorConnection.getTableColumnsInfo(tableNameList, databaseMetaData);
            Map<String,List<Map<String,Object>>> fixEntity = new HashMap<>();
            List<Map<String,Object>> entityList = null;
            for (Map<String, String[]> stringMap : tableColumnsInfo) {
                for (Map.Entry entry :stringMap.entrySet()) {
                    Map<String,Object> entity = new HashMap<>();
                    if (!entry.getKey().equals(tableName)){
                        entityList = new ArrayList<>();
                        tableName = entry.getKey().toString();
                    }
                    String[] tableFiled = (String[]) entry.getValue();
                    if (!tableFiled[0].equals("null")) {
                        entity.put("columnName", CodeGeneratorDataBaseNameUtil.translate(tableFiled[0],false));
                    }
                    if (!tableFiled[1].equals("null")) {
                        entity.put("remark",tableFiled[1]);
                    }
                    if (!tableFiled[2].equals("null")) {
                        entity.put("typeName",tableFiled[2]);
                    }
                    if (!tableFiled[3].equals("null")) {
                        entity.put("changeColumnName",tableFiled[3]);
                    }
                    entityList.add(entity);
                    fixEntity.put(tableName,entityList);
                }
            }
            return fixEntity;
        }else{
            List<String> tableNameList = codeGeneratorConnection.getTableNameListBySomeOneEntity(databaseMetaData,tableName);
            List<String> entityName = new ArrayList<>();
            for (String s : tableNameList) {
                String translate = CodeGeneratorDataBaseNameUtil.translate(s, true);
                entityName.add(translate);
            }
            List<Map<String, String[]>> tableColumnsInfo = codeGeneratorConnection.getTableColumnsInfo(tableNameList, databaseMetaData);
            Map<String,List<Map<String,Object>>> fixEntity = new HashMap<>();
            List<Map<String,Object>> entityList = new ArrayList<>();
            for (Map<String, String[]> stringMap : tableColumnsInfo) {
                for (Map.Entry entry :stringMap.entrySet()) {
                    Map<String,Object> entity = new HashMap<>();
                    // if (!entry.getKey().equals(tableName)){
                    //     entityList = new ArrayList<>();
                    //     tableName = entry.getKey().toString();
                    // }
                    String[] tableFiled = (String[]) entry.getValue();
                    if (!tableFiled[0].equals("null")) {
                        entity.put("columnName", CodeGeneratorDataBaseNameUtil.translate(tableFiled[0],false));
                    }
                    if (!tableFiled[1].equals("null")) {
                        entity.put("remark",tableFiled[1]);
                    }
                    if (!tableFiled[2].equals("null")) {
                        entity.put("typeName",tableFiled[2]);
                    }
                    if (!tableFiled[3].equals("null")) {
                        entity.put("changeColumnName",tableFiled[3]);
                    }
                    entityList.add(entity);
                    fixEntity.put(tableName,entityList);
                }
            }
            return fixEntity;
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

    private void generateEntityFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = ".java";
        String path = localPath() +"/entity/" + generatorEntity.getEntityName() + suffix;
        String templateName = "Entity.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generatorBasicClass(packageName());
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }

    private void generateControllerFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "Controller.java";
        String path = localPath() +"/controller/" + generatorEntity.getEntityName().toLowerCase() + generatorEntity.getEntityName() + suffix;
        String templateName = "Controller.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }

    private void generateServiceFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "Service.java";
        String path = localPath() +"/service/" + generatorEntity.getEntityName().toLowerCase() + generatorEntity.getEntityName() + suffix;
        String templateName = "Service.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }

    private void generateServiceImplFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "ServiceImpl.java";
        String path = localPath() +"/service/impl/" + generatorEntity.getEntityName().toLowerCase() + generatorEntity.getEntityName() + suffix;
        String templateName = "ServiceImpl.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }
    private void generateDaoFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "Dao.java";
        String path = localPath() +"/dao/" + generatorEntity.getEntityName().toLowerCase() + generatorEntity.getEntityName() + suffix;
        String templateName = "Repository.ftl";
        File generatorFile = new File(path);
        if (generatorFile.exists()){
            return;
        }
        Map<String, Object> entityColumnMap = getEntityColumnMap(generatorEntity);
        generateFileByTemplate(templateName,generatorFile,entityColumnMap);
    }
    private void generateDaoImplFile(CodeGeneratorEntity generatorEntity) throws Exception{
        String suffix = "DaoImpl.java";
        String path = localPath() +"/dao/impl/" + generatorEntity.getEntityName().toLowerCase() + generatorEntity.getEntityName() + suffix;
        String templateName = "DaoImpl.ftl";
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
        generatePager(packageName);
        generateGenericDao(packageName);
    }

    private void generatePager(String packageName){
        String suffix = ".java";
        String path = localPath() +"/type/Pager" +suffix;
        String templateName = "Pager.ftl";
        File generatorFile = new File(path);
        if (!generatorFile.exists()){
            File file = new File(localPath() + "/type");
            if (!file.exists()) {
                file.mkdirs();
            }
            String PagerPath = localPath() + "/type/Pager" +suffix;
            File pagerFile = new File(PagerPath);
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("packageName",packageName);
            generateFileByTemplate(templateName,pagerFile,dataMap);
        }
    }

    private void generateGenericDao(String packageName){
        String suffix = ".java";
        String path = localPath() +"/dao/GenericDao" +suffix;
        String templateName = "GenericDao.ftl";
        File generatorFile = new File(path);
        if (!generatorFile.exists()){
            File file = new File(localPath() + "/dao");
            file.mkdirs();
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("packageName",packageName);
            generateFileByTemplate(templateName,generatorFile,dataMap);
        }
    }

    private void generateFileByTemplate(String templateName,File file,Map<String,Object> dataMap){
        try {
            Object entityName = dataMap.get("entityName");
            if (entityName!=null){
                if (entityName.toString().equals("MpRoleAuthority") || entityName.toString().equals("MpUserRole")) {
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

    private String localPath(){
        String prjPath = System.getProperty("user.dir");
        prjPath = prjPath+"/src/main/java";
        String packAge = RevisitSpringBootApplication.class.getPackage().getName();
        packAge = packAge.substring(0,packAge.lastIndexOf("."));
        packAge = packageConvertPath(packAge);
        String classPath = prjPath+packAge;
        File file = new File(classPath);
        if (file.exists()){
            return file.getAbsolutePath();
        }else{
            boolean isCreate = file.mkdirs();
            if (isCreate){
                return file.getAbsolutePath();
            }else{
                return "";
            }
        }
    }

    private String packageName(){
        String packAge = RevisitSpringBootApplication.class.getPackage().getName();
        packAge = packAge.substring(0,packAge.lastIndexOf("."));
        if (packAge!=null||!packAge.equals("")){
            return packAge;
        }else{
            return "";
        }
    }

    private String packageConvertPath(String packageName) {
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }
}
