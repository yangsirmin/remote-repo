package com.sky.service.impl;

import com.github.pagehelper.util.StringUtil;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private WorkspaceService workspaceService;
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

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserReportStatistics(LocalDate begin, LocalDate end) {
        //获得日期
        //封装日期
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //用户总量 create_time < endTime
        //新增用户 create_time > beginTime && create_time < endTime
        //封装总用户
        List<Integer> totalUserList = new ArrayList();
        //封装新用户
        List<Integer> newUserList = new ArrayList();
        //封装新用户
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("end", endTime);
            Integer totalUser = orderMapper.getUserByMap(map);
            totalUserList.add(totalUser);
            map.put("begin", beginTime);
            Integer newUser = orderMapper.getUserByMap(map);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderReportStatistics(LocalDate begin, LocalDate end) {
        //获得日期
        //封装日期
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //封装每日订单数
        ArrayList<Integer> orderCountList = new ArrayList();
        //封装每日有效订单数
        ArrayList<Integer> validOrderCountList = new ArrayList();
        //每日订单数和有效订单数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //每日订单数 order_time > beginTime && order_time < endTime
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            if (orderCount == null){
                orderCount = 0;
            }
            orderCountList.add(orderCount);
            //每日有效订单数 order_time > beginTime && order_time < endTime && status = 5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            if (validOrderCount == null){
                validOrderCount = 0;
            }
            validOrderCountList.add(validOrderCount);
        }
        //订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }




        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 查询订单数量
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status){
        Map map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);
        return orderMapper.getOrderCountByMap(map);
    }

    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //status = 5 && od.order_id = o.id && order_time > ? && order_time < ?
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSaleTop10(beginTime, endTime);
        //获得菜品名和销售量
        List<String> name = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> number = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(name, ","))
                .numberList(StringUtils.join(number, ","))
                .build();
    }

    /**
     * 导出Excel报表
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        LocalDate beginTime = LocalDate.now().minusDays(30);
        LocalDate endTime = LocalDate.now().minusDays(1);
        //查询进30天的数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginTime, LocalTime.MIN), LocalDateTime.of(endTime, LocalTime.MAX));
        //通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template\\运营数据报表模板.xlsx");
        //创建excel对象
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            //第二行
            sheet1.getRow(1).getCell(1).setCellValue(beginTime + " 至 " + endTime);
            //获取第4行
            XSSFRow row = sheet1.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            //获得第五行
            row = sheet1.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            BusinessDataVO businessDataVO = null;
            for (int i = 0; i < 30; i++) {
                LocalDate date = beginTime.plusDays(i);
                //获取一天的数据
                businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet1.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessDataVO.getTurnover());
                row.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            }
            //写入到浏览器页面
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭流
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
}
