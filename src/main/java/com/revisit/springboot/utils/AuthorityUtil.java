/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AuthorityUtil
 * Author:   Revisit-Moon
 * Date:     2019/1/31 4:58 PM
 * Description: AuthorityUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 4:58 PM        1.0              描述
 */

package com.revisit.springboot.utils;

import com.revisit.springboot.entity.user.Authority;
import com.revisit.springboot.repository.user.AuthorityRepository;
import com.revisit.springboot.repository.user.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 〈AuthorityUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */

public class AuthorityUtil {
    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RoleRepository roleRepository;

    public static final String TOURIST = "游客";
    public static final String ORDINARY_MEMBER = "普通会员";
    public static final String VIP_MEMBER = "VIP会员";
    public static final String PARTNER = "合伙人";
    public static final String COMMUNITY_PARTNER= "社区合伙人";
    public static final String MEMBER_ADMIN = "会员管理员";
    public static final String SHOP_ADMIN = "商家管理员";
    public static final String SUPER_ADMIN = "超级管理员";
    public static final String SYSTEM_ADMIN = "系统管理员";


    public static final String ORDER_FORM_ADD = "新增订单";
    public static final String ORDER_FORM_DELETE = "删除订单";
    public static final String ORDER_FORM_UPDATE = "修改订单";
    public static final String ORDER_FORM_READ = "读取订单";

    public static final String ADDRESS_ADD = "新增地址";
    public static final String ADDRESS_DELETE = "删除地址";
    public static final String ADDRESS_UPDATE = "修改地址";
    public static final String ADDRESS_READ = "读取地址";

    public static final String SHOP_ADD = "新增店铺";
    public static final String SHOP_DELETE = "删除店铺";
    public static final String SHOP_UPDATE = "修改店铺";
    public static final String SHOP_READ = "读取店铺";

    public static final String USER_ADD = "新增用户";
    public static final String USER_DELETE = "删除用户";
    public static final String USER_UPDATE = "修改用户";
    public static final String USER_READ = "读取用户";

    public static final String PRODUCT_ADD = "新增产品";
    public static final String PRODUCT_DELETE = "删除产品";
    public static final String PRODUCT_UPDATE = "修改产品";
    public static final String PRODUCT_READ = "读取产品";

    public static final String CLASS_ADD = "新增分类";
    public static final String CLASS_DELETE = "删除分类";
    public static final String CLASS_UPDATE = "修改分类";
    public static final String CLASS_READ = "读取分类";

    public static final String RESOURCES_ADD = "新增资源";
    public static final String RESOURCES_DELETE = "删除资源";
    public static final String RESOURCES_UPDATE = "修改资源";
    public static final String RESOURCES_READ = "读取资源";

    public static final String WALLET_ADD = "新增钱包";
    public static final String WALLET_DELETE = "删除钱包";
    public static final String WALLET_UPDATE = "修改钱包";
    public static final String WALLET_READ = "读取钱包";

    public static final String SHOPPING_CART_ADD = "新增购物车";
    public static final String SHOPPING_CART_DELETE = "删除购物车";
    public static final String SHOPPING_CART_UPDATE = "修改购物车";
    public static final String SHOPPING_CART_READ = "读取购物车";

    public static final String SETTING_UPDATE = "修改系统设置";
    public static final String SETTING_READ = "读取系统设置";


    /**
     * 〈定义游客权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generateTouristAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_ADD",ORDER_FORM_ADD);
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);

    }

    /**
     * 〈定义普通会员权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generateOrdinaryMemberAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_ADD",ORDER_FORM_ADD);
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);
    }


    /**
     * 〈定义VIP会员权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generateVipMenberAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_ADD",ORDER_FORM_ADD);
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);
    }

    /**
     * 〈定义合伙人权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generatePartnerAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_ADD",ORDER_FORM_ADD);
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);
    }

    /**
     * 〈定义社区合伙人权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generateCommunityPartnerAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_ADD",ORDER_FORM_ADD);
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("PRODUCT_ADD",PRODUCT_ADD);
        authorityMap.put("PRODUCT_DELETE",PRODUCT_DELETE);
        authorityMap.put("PRODUCT_UPDATE",PRODUCT_UPDATE);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("SHOP_ADD",SHOP_ADD);
        authorityMap.put("SHOP_DELETE",SHOP_DELETE);
        authorityMap.put("SHOP_UPDATE",SHOP_UPDATE);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);
    }

    /**
     * 〈定义会员管理员权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generateMemberAdminAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        // authorityMap.put("SHOP_ADD",SHOP_ADD);
        // authorityMap.put("SHOP_DELETE",SHOP_DELETE);
        // authorityMap.put("SHOP_UPDATE",SHOP_UPDATE);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("USER_ADD",USER_ADD);
        authorityMap.put("USER_DELETE",USER_DELETE);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("PRODUCT_ADD",PRODUCT_ADD);
        authorityMap.put("PRODUCT_DELETE",PRODUCT_DELETE);
        authorityMap.put("PRODUCT_UPDATE",PRODUCT_UPDATE);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_DELETE",RESOURCES_DELETE);
        authorityMap.put("RESOURCES_UPDATE",RESOURCES_UPDATE);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);
    }

    /**
     * 〈定义店铺管理员权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generateShopAdminAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_ADD",ORDER_FORM_ADD);
        authorityMap.put("ORDER_FORM_DELETE",ORDER_FORM_DELETE);
        authorityMap.put("ORDER_FORM_UPDATE",ORDER_FORM_UPDATE);
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        authorityMap.put("SHOP_ADD",SHOP_ADD);
        authorityMap.put("SHOP_DELETE",SHOP_DELETE);
        authorityMap.put("SHOP_UPDATE",SHOP_UPDATE);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("PRODUCT_ADD",PRODUCT_ADD);
        authorityMap.put("PRODUCT_DELETE",PRODUCT_DELETE);
        authorityMap.put("PRODUCT_UPDATE",PRODUCT_UPDATE);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("CLASS_ADD",CLASS_ADD);
        authorityMap.put("CLASS_DELETE",CLASS_DELETE);
        authorityMap.put("CLASS_UPDATE",CLASS_UPDATE);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_DELETE",RESOURCES_DELETE);
        authorityMap.put("RESOURCES_UPDATE",RESOURCES_UPDATE);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);
    }

    /**
     * 〈定义超级管理员权限〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/1/31 4:59 PM
     */
    public static Set<Authority> generateSuperAdminAuthority(){
        Map<String,String> authorityMap = new HashMap<>();
        authorityMap.put("ORDER_FORM_ADD",ORDER_FORM_ADD);
        authorityMap.put("ORDER_FORM_DELETE",ORDER_FORM_DELETE);
        authorityMap.put("ORDER_FORM_UPDATE",ORDER_FORM_UPDATE);
        authorityMap.put("ORDER_FORM_READ",ORDER_FORM_READ);
        authorityMap.put("SHOP_ADD",SHOP_ADD);
        authorityMap.put("SHOP_DELETE",SHOP_DELETE);
        authorityMap.put("SHOP_UPDATE",SHOP_UPDATE);
        authorityMap.put("SHOP_READ",SHOP_READ);
        authorityMap.put("SHOPPING_CART_ADD",SHOPPING_CART_ADD);
        authorityMap.put("SHOPPING_CART_DELETE",SHOPPING_CART_DELETE);
        authorityMap.put("SHOPPING_CART_UPDATE",SHOPPING_CART_UPDATE);
        authorityMap.put("SHOPPING_CART_READ",SHOPPING_CART_READ);
        authorityMap.put("ADDRESS_ADD",ADDRESS_ADD);
        authorityMap.put("ADDRESS_DELETE",ADDRESS_DELETE);
        authorityMap.put("ADDRESS_UPDATE",ADDRESS_UPDATE);
        authorityMap.put("ADDRESS_READ",ADDRESS_READ);
        authorityMap.put("USER_ADD",USER_ADD);
        authorityMap.put("USER_DELETE",USER_DELETE);
        authorityMap.put("USER_UPDATE",USER_UPDATE);
        authorityMap.put("USER_READ",USER_READ);
        authorityMap.put("PRODUCT_ADD",PRODUCT_ADD);
        authorityMap.put("PRODUCT_DELETE",PRODUCT_DELETE);
        authorityMap.put("PRODUCT_UPDATE",PRODUCT_UPDATE);
        authorityMap.put("PRODUCT_READ",PRODUCT_READ);
        authorityMap.put("CLASS_ADD",CLASS_ADD);
        authorityMap.put("CLASS_DELETE",CLASS_DELETE);
        authorityMap.put("CLASS_UPDATE",CLASS_UPDATE);
        authorityMap.put("CLASS_READ",CLASS_READ);
        authorityMap.put("RESOURCES_ADD",RESOURCES_ADD);
        authorityMap.put("RESOURCES_DELETE",RESOURCES_DELETE);
        authorityMap.put("RESOURCES_UPDATE",RESOURCES_UPDATE);
        authorityMap.put("RESOURCES_READ",RESOURCES_READ);
        authorityMap.put("WALLET_ADD",WALLET_ADD);
        authorityMap.put("WALLET_DELETE",WALLET_DELETE);
        authorityMap.put("WALLET_UPDATE",WALLET_UPDATE);
        authorityMap.put("WALLET_READ",WALLET_READ);
        authorityMap.put("SETTING_UPDATE",SETTING_UPDATE);
        authorityMap.put("SETTING_READ",SETTING_READ);
        return generateAuthority(authorityMap);
    }


    /**
     * 〈生成权限集合〉
     *
     * @param authorityMap
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019-06-13 10:52
     */
    private static Set<Authority> generateAuthority(Map<String,String> authorityMap){
        Set<Authority> authorities = new HashSet<>();
        for (Map.Entry map : authorityMap.entrySet()) {
            Authority authority = new Authority();
            authority.setAuthorityName(map.getKey().toString());
            authority.setDescription(map.getValue().toString());
            authorities.add(authority);
        }
        return authorities;
    }

    /**
     * 〈是否存在某角色〉
     *
     * @param roleName
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019-06-13 15:29
     */
    public static boolean existOrdinaryRole(String roleName){
        String[] roleNameArray = new String[]{AuthorityUtil.TOURIST,AuthorityUtil.ORDINARY_MEMBER,AuthorityUtil.VIP_MEMBER
                ,AuthorityUtil.PARTNER,AuthorityUtil.COMMUNITY_PARTNER,AuthorityUtil.SHOP_ADMIN
        };
        Set<String> sets = new HashSet<String>(Arrays.asList(roleNameArray));
        return sets.contains(roleName);
    }
}
