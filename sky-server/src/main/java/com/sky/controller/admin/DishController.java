package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
public class DishController {
    private static final String KEY= "dish_*";
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result saveDish(@RequestBody DishDTO dishDTO){
        dishService.saveDish(dishDTO);
        //清理相关分类菜品
        cleanRedis(String.valueOf(dishDTO.getCategoryId()));
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> dishPage(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.dishPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids){
        log.info("批量删除菜品：{}",ids);
        dishService.deleteDishByIds(ids);
        //清理redis
        cleanRedis(KEY);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getDishById(@PathVariable Long id){
        log.info("根据id查询菜品");
        DishVO dishVO = dishService.getDishById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDishAndFlavor(@RequestBody DishDTO dishDTO){
        log.info("修改菜品");
        dishService.updateDishAndFlavor(dishDTO);
        //清理redis
        cleanRedis(KEY);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    private void cleanRedis(String parten){
        Set keys = redisTemplate.keys(parten);
        redisTemplate.delete(keys);
    }

    /**
     * 菜品的起售，停售
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品的起售，停售")
    public Result updateStatus(@PathVariable Integer status, Long id){
        log.info("菜品的起售，停售:{}",status);
        dishService.updateStatus(status, id);
        return Result.success();
    }

}
