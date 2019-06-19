/**
 * Copyright (C), 2015-2018, 美果科技有限公司
 * FileName: MysqlDialectCharSet
 * Author:   Revisit-Moon
 * Date:     2018/11/13 2:47 PM
 * Description: component.MysqlDialectCharSet
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2018/11/13 2:47 PM        1.0              描述
 */

package com.revisit.springboot.component;

import org.hibernate.dialect.MySQL5Dialect;

/**
 * 〈component.MysqlDialectCharSet〉
 *
 * @author Revisit-Moon
 * @create 2018/11/13
 * @since 1.0.0
 */

public class MysqlDialectCharSet extends MySQL5Dialect {
    @Override
    public String getTableTypeString() {
        return "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
    }
}
