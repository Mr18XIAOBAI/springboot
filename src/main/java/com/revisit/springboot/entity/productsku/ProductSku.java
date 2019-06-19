/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: productSku
 * Author:   Revisit-Moon
 * Date:     2019/3/3 2:46 PM
 * Description: productsku.productSku
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/3/3 2:46 PM        1.0              描述
 */

package com.revisit.springboot.entity.productsku;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 〈productsku.productSku〉
 *
 * @author Revisit-Moon
 * @create 2019/3/3
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "product_sku")
public class ProductSku extends BasicEntity {
    @Column(length = 32)
    private String productId;                                   //商品ID
    private String skuName;                                     //规格名称
    private String skuUnit;                                     //规格单位
    private String skuAlbum;                                    //规格相册
    private String skuIcon;                                     //规格图标
    private String skuVideo;                                    //规格视频
    private int skuClickRate;                                   //规格点击率
    private int skuSales;                                       //规格销量
    @Lob
    private String skuDetail;                                   //规格详情
    private int skuStock;                                       //规格库存量
    private boolean online = false;                              //规格是否上架

    @Excel(name = "市场价(元)",orderNum = "2")
    private BigDecimal skuSalePrice;                            //规格市场价格
    @Excel(name = "普通会员价(元)",orderNum = "3")
    private BigDecimal skuOrdinaryMemberPrice;                  //规格普通会员价格
    @Excel(name = "Vip会员价(元)",orderNum = "4")
    private BigDecimal skuVipMemberPrice;                       //规格VIP会员价格
    @Excel(name = "合伙人价(元)",orderNum = "5")
    private BigDecimal skuPartnerPrice;                         //规格经销价格
    @Excel(name = "社区合伙人价(元)",orderNum = "6")
    private BigDecimal skuCommunityPartnerPrice;                //规格代理价格

    private int sortNumber;                                     //规格排序号
}
