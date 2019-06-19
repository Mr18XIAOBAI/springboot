package com.revisit.springboot.entity.partyinfo;

import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * describe:
 *
 * @author xxx
 * @date 2019/06/19
 */
@Data
@Entity
@Table(name = "party_info")
public class PartyInfo extends BasicEntity {
    private Integer type;                   //类型
    private String name;                    //名字
    @Lob
    private String content;                 //内容
}
