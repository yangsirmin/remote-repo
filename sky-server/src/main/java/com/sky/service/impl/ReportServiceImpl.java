package com.sky.service.impl;

import com.github.pagehelper.util.StringUtil;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatus(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList();
        ArrayList<Double> turnoverList = new ArrayList<>();
        //获取日期
        dateList.add(begin);
        while(!begin.equals(end)) {
            //获取营业额
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            //orders_time > beginTime && orders_tome < endTime && status = 5
            Double turnover = orderMapper.turnoverByMap(map);
            turnover = turnover == null ? 0 : turnover;
            turnoverList.add(turnover);
            ////获取日期,天数加1
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //获取日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
        String date = StringUtils.join(dateList, ",");
        //获取营业额,以逗号分隔，例如：406.0,1520.0,75.0
        String turnoverString = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO.builder()
                .dateList(date)
                .turnoverList(turnoverString)
                .build();
    }
}
