package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 通过菜品id查询套餐
     * @param dishId
     * @return
     */
    @Select("select sky_take_out.setmeal_dish.dish_id from sky_take_out.setmeal_dish where dish_id = #{dishId}")
    List<Long> getSetmealIdByDishId(Long dishId);
}
