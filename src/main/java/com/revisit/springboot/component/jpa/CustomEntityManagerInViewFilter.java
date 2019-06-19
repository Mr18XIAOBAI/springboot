/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CustomEntityManagerInViewFilter
 * Author:   Revisit-Moon
 * Date:     2019/2/4 8:26 PM
 * Description: jpa.CustomEntityManagerInViewFilter
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/4 8:26 PM        1.0              描述
 */

package com.revisit.springboot.component.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.stereotype.Component;

/**
 * 〈jpa.CustomEntityManagerInViewFilter〉
 *
 * @author Revisit-Moon
 * @create 2019/2/4
 * @since 1.0.0
 */
@Component
public class CustomEntityManagerInViewFilter {

    //解决使用JPA复杂查询如果存在懒加载报错问题
    @Bean
    public OpenEntityManagerInViewFilter openEntityManagerInViewFilter() {
        return new OpenEntityManagerInViewFilter();
    }
}
