package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品
     * @param dishDTO
     */
    void saveDish(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult dishPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     */
    void deleteDishByIds(List<Long> ids);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    DishVO getDishById(Long id);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void updateDishAndFlavor(DishDTO dishDTO);
}