/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: Carousel
 * Author:   Revisit-Moon
 * Date:     2019/3/5 5:37 PM
 * Description: carousel.Carousel
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/3/5 5:37 PM        1.0              描述
 */

package com.revisit.springboot.entity.carousel;

import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 〈轮播图实体〉
 *
 * @author Revisit-Moon
 * @create 2019/3/5
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "carousel")
public class Carousel extends BasicEntity {
    private String carouselPath;                        //轮播图路径
    private String carouselInfo;                        //轮播图提示
    private String carouselLink;                        //轮播图链接
    private Integer sortNumber;                         //轮播图序号
}
