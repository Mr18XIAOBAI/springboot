/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: Wallet
 * Author:   Revisit-Moon
 * Date:     2019/4/20 6:37 PM
 * Description: wallet.Wallet
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/4/20 6:37 PM        1.0              描述
 */

package com.revisit.springboot.entity.wallet;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 〈钱包实体〉
 *
 * @author Revisit-Moon
 * @create 2019/4/20
 * @since 1.0.0
 */

@Data
@Entity
@Table(name = "wallet")
public class Wallet extends BasicEntity {

    @Column(unique = true,nullable = false,length = 32)
    private String userId;                                              //钱包对应的用户ID

    @Excel(name = "余额")
    private BigDecimal price = new BigDecimal(0);        //可提现金额

     // @Excel(name = "不可提现余额")
     // private BigDecimal noWithdrawPrice = new BigDecimal(0);         //不可提现金额

     // private BigDecimal totalIncome = new BigDecimal(0);             //总收入

}
