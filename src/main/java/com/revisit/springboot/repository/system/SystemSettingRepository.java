/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: SystemSettingRepository
 * Author:   Revisit-Moon
 * Date:     2019/1/31 11:08 AM
 * Description: system.SystemSettingRepository
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 11:08 AM        1.0              描述
 */

package com.revisit.springboot.repository.system;

import com.revisit.springboot.entity.system.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 〈system.SystemSettingRepository〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
public interface SystemSettingRepository extends JpaRepository<SystemSetting,String>,JpaSpecificationExecutor {
    @Query("from SystemSetting")
    SystemSetting findSystemSetting();
}
