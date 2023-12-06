package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")//从零秒开始，每分钟执行一次
    public void paymentOutTime(){
        log.info("取消支付超时订单");
        //15分钟未支付，取消订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> list = orderMapper.outTime(Orders.TO_BE_CONFIRMED, time);
        //支付超时改为 已取消
        LocalDateTime now = LocalDateTime.now();
        if (list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时");
                orders.setCancelTime(now);
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ? *")//每天凌晨1点执行
    public void completedDeliver(){
        log.info("修改凌晨1点派送中的订单");
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> list = orderMapper.outTime(Orders.DELIVERY_IN_PROGRESS, time);
        //凌晨1点存在派送中的外卖并且派送时间达到1小时，则更新为已完成
        LocalDateTime now = LocalDateTime.now();
        if (list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
