package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return TurnoverReportVO
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while(begin.isBefore(end.plusDays(1))){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        List<Double> turnoverList = new ArrayList<>();
        dateList.forEach(date -> {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime last = LocalDateTime.of(date, LocalTime.MAX);


            Double turnover = reportMapper.getTurnoverByDates(start, last, Orders.COMPLETED);
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
        List<LocalDate> dateList = new ArrayList<>();
        while(begin.isBefore(end.plusDays(1))){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

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
}
