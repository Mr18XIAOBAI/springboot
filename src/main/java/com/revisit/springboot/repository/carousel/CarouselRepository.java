package com.revisit.springboot.repository.carousel;

import com.revisit.springboot.entity.carousel.Carousel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* Carousel持久层接口类
* @author Revisit-Moon
* @date 2019-03-05 17:43:20
*/
public interface CarouselRepository extends JpaRepository<Carousel,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from Carousel carousel where carousel.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query(value = "UPDATE carousel SET sort_number = sort_number-1 WHERE sort_number > :sortNumber",nativeQuery = true)
    int allSortNumberMinusOneBySortNumber(@Param("sortNumber") int sortNumber);


    @Query("SELECT MAX(sortNumber) FROM Carousel")
    Integer findByCarouselMaxSortNumber();

    Carousel findBySortNumber(int sortNumber);
}