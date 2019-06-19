/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CodeGeneratorEntity
 * Author:   Revisit-Moon
 * Date:     2019/2/25 5:33 PM
 * Description: CodeGeneratorEntity
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/25 5:33 PM        1.0              描述
 */

package com.revisit.springboot.component.codegenerator;

import java.util.List;
import java.util.Map;

/**
 * 〈CodeGeneratorEntity〉
 *
 * @author Revisit-Moon
 * @create 2019/2/25
 * @since 1.0.0
 */

public class CodeGeneratorEntity {
    private String author;                                  //作者
    private String date;                                    //创建时间
    private String entityName;                              //实体名称
    private String packageName;                             //所在包名
    private String tableName;                               //数据库表名称
    private List<Map<String,Object>> entityColumnList;      //实体属性列表

    public CodeGeneratorEntity() {

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Map<String, Object>> getEntityColumnList() {
        return entityColumnList;
    }

    public void setEntityColumnList(List<Map<String, Object>> entityColumnList) {
        this.entityColumnList = entityColumnList;
    }
}
