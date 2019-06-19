/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CodeGeneratorDataBaseNameUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/25 5:36 PM
 * Description: CodeGeneratorDataBaseNameUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/25 5:36 PM        1.0              描述
 */

package com.revisit.springboot.component.codegenerator;

/**
 * 〈CodeGeneratorDataBaseNameUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/25
 * @since 1.0.0
 */

public class CodeGeneratorDataBaseNameUtil {
    /**
     * 将数据库（表、字段）转换以java命名方式帕斯卡或者骆驼
     * @param unberscoreName
     * @param isPascal       是否将首字母转化大写，true则转化为骆驼命名，false则转换为帕斯卡命名
     * @return 骆驼或帕斯卡命名字符串
     */
    public static String translate(String unberscoreName, boolean isPascal) {
        StringBuilder result = new StringBuilder();
        //从第一个字母
        if (unberscoreName != null && unberscoreName.length() !=0) {
            boolean flag = false;
            char firstChar = unberscoreName.charAt(0);  //得到首字母
            if (isPascal) {
                result.append(Character.toUpperCase(firstChar));
            } else {
                result.append(firstChar);
            }
            //从第二个字母以后开始
            for (int i = 1, length = unberscoreName.length(); i < length; i++) {
                char ch = unberscoreName.charAt(i);
                if ('_' == ch) {
                    flag = true;
                } else {
                    if (flag) { //标记上一个是下划线，就转化为大写。
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * 调用translate() 转换为帕斯卡命名
     * @param unberscoreName 数据库（表名、字段名）
     * @return
     */
    public static String toPascal(String unberscoreName) {
        return translate(unberscoreName, true);
    }

    /**
     * 调用translate() 转换为骆驼命名
     * @param unberscoreName 数据库（表名、字段名）
     * @return
     */
    public static String toCamel(String unberscoreName) {
        return translate(unberscoreName, false);
    }

    /**
     *
     * 将获取数据库类型转化为java类型
     * @param dbTypeName 实际的数据库类型
     * @return
     */
    public static String dbTypeChangeJavaType(String dbTypeName){
        String javaType=null;
        switch(dbTypeName){
            case "VARCHAR" :javaType="String";break;
            case "NVARCHAR" :javaType="String";break;
            case "CHAR" :javaType="String";break;
            case "NCHAR" :javaType="String";break;
            case "TEXT" :javaType="String";break;
            case "LONGTEXT" :javaType="String";break;
            case "BIGINT" :javaType="Long";break;
            case "INT" :javaType="Integer";break;
            case "DOUBLE" :javaType="Double";break;
            case "FLOAT" :javaType="Float";break;
            case "DATETIME" :javaType="Date";break;
            case "BIT" :javaType="Boolean";break;
            case "TINYINT" :javaType="Byte";break;
            case "SMALLINT" :javaType="Short";break;
            case "DECIMAL" :javaType="BigDecimal";break;
            default:javaType="String";break;
        }
        return javaType;
    }
}
