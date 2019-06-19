/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AssembleProductSku
 * Author:   Revisit-Moon
 * Date:     2019/5/5 10:19 AM
 * Description: assembleproductsku.AssembleProductSku
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/5/5 10:19 AM        1.0              描述
 */

package com.revisit.springboot.entity.assembleproductsku;


import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 〈assembleproductsku.AssembleProductSku〉
 *
 * @author Revisit-Moon
 * @create 2019/5/5
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "assemble_product_sku")
public class AssembleProductSku extends BasicEntity {

    @Column(length = 32)
    private String assembleProductId;                           //拼团商品ID
    private String skuName;                                     //规格名称
    private String skuUnit;                                     //规格单位
    private String skuAlbum;                                    //规格相册
    private String skuIcon;                                     //规格图标
    private String skuVideo;                                    //规格视频
    private int skuClickRate;                                   //规格点击率
    private int skuSales;                                       //规格销量
    @Lob
    private String skuDetail;                                   //规格详情
    private boolean isOnline = true;                            //规格是否上架
    private BigDecimal skuPrice;                                //规格拼团价
    private BigDecimal skuSalePrice;                            //规格原价
    private int sortNumber;                                     //规格排序号
}
