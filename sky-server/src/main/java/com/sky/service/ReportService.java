package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {
    /**
     * 营业额统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return TurnoverReportVO
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return UserReportVO
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单数据统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return OrderReportVO
     */
    OrderReportVO orderStatistics(LocalDate begin, LocalDate end);
}
