/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AssembleProduct
 * Author:   Revisit-Moon
 * Date:     2019/5/4 10:31 PM
 * Description: assembleproduct.AssembleProduct
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/5/4 10:31 PM        1.0              描述
 */

package com.revisit.springboot.entity.assembleproduct;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.fastjson.annotation.JSONField;
import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 〈assembleproduct.AssembleProduct〉
 *
 * @author Revisit-Moon
 * @create 2019/5/4
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "assemble_product")
public class AssembleProduct extends BasicEntity {
    @Column(length = 32)
    private String productId;                                       //商品Id

    private Date beginTime;                                         //开始时间

    private Date endTime;                                           //结束时间

    private int fullSize;                                           //成团人数

    private String mainIcon;                                        //拼团商品主图标

    @Excel(name = "拼团商品名称")
    private String name;                                            //拼团商品名称

    private String video;                                           //拼团商品视频介绍

    @Excel(name = "拼团商品总浏览量")
    private int totalViews;                                         //拼团商品总浏览量

    @Excel(name = "拼团商品点击率")
    private int clickRate;                                          //拼团商品点击率

    @Excel(name = "拼团商品总销量")
    private int totalSales;                                         //拼团商品总销量

    private String status = "等待开始";                               //状态

    @JSONField(serialize = false)
    private String nowTeamId;                                       //当前团队id

    private boolean online = true;                                  //拼团商品是否在线

    private int sortNumber;                                         //拼团商品排序号

}
