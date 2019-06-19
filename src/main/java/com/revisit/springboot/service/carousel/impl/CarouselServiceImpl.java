package com.revisit.springboot.service.carousel.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.carousel.Carousel;
import com.revisit.springboot.repository.carousel.CarouselRepository;
import com.revisit.springboot.service.carousel.CarouselService;
import com.revisit.springboot.utils.ExcelUtil;
import com.revisit.springboot.utils.JavaBeanUtil;
import com.revisit.springboot.utils.PageableUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Carousel逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-03-05 17:43:20
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class CarouselServiceImpl implements CarouselService {

    @Autowired
    private CarouselRepository carouselRepository;

    private final static Logger logger = LoggerFactory.getLogger(CarouselServiceImpl.class);

    @Override
    public JSONObject addCarousel(Carousel carousel) {
        logger.info("新增轮播图");

		String carouselPath = carousel.getCarouselPath();
		String carouselLink = carousel.getCarouselLink();
		if (StringUtils.isBlank(carouselPath)){
			return Result.fail(102,"参数错误","必填参数不能为空");
		}

		if(StringUtils.isBlank(carouselLink)){
			carousel.setCarouselLink("#");
		}

		Integer sortNumber = carouselRepository.findByCarouselMaxSortNumber();
		if (sortNumber==null){
			carousel.setSortNumber(0);
		}else{
			carousel.setSortNumber(sortNumber+1);
		}

		//保存此对象
		carousel = carouselRepository.save(carousel);

        if (StringUtils.isBlank(carousel.getId())){
            return Result.fail(110,"系统错误","新增轮播图失败,请联系管理员");
        }
		JSONObject carouselBean = (JSONObject)JSON.toJSON(carousel);
        return Result.success(200,"新增轮播图成功",carouselBean);
    }

    @Override
    public JSONObject deleteCarouselById(String id){

		logger.info("删除轮播图: " + id);

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				Carousel carousel = carouselRepository.findById(s).orElse(null);
				if(carousel!=null){
					carouselRepository.allSortNumberMinusOneBySortNumber(carousel.getSortNumber());
					ids.add(s);
    			}
			}
    	}else{
			Carousel carousel = carouselRepository.findById(id).orElse(null);
			if (carousel == null) {
				return Result.fail(102,"查询失败","轮播图对象不存在");
			}
			carouselRepository.allSortNumberMinusOneBySortNumber(carousel.getSortNumber());
			ids.add(id);
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","轮播图对象不存在");
		}

		int carouselRows = carouselRepository.deleteByIds(ids);

    	return Result.success(200,"删除轮播图成功","批量删除轮播图成功,共删除轮播图: " + carouselRows + " 个");
	}

    @Override
    public JSONObject updateCarouselById(String id,Carousel newCarousel){
		logger.info("更新轮播图: " + id);

		Carousel oldCarousel = carouselRepository.findById(id).orElse(null);
		if (oldCarousel==null){
			return Result.fail(102,"查询失败","轮播图对象不存在");
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "sortNumber";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldCarousel,newCarousel,ignoreProperties);

    	newCarousel = carouselRepository.save(newCarousel);

    	JSONObject carouselBean = (JSONObject)JSON.toJSON(newCarousel);

    	return Result.success(200,"更新成功",carouselBean);
    }

	@Override
    public JSONObject findCarouselById(String id){
    	logger.info("获取轮播图: " + id);

		Carousel carousel = carouselRepository.findById(id).orElse(null);

		if(carousel == null){
    		return Result.fail(102,"查询失败","轮播图对象不存在");
    	}

    	JSONObject carouselBean = (JSONObject)JSON.toJSON(carousel);

		return Result.success(200,"查询成功",carouselBean);
    }

	@Override
    public JSONObject findCarouselByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取轮播图列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}

		if (StringUtils.isBlank(orderBy)){
			orderBy = "sortNumber_ASC";
		}
		Page carouselListPage = findCarouselList(keyword,orderBy,beginTime,endTime,page,rows);

		if(carouselListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",carouselListPage.getTotalElements());
		result.put("page",carouselListPage.getNumber()+1);
		result.put("rows",carouselListPage.getSize());
		result.put("carouselList",carouselListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findCarouselList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page carouselListPage = carouselRepository.findAll(new Specification<Carousel>() {
			@Override
			public Predicate toPredicate(Root<Carousel> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
        		//指定查询对象
        		if (StringUtils.isNotBlank(keyword)) {
        			predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("carouselInfo"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("carouselPath"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("carouselLink"), "%" + keyword + "%")));
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

        if (!carouselListPage.hasContent()){
        	return null;
        }

		return carouselListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page carouselListPage = findCarouselList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<Carousel> carouselList = new ArrayList<>();

		if (carouselListPage!=null){
			carouselList.addAll(carouselListPage.getContent());
		}

		if (carouselList!=null&&!carouselList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(carouselList, "轮播图列表", "轮播图列表",Carousel.class, "轮播图列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

	@Override
	public JSONObject carouselSortUpOrDown(String id, String upOrDown) {
		Carousel carousel = carouselRepository.findById(id).orElse(null);
		if (carousel==null){
			return Result.fail(102,"查询失败","该轮播图对象不存在");
		}
		Integer sortNumber = carousel.getSortNumber();
		if (sortNumber==0&&upOrDown.equals("上移")){
			return Result.fail(102,"排序失败","该轮播图当前已在同级别分类的最顶端");
		}
		int maxSortNumber = carouselRepository.findByCarouselMaxSortNumber();

		if (sortNumber==maxSortNumber&&upOrDown.equals("下移")){
			return Result.fail(102,"排序失败","该轮播图当前已在同级别分类的最末端");
		}
		if (upOrDown.equals("上移")){
			sortNumber = sortNumber-1;
		}

		if (upOrDown.equals("下移")){
			sortNumber = sortNumber+1;
		}
		Carousel brothersCarousel = carouselRepository.findBySortNumber(sortNumber);

		if (brothersCarousel!=null){
			brothersCarousel.setSortNumber(carousel.getSortNumber());
			carousel.setSortNumber(sortNumber);
			carouselRepository.save(brothersCarousel);
			carousel = carouselRepository.save(carousel);
		}

		return Result.success(200,"排序成功",carousel);

	}
}