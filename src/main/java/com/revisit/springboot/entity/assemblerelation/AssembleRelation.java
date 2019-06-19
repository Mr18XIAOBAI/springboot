/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AssembleRelation
 * Author:   Revisit-Moon
 * Date:     2019/5/6 10:16 AM
 * Description: assemblerelation.AssembleRelation
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/5/6 10:16 AM        1.0              描述
 */

package com.revisit.springboot.entity.assemblerelation;

import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 〈assemblerelation.AssembleRelation〉
 *
 * @author Revisit-Moon
 * @create 2019/5/6
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "assemble_relation")
public class AssembleRelation extends BasicEntity {

    private String assembleProductId;                   //拼团商品ID

    private String userId;                              //用户ID

    private String realName;                            //真实名称

    private String weChatName;                          //微信名称

    private String avatar;                              //用户头像

    private String orderFormId;                         //订单ID

    private String status = "等待成团";                   //状态

    private String teamId;                              //成团ID

    private Date endTime;                               //结束时间
}
