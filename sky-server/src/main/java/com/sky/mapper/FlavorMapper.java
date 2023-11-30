package com.sky.mapper;

import com.sky.entity.DishFlavor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlavorMapper {
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 通过菜品id删除口味
     * @param dishIds
     */
    void deleteByDishId(List<Long> dishIds);
}
