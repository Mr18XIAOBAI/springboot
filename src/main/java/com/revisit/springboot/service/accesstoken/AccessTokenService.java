/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AccessTokenUtil
 * Author:   Revisit-Moon
 * Date:     2019/1/31 11:51 AM
 * Description: AccessTokenUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 11:51 AM        1.0              描述
 */

package com.revisit.springboot.service.accesstoken;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.entity.user.User;

import java.util.List;

/**
 * 〈AccessToken〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
public interface AccessTokenService {
    AccessToken findAccessTokenByIdAndIsValid(String id);
    List<Integer> findPriceLevelByAccessTokenAndIsValid(String id);
    AccessToken isValid(String authorization);
    AccessToken isValidTokenAndRoles(String authorization,String ...roleNames);
    AccessToken isValidTokenAndAuthorities(String authorization,String ...authorities);
    AccessToken isValidTokenAndValidAuthority(String authorization,String authorityName);
    AccessToken isValidTokenAndRole(String authorization,String roleName);
    AccessToken isSelf(String id, JSONObject target);
}
