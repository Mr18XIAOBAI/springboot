/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: JavaBeanUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/26 11:44 AM
 * Description: JavaBeanUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/26 11:44 AM        1.0              描述
 */

package com.revisit.springboot.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.FatalBeanException;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 〈JavaBeanUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/26
 * @since 1.0.0
 */

public class JavaBeanUtil {
    private static List<String> getIgnoreProperties(){
        List<String> ignoreProperties = new ArrayList<>();
        ignoreProperties.add("id");
        ignoreProperties.add("createTime");
        ignoreProperties.add("UpdateTime");
        return ignoreProperties;
    }

    //合并值(目标值为空则使用源目标的值替换,不转换成其他对象,不排除任何字段)
    public static boolean copyProperties(Object source, Object target) {
        return copyProperties(source, target,null, null, null);
    }

    //合并值(目标值为空则判断是否覆盖空值,如果不覆盖则使用源目标的值替换,不转换成其他对象,不排除任何字段)
    public static boolean copyProperties(Object source, Object target,Boolean isCopyNull) {
        return copyProperties(source, target,isCopyNull, null, null);
    }

    //合并值(目标值如果在传入的排除列表中,则直接使用源目标值替换,不转换成其他对象,不排除任何字段)
    public static boolean copyProperties(Object source, Object target,@Nullable String ignorePropertiesStr) {
        return copyProperties(source, target, null,null, ignorePropertiesStr);
    }

    //合并值(目标值为空则判断是否覆盖空值,如果不覆盖则使用源目标的值替换,目标值如果在传入的排除列表中,则直接使用源目标值替换,不转换成其他对象,不排除任何字段)
    public static boolean copyProperties(Object source, Object target,Boolean isCopyNull, @Nullable String ignorePropertiesStr) {
        return copyProperties(source, target, isCopyNull,null, ignorePropertiesStr);
    }

    //合并值
    public static boolean copyProperties(Object source, Object target,Boolean isCopyNull,@Nullable Class<?> editable,
                                         @Nullable String ignorePropertiesStr) {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        boolean changed = false;

        Class<?> actualEditable = target.getClass();
        if (editable != null) {
            if (!editable.isInstance(target)) {
                throw new IllegalArgumentException("目标类: [" + target.getClass().getName()
                        + "] 不可以替换成指定类 [" + editable.getName() + "]");
            }
            actualEditable = editable;
        }

        List<String> ignoreList = getIgnoreProperties();
        if (StringUtils.isBlank(ignorePropertiesStr)){
            ignorePropertiesStr = "";
        }
        if (ignorePropertiesStr.contains(",")){
            String[] split = StringUtils.split(ignorePropertiesStr, ",");
            for (String s :split) {
                ignoreList.add(s);
            }
        }

        //获取一个包含目标对象的类名和所有参数名的PropertyDescriptor对象
        PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);
        //遍历替换开始
        for (PropertyDescriptor targetPd : targetPds) {

            //获取目标对象写方法set
            Method targetWriteMethod = targetPd.getWriteMethod();

            //判断此属性名是否存在排除列表
            if (targetWriteMethod != null) {

                //根据目标对象的属性名在源对象中获取一个PropertyDescriptor对象
                PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());

                //如果源对象中存在同名get|set方法
                if (sourcePd != null) {
                    //获取源对象读方法get
                    Method sourceReadMethod = sourcePd.getReadMethod();

                    //如果获取到且与目标对象的set方法的类型一致
                    if (sourceReadMethod != null && ClassUtils.isAssignable(targetWriteMethod.getParameterTypes()[0],
                            sourceReadMethod.getReturnType())) {
                        try {

                            //如果源对象读方法get不是public修饰,则允许读取
                            if (!Modifier.isPublic(sourceReadMethod.getDeclaringClass().getModifiers())) {
                                sourceReadMethod.setAccessible(true);
                            }

                            //如果目标读方法get不是public修饰,则允许读取
                            Method targetReadMethod = targetPd.getReadMethod();
                            if (!Modifier.isPublic(targetReadMethod.getDeclaringClass().getModifiers())) {
                                targetReadMethod.setAccessible(true);
                            }

                            //如果目标写方法set不是public修饰,则允许写入
                            if (!Modifier.isPublic(targetWriteMethod.getDeclaringClass().getModifiers())) {
                                targetWriteMethod.setAccessible(true);
                            }

                            //初始化源对象的这个属性
                            Object sourceValue = sourceReadMethod.invoke(source);

                            //初始化目标对象的这个属性
                            Object targetValue = targetReadMethod.invoke(target);

                            if (isCopyNull==null){
                                isCopyNull=false;
                            }

                            boolean tarIsNull = isNull(targetValue);
                            boolean srcIsNull = isNull(sourceValue);

                            //如果在排除字段数组中则直接将源目标值的属性替换
                            if ((ignoreList!=null&&!ignoreList.isEmpty())&&ignoreList.contains(targetPd.getName())){
                                targetWriteMethod.invoke(target, sourceValue);
                                continue;
                            }

                            //优先判断目标值是否为空
                            if (tarIsNull&&!isCopyNull){
                                if (!srcIsNull){
                                    targetWriteMethod.invoke(target, sourceValue);
                                    continue;
                                }
                            }

                        } catch (Throwable ex) {
                            System.out.println("不可以复制源目标属性值: " + targetPd.getName() + " 到目标对象,错误信息: " + ex.getMessage());
                        }
                    }
                }
            }
        }

        return changed;
    }

    public static boolean isNull(Object object){
        boolean empty = false;
        try {
            if (object == null
                    ||object.equals("")){
                empty = true;
            }
            if (!empty) {
                if (object.getClass().getTypeName().contains("Set")
                        || object.getClass().getTypeName().contains("List")) {
                    if (((Collection) object).isEmpty()) {
                        empty = true;
                    }
                }
            }
        }catch (Exception e){
            if (!empty) {
                if (object.getClass().getTypeName().contains("Map")) {
                    if (((Map) object).isEmpty()) {
                        empty = true;
                    }
                }
            }
        }
        return empty;
    }
}
