/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: ProductClass
 * Author:   Revisit-Moon
 * Date:     2019/3/3 7:11 PM
 * Description: productclass.ProductClass
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/3/3 7:11 PM        1.0              描述
 */

package com.revisit.springboot.entity.productclass;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 〈productclass.ProductClass〉
 *
 * @author Revisit-Moon
 * @create 2019/3/3
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "product_class")
public class ProductClass extends BasicEntity {
    @Excel(name = "分类名")
    private String className;                       //分类名
    @Column(length = 32)
    private String fatherClassId;                   //父分类ID
    private String classIcon;                       //分类图标
    private int sortNumber;                       //排序号
    @Excel(name = "分类级别",replace = {"一级分类_1","二级分类_2","三级分类_3"})
    private int classLevel = 1;                 //分类级别
    @Excel(name = "是否包含子分类",replace = {"是_1","否_2"})
    private Boolean haveSonClass = false;           //是否有子分类(默认没有)
    @Excel(name = "子分类数量",replace = {"没有子分类_0"})
    private int sonClassCount;              //子分类总数

}
