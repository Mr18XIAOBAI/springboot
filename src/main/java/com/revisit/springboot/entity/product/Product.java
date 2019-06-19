package com.revisit.springboot.entity.product;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <Product实体类>
 * @author Revisit-Moon
 * @date 2019-02-28 18:33:01
 */
@Data
@Entity
@Table(name="product")
public class Product extends BasicEntity {

    @Column(length = 32)
    private String shopId;                              //店铺ID

    @Column(length = 32)
    private String productClassId;                      //商品分类ID

    private String productMainIcon;                     //商品主图标

    @Excel(name = "商品名称")
    private String productName;                         //商品名称

    @Excel(name = "商品标题")
    private String productTitle;                        //商品标题

    private String productVideo;                        //商品视频介绍

    @Excel(name = "商品总浏览量")
    private int productTotalViews;                      //商品总浏览量

    @Excel(name = "商品点击率")
    private int productClickRate;                       //商品点击率

    @Excel(name = "商品总销量")
    private int productTotalSales;                      //商品总销量

    private int sortNumber;                             //商品排序号

    @Excel(name = "是否上架",replace = {"否_0","是_1"})
    private boolean online = false;                     //商品上下架

    private String status = "待审核";                    //审核状态

    private String failureReason;                       //失败原因
}