package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDatilsMapper {

    /**
     * 插入表单明细
     * @param ordersList
     */
    void insetOrderDetails(List<OrderDetail> ordersList);
}
