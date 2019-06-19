package com.revisit.springboot.service.assembleproductsku.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.assembleproduct.AssembleProduct;
import com.revisit.springboot.entity.assembleproductsku.AssembleProductSku;
import com.revisit.springboot.repository.assembleproduct.AssembleProductRepository;
import com.revisit.springboot.repository.assembleproductsku.AssembleProductSkuRepository;
import com.revisit.springboot.service.assembleproductsku.AssembleProductSkuService;
import com.revisit.springboot.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * ProductSku逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-03-03 15:28:24
 */
@Service
public class AssembleProductSkuServiceImpl implements AssembleProductSkuService {

    @Autowired
    private AssembleProductSkuRepository assembleProductSkuRepository;

    @Autowired
    private AssembleProductRepository assembleProductRepository;

    private final static Logger logger = LoggerFactory.getLogger(AssembleProductSkuServiceImpl.class);

    @Override
    public JSONObject addAssembleProductSku(AssembleProductSku assembleProductSku) {
        logger.info("新增拼团商品sku");


        AssembleProduct assembleProduct = assembleProductRepository.findById(assembleProductSku.getId()).orElse(null);
        if (assembleProduct==null){
            return Result.fail(102,"更新失败","拼团商品不存在");
        }

        Integer maxSkuSortNumber = assembleProductSkuRepository.findMaxSortNumberByAssembleProductId(assembleProduct.getId());
        if (maxSkuSortNumber==null) {
            assembleProductSku.setSortNumber(0);
        }else{
            assembleProductSku.setSortNumber(maxSkuSortNumber + 1);
        }

        //保存此对象
        assembleProductSku = assembleProductSkuRepository.save(assembleProductSku);

        if (StringUtils.isBlank(assembleProductSku.getId())){
            return Result.fail(110,"系统错误","新增拼团商品sku失败,请联系管理员");
        }
        // JSONObject productSkuBean = (JSONObject)JSON.toJSON(assembleProductSku);
        return Result.success(200,"新增拼团商品sku成功",assembleProductSku);
    }

    @Override
    public JSONObject deleteAssembleProductSkuById(String id){

        logger.info("删除拼团商品sku: " + id);

        List<String> ids = new ArrayList<>();
        if (StringUtils.contains(id,(","))){
            String[] split = StringUtils.split(id,",");
            for (String s :split) {
                AssembleProductSku assembleProductSku = assembleProductSkuRepository.findById(s).orElse(null);
                if (assembleProductSku!=null) {
                    assembleProductSkuRepository.allSortNumberMinusOneBySortNumberAndRelationId(assembleProductSku.getSortNumber(),assembleProductSku.getAssembleProductId());
                    List<String> oldProductSkuFileList = MoonUtil.getStringListByComma(assembleProductSku.getSkuAlbum());
                    String assembleProductSkuIcon = assembleProductSku.getSkuIcon();
                    if (StringUtils.isNotBlank(assembleProductSkuIcon)){
                        oldProductSkuFileList.add(assembleProductSkuIcon);
                    }
                    String assembleProductSkuVideo = assembleProductSku.getSkuVideo();
                    if (StringUtils.isNotBlank(assembleProductSkuVideo)){
                        oldProductSkuFileList.add(assembleProductSkuVideo);
                    }
                    FileUtil.deleteFileList(oldProductSkuFileList);
                    ids.add(s);
                }
            }
        }else{
            AssembleProductSku assembleProductSku = assembleProductSkuRepository.findById(id).orElse(null);
            if (assembleProductSku==null) {
                return Result.fail(102,"查询失败","拼团商品sku对象不存在");
            }
            assembleProductSkuRepository.allSortNumberMinusOneBySortNumberAndRelationId(assembleProductSku.getSortNumber(),assembleProductSku.getAssembleProductId());
            List<String> oldProductSkuImageList = MoonUtil.getStringListByComma(assembleProductSku.getSkuAlbum());
            FileUtil.deleteFileList(oldProductSkuImageList);
            ids.add(id);
        }

        if (ids == null || ids.isEmpty()) {
            return Result.fail(102,"查询失败","拼团商品sku对象不存在");
        }

        int assembleProductSkuRows = assembleProductSkuRepository.deleteByIds(ids);

        return Result.success(200,"删除拼团商品sku成功","批量删除拼团商品sku成功,共删除拼团商品sku: " + assembleProductSkuRows + " 个");
    }

    @Override
    public JSONObject updateAssembleProductSkuById(String id,AssembleProductSku newAssembleProductSku){
        logger.info("更新拼团商品sku: " + id);

        AssembleProductSku oldAssembleProductSku = assembleProductSkuRepository.findById(id).orElse(null);
        if (oldAssembleProductSku==null){
            return Result.fail(102,"查询失败","拼团商品sku对象不存在");
        }

        String newSkuAlbum = newAssembleProductSku.getSkuAlbum();
        String oldSkuAlbum = oldAssembleProductSku.getSkuAlbum();
        //删除旧相册
        if (StringUtils.isNotBlank(newSkuAlbum)) {
            if (StringUtils.isNotBlank(oldSkuAlbum)) {
                if (!newSkuAlbum.equals(oldSkuAlbum)) {
                    FileUtil.deleteOldFileListByNewFileList(oldSkuAlbum,newSkuAlbum);
                }
            }
        }

        //删除旧的图标
        String oldSkuIcon = oldAssembleProductSku.getSkuIcon();
        String newSkuIcon = oldAssembleProductSku.getSkuIcon();
        if (StringUtils.isNotBlank(newSkuIcon)) {
            if (StringUtils.isNotBlank(oldSkuIcon)) {
                if (!oldSkuIcon.equals(newSkuIcon)) {
                    FileUtil.deleteOldFileListByNewFileList(oldSkuIcon,newSkuIcon);
                }
            }
        }

        //删除旧的视频
        String oldSkuVideo = oldAssembleProductSku.getSkuVideo();
        String newSkuVideo = oldAssembleProductSku.getSkuVideo();
        if (StringUtils.isNotBlank(newSkuVideo)) {
            if (StringUtils.isNotBlank(oldSkuVideo)) {
                if (!oldSkuVideo.equals(newSkuVideo)) {
                    FileUtil.deleteOldFileListByNewFileList(oldSkuVideo,newSkuVideo);
                }
            }
        }

        //设置不更新字段,默认空值会被源对象替换
        String ignoreProperties = "assembleProductId,sortNumber";

        //开始合并对象
        JavaBeanUtil.copyProperties(oldAssembleProductSku,newAssembleProductSku,ignoreProperties);

        newAssembleProductSku = assembleProductSkuRepository.save(newAssembleProductSku);

        JSONObject productSkuBean = (JSONObject)JSON.toJSON(newAssembleProductSku);

        return Result.success(200,"更新成功",productSkuBean);
    }

    @Override
    public JSONObject findAssembleProductSkuById(String id){
        logger.info("获取拼团商品sku: " + id);

        AssembleProductSku assembleProductSku = assembleProductSkuRepository.findById(id).orElse(null);

        if(assembleProductSku == null){
            return Result.fail(102,"查询失败","拼团商品sku对象不存在");
        }
        assembleProductSku.setSkuClickRate(assembleProductSku.getSkuClickRate()+1);
        assembleProductSku = assembleProductSkuRepository.save(assembleProductSku);
        JSONObject assembleProductSkuBean = (JSONObject)JSON.toJSON(assembleProductSku);

        return Result.success(200,"查询成功",assembleProductSkuBean);
    }

    @Override
    public JSONObject findAssembleProductSkuByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

        logger.info("根据条件获取拼团商品sku列表: " + keyword);

        //如果当前页数是空,则默认第一页
        if (page==null) {
            page = 1;
        }
        //如果需要查询条数为空,则默认查询10条
        if (rows==null){
            rows=10;
        }
        Page assembleProductSkuListPage = findAssembleProductSkuList(keyword,orderBy,beginTime,endTime,page,rows);

        if(assembleProductSkuListPage==null){
            return Result.fail(102,"参数有误","获取不到相关数据");
        }

        JSONObject result = new JSONObject();
        result.put("rowsTotal",assembleProductSkuListPage.getTotalElements());
        result.put("page",assembleProductSkuListPage.getNumber()+1);
        result.put("rows",assembleProductSkuListPage.getSize());
        result.put("productSkuList",assembleProductSkuListPage.getContent());
        return Result.success(200,"查询成功",result);
    }

    private Page findAssembleProductSkuList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
        //分页插件
        PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
        Pageable pageable = pageableUtil.getPageable();
        Page assembleProductSkuListPage = assembleProductSkuRepository.findAll(new Specification<AssembleProductSku>() {
            @Override
            public Predicate toPredicate(Root<AssembleProductSku> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                //指定查询对象
                if (StringUtils.isNotBlank(keyword)) {
                    predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("id"), "%" + keyword + "%")
                            , criteriaBuilder.like(root.get("id"), "%" + keyword + "%")
                            , criteriaBuilder.like(root.get("id"), "%" + keyword + "%")));
                }

                if (beginTime != null) {
                    predicateList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get("createTime"), beginTime)));
                }

                if (endTime != null) {
                    predicateList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("createTime"), endTime)));
                }
                return query.where(predicateList.toArray(new Predicate[predicateList.size()])).getRestriction();
            }
        }, pageable);

        if (!assembleProductSkuListPage.hasContent()){
            return null;
        }

        return assembleProductSkuListPage;
    }

    @Override
    public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page assembleProductSkuListPage = findAssembleProductSkuList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

        List<AssembleProductSku> assembleProductSkuList = new ArrayList<>();

        if (assembleProductSkuListPage!=null){
            assembleProductSkuList.addAll(assembleProductSkuListPage.getContent());
        }

        if (assembleProductSkuList!=null&&!assembleProductSkuList.isEmpty()) {
            //导出操作
            ExcelUtil.exportExcel(assembleProductSkuList, "拼团商品sku列表", "拼团商品sku列表",AssembleProductSku.class, "拼团商品sku列表.xls", response);
        }else {
            try {
                response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public JSONObject assembleProductSkuSortUpOrDown(String id, String upOrDown) {
        AssembleProductSku assembleProductSku = assembleProductSkuRepository.findById(id).orElse(null);

        if (assembleProductSku==null){
            return Result.fail(102,"查询失败","该拼团商品sku对象不存在");
        }
        Integer sortNumber = assembleProductSku.getSortNumber();
        if (sortNumber==0&&upOrDown.equals("上移")){
            return Result.fail(102,"排序失败","该拼团商品sku当前已在同级别分类的最顶端");
        }

        Integer maxSortNumber = assembleProductSkuRepository.findMaxSortNumberByAssembleProductId(assembleProductSku.getAssembleProductId());

        if (sortNumber==maxSortNumber&&upOrDown.equals("下移")){
            return Result.fail(102,"排序失败","该拼团商品sku当前已在同级别分类的最末端");
        }
        if (upOrDown.equals("上移")){
            sortNumber = sortNumber-1;
        }

        if (upOrDown.equals("下移")){
            sortNumber = sortNumber+1;
        }
        AssembleProductSku brothersProductSku = assembleProductSkuRepository.findByAssembleProductIdAndSortNumber(assembleProductSku.getAssembleProductId(), sortNumber);
        if (brothersProductSku!=null){
            brothersProductSku.setSortNumber(assembleProductSku.getSortNumber());
            assembleProductSku.setSortNumber(sortNumber);
            assembleProductSkuRepository.save(brothersProductSku);
            assembleProductSku = assembleProductSkuRepository.save(assembleProductSku);
        }
        return Result.success(200,"排序成功",assembleProductSku);
    }

    // /**
    //  * 〈扣除拼团商品库存〉
    //  *
    //  * @param skuIdAndNumber
    //  * @return:
    //  * @since: 1.0.0
    //  * @Author: Revisit-Moon
    //  * @Date: 2019/4/14 4:16 PM
    //  */
    // @Override
    // public boolean deductStock(Map<String, Object> skuIdAndNumber) {
    //     if (skuIdAndNumber==null||skuIdAndNumber.isEmpty()) {
    //         return false;
    //     }
    //     Set<String> productIdSet = new HashSet<>();
    //     List<AssembleProductSku> productSkuList = new ArrayList<>();
    //     for (Map.Entry entry :skuIdAndNumber.entrySet()) {
    //         String skuId = entry.getKey().toString();
    //         AssembleProductSku sku = productSkuRepository.findById(skuId).orElse(null);
    //         if (sku==null){
    //             logger.info("扣除库存失败,sku不存在: "+skuId);
    //             continue;
    //         }
    //         int buyNumber = Integer.parseInt(entry.getValue().toString());
    //
    //         //扣除库存
    //         sku.setSkuStock(sku.getSkuStock()-buyNumber);
    //
    //         //增加销量
    //         sku.setSkuSales(sku.getSkuSales()+buyNumber);
    //         productSkuList.add(sku);
    //         productIdSet.add(sku.getProductId());
    //     }
    //
    //     if (productSkuList.isEmpty()){
    //         return false;
    //     }
    //     productSkuList = productSkuRepository.saveAll(productSkuList);
    //
    //     if (productSkuList!=null&&!productSkuList.isEmpty()){
    //         return true;
    //     }else {
    //         return false;
    //     }
    // }


    //计算价格
    // @Override
    // public BigDecimal whatPriceByRoleNameAndSku(int priceLevel,AssembleProductSku sku){
    //     switch (priceLevel){
    //         case 0:{
    //             return sku.getSkuNoMemberPrice();
    //         }
    //         case 1:{
    //             return sku.getSkuYearCarPrice();
    //         }
    //         case 2:{
    //             return sku.getSkuYearCarPrice();
    //         }
    //         case 3:{
    //             return sku.getSkuMemberPrice();
    //         }
    //         case 4:{
    //             return sku.getSkuDealerPrice();
    //         }
    //         case 5:{
    //             return sku.getSkuProxyPrice();
    //         }
    //         case 6:{
    //             return sku.getSkuPartnerPrice();
    //         }
    //         default:{
    //             return sku.getSkuSalePrice();
    //         }
    //     }
    // }
}