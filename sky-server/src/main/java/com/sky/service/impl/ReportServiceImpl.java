package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return TurnoverReportVO
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Double> turnoverList = new ArrayList<>();
        dateList.forEach(date -> {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime last = LocalDateTime.of(date, LocalTime.MAX);


            Double turnover = orderMapper.getTurnoverByDates(start, last, Orders.COMPLETED);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        });

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return UserReportVO
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        //首先计算第一天的前一天的用户人数
        Integer totalUser = userMapper.getUserCountByDates(null,
                LocalDateTime.of(dateList.get(0).minusDays(1), LocalTime.MAX));
        totalUser = totalUser == null ? 0 : totalUser;

        for(LocalDate date : dateList){
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime last = LocalDateTime.of(date, LocalTime.MAX);

//            Integer totalUser = userMapper.getUserCountByDates(null, last);
//            totalUserList.add(totalUser == null ? 0 : totalUser);
            Integer newUser = userMapper.getUserCountByDates(start, last);//计算新增用户数
            newUser = newUser == null ? 0 : newUser;

            totalUser += newUser;//总用户数累加

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 营业额数据统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return TurnoverReportVO
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime last = LocalDateTime.of(date, LocalTime.MAX);

            Integer orderCount = orderMapper.getCountByDates(start, last, null);//每日订单数
            Integer validOrderCount = orderMapper.getCountByDates(start, last, Orders.COMPLETED);//每日有效订单数

            orderCountList.add(orderCount == null ? 0 : orderCount);
            validOrderCountList.add(validOrderCount == null ? 0 : validOrderCount);
        }

        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).orElse(0);
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).orElse(0);

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(validOrderCount.doubleValue() / totalOrderCount)
                .build();
    }

    /**
     * 计算两个时间段内的所有日期
     * @param begin 时间段开始时间
     * @param end 时间段结束时间
     * @return List<LocalDate>
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while(begin.isBefore(end.plusDays(1))){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }


    /**
     * 查询销量排名top10
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return SalesTop10ReportVO
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> goodsSalesDTOlist = orderDetailMapper
                .getDishesSalesTop10ByDates(
                        LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX), Orders.COMPLETED);

        return SalesTop10ReportVO.builder()
                .nameList(goodsSalesDTOlist.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(",")))
                .numberList(goodsSalesDTOlist.stream().map(GoodsSalesDTO::getNumber).map(Object::toString).collect(Collectors.joining(",")))
                .build();
    }

    /**
     * 导出Excel报表
     * @param response HttpServletResponse
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx")));

        XSSFSheet sheet = workbook.getSheet("Sheet1");

        LocalDate now = LocalDate.now().minusDays(1);
        LocalDate past30Date = LocalDate.now().minusDays(30);

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(past30Date, LocalTime.MIN), LocalDateTime.of(now, LocalTime.MAX));

        //设置时间
        sheet.getRow(1).getCell(1).setCellValue("时间：" + past30Date + "至" + now);
        //设置营业额
        XSSFRow row3 = sheet.getRow(3);
        row3.getCell(2).setCellValue(businessData.getTurnover());
        //设置订单完成率
        row3.getCell(4).setCellValue(String.format("%.2f%%", businessData.getOrderCompletionRate() * 100));
        //设置新增用户数
        row3.getCell(6).setCellValue(businessData.getNewUsers());
        //设置有效订单
        XSSFRow row4 = sheet.getRow(4);
        row4.getCell(2).setCellValue(businessData.getValidOrderCount());
        //设置平均客单价
        row4.getCell(4).setCellValue("￥" + String.format("%.2f", businessData.getTurnover()/ businessData.getValidOrderCount()));

        for (int i = 0; i < 30; i++) {
            XSSFRow row = sheet.getRow(7 + i);
            BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(past30Date.plusDays(i), LocalTime.MIN), LocalDateTime.of(past30Date.plusDays(i), LocalTime.MAX));

            //设置日期
            row.getCell(1).setCellValue(past30Date.plusDays(i).toString());
            //设置营业额
            row.getCell(2).setCellValue(data.getTurnover());
            //设置有效订单
            row.getCell(3).setCellValue(data.getValidOrderCount());
            //设置订单完成率
            row.getCell(4).setCellValue(String.format("%.2f%%", data.getOrderCompletionRate() * 100));
            //设置平均客单价
            row.getCell(5).setCellValue("￥" + String.format("%.2f", data.getValidOrderCount() == 0 ? 0 : data.getTurnover() / data.getValidOrderCount()));
            //设置新增用户数
            row.getCell(6).setCellValue(data.getNewUsers());
        }


        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);

        outputStream.flush();
        outputStream.close();
        workbook.close();
    }
}
