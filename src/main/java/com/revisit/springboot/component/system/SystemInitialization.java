/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: SystemInitialization
 * Author:   Revisit-Moon
 * Date:     2019/1/31 11:33 AM
 * Description: system.SystemInitialization
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 11:33 AM        1.0              描述
 */

package com.revisit.springboot.component.system;

import com.revisit.springboot.entity.system.SystemSetting;
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.system.SystemSettingRepository;
import com.revisit.springboot.repository.token.AccessTokenRepository;
import com.revisit.springboot.repository.user.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * 〈system.SystemInitialization〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class SystemInitialization implements ApplicationRunner {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    private final Logger logger = LoggerFactory.getLogger(SystemInitialization.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("系统初始化中...");
        SystemSetting systemSetting = systemSettingRepository.findSystemSetting();
        if (systemSetting==null){
            systemSettingRepository.save(new SystemSetting());
        }
        initSystemAdmin();
        cleanNotUserToken();
        logger.info("系统初始化完成...");
    }

    private boolean initSystemAdmin(){
        User systemAdmin = userRepository.findByUserName("MeiGuoTec");
        if (systemAdmin==null){
            systemAdmin = new User();
            systemAdmin.setUserName("MeiGuoTec");
            systemAdmin.setPassword(DigestUtils.md5Hex("888888"));
            Set<Role> roles = new HashSet<>();
            Role role = new Role();
            role.setRoleName("系统管理员");
            role.setDescription("拥有系统最高权限");
            roles.add(role);
            systemAdmin.setRoles(roles);
            userRepository.save(systemAdmin);
        }else{
            systemAdmin.setUserName("MeiGuoTec");
            systemAdmin.setPassword(DigestUtils.md5Hex("888888"));
            userRepository.saveAndFlush(systemAdmin);
        }
        return true;
    }

    private void cleanNotUserToken(){
        logger.info("删除没有对应的user的token: "+accessTokenRepository.findNotUserToken()+" 个");
    }
}
