package com.revisit.springboot.controller.addressmanage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.addressmanage.AddressManage;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.addressmanage.AddressManageService;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
* AddressManage访问控制层
* @author Revisit-Moon
* @date 2019-04-15 21:11:34
*/
@RestController
@RequestMapping("/api/addressManage")
public class AddressManageController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private AddressManageService addressManageService;

    private final static Logger logger = LoggerFactory.getLogger(AddressManageController.class);

    /**
     * 新增AddressManage
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addAddressManage(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValid(authorization)==null){
           return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        AccessToken token = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        AddressManage addressManage;

        try {
            addressManage = JSON.toJavaObject(param, AddressManage.class);
            if (addressManage == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }

            String address = addressManage.getAddress();
            if (StringUtils.isBlank(address)){
                return Result.fail(102,"参数错误","地址不能为空");
            }

            String consignee = addressManage.getConsignee();
            if (StringUtils.isBlank(consignee)){
                return Result.fail(102,"参数错误","联系人不能为空");
            }

            String contactMobile = addressManage.getContactMobile();
            if (StringUtils.isBlank(contactMobile)){
                return Result.fail(102,"参数错误","联系电话不能为空");
            }
            String province = addressManage.getProvince();
            if (StringUtils.isBlank(province)){
                return Result.fail(102,"参数错误","省级地址不能为空");
            }

            String city = addressManage.getCity();
            if (StringUtils.isBlank(city)){
                return Result.fail(102,"参数错误","市级地址不能为空");
            }

            String district = addressManage.getDistrict();
            if (StringUtils.isBlank(district)){
                return Result.fail(102,"参数错误","区县级地址不能为空");
            }

            addressManage.setUserId(token.getUserId());
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return addressManageService.addAddressManage(addressManage);
    }

    /**
     * 根据ID删除AddressManage
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteAddressManage(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.ADDRESS_DELETE);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return addressManageService.deleteAddressManageById(id,token.getUserId());
    }

    /**
     * 根据新AddressManage更新id已存在的AddressManage
     * @param id,newAddressManage
     */
    @PutMapping(value = "/{id}")
    @ResponseBody
    public JSONObject updateAddressManage(@PathVariable("id") String id,@RequestBody JSONObject param){
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ADDRESS_UPDATE);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        AddressManage newAddressManage;
        try {
            newAddressManage = JSON.toJavaObject(param, AddressManage.class);
            if (newAddressManage == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
            newAddressManage.setUserId(token.getUserId());
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }
        return addressManageService.updateAddressManageById(id,newAddressManage);
    }

    /**
     * 根据id获取AddressManage
     * @param userId
     */
    @GetMapping(value = "/{userId}")
    public @ResponseBody JSONObject getAddressManageByUserId(@PathVariable("userId") String userId){
        if (userId==null||StringUtils.isBlank(userId)||userId.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        String authorization = request.getHeader("Authorization");

        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ADDRESS_READ);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        if (!userId.equals(token.getUserId())){
            return Result.fail(102,"参数错误","非法操作");
        }

        return addressManageService.findAddressManageByUserId(userId);
    }

    /**
     * 根据id获取AddressManage
     * @param id
     */
    @GetMapping(value = "/info/{id}")
    public @ResponseBody JSONObject getAddressManageById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");

        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ADDRESS_READ);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return addressManageService.findAddressManageById(id,token.getUserId());
    }

    /**
     * 分页获取AddressManage列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findAddressManageByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");
        String userId = param.getString("userId");

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return addressManageService.findAddressManageByList(keyword,userId,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 分页获取AddressManage列表
     * @param param
     */
    @PostMapping(value = "/myList")
    public @ResponseBody JSONObject findMyAddressManageByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");

        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ADDRESS_READ);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");
        String userId = token.getUserId();
        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return addressManageService.findAddressManageByList(keyword,userId,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出AddressManageExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String userId,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出AddressManage列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出AddressManageExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        addressManageService.exportExcel(keyword,userId,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

}