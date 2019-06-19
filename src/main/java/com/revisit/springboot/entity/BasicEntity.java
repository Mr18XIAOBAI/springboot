/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: basicEntity
 * Author:   Revisit-Moon
 * Date:     2019/1/28 2:32 PM
 * Description: basicEntity
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/28 2:32 PM        1.0              描述
 */

package com.revisit.springboot.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 〈基础实体类,所有实体类应继承〉
 *
 * @author Revisit-Moon
 * @create 2019/1/28
 * @since 1.0.0
 */
@MappedSuperclass
@Data
public class BasicEntity implements Serializable {
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 32)
    @GenericGenerator(name = "system-uuid", strategy = "com.revisit.springboot.component.uuid.CustomizeUUIDGenerate" )
    @GeneratedValue(generator = "system-uuid")
    private String id;                          //主键

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss",orderNum = "1")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name="create_time", nullable=false, length=19)
    private Date createTime;                    //创建时间

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name="update_time", nullable=false, length=19)
    private Date updateTime;                    //更新时间

    public BasicEntity() {
        Date now = new Date();
        this.createTime = now;
        this.updateTime = now;
    }
}
