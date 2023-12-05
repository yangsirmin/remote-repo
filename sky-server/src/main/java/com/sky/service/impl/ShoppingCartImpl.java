package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断套餐或者菜品是否已存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //在线程中取出id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.selectShoppingCartDish(shoppingCart);
        if (list != null && list.size() > 0){
            //菜品或者套餐已存在(number + 1),更新shopping_cart表
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateShoppingCart(cart);
        }else {//菜品或者套餐不存在
            //判断为套餐还是菜品
            if (shoppingCart.getDishId() != null){//添加菜品操作
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                //设置 菜品名 价格 图片
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }else{//添加套餐操作
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                //设置 菜品名 价格 图片
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }

            //设置 数量 创建时间
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            //插入套餐或者菜品
            shoppingCartMapper.insertShoppingCart(shoppingCart);
        }
    }

    /**
     * 查看购物车
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(currentId)
                .build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectShoppingCartDish(shoppingCart);
        return shoppingCarts;
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.cleanShoppingCart(userId);
    }

    /**
     * 删除购物车中一个商品
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        shoppingCartMapper.subShoppingCart(shoppingCart);
    }
}
