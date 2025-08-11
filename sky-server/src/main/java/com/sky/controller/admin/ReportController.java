package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    /**
     * 营业额数据统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return Result<TurnoverReportVO>
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额数据统计")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额数据统计：{}到{}", begin, end);


        return Result.success(reportService.turnoverStatistics(begin, end));
    }

    /**
     * 用户数据统计
     * @param begin 统计的开始时间
     * @param end 统计的结束时间
     * @return Result<UserReportVO>
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户数据统计")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户数据统计：{}到{}", begin, end);

        return Result.success(reportService.userStatistics(begin, end));
    }


    /**
     * 订单数据统计
     * @param begin 开始 时间
     * @param end 结束 时间
     * @return Result<OrderReportVO>
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单数据统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("订单数据统计：{}到{}", begin, end);
        return Result.success(reportService.orderStatistics(begin, end));
    }

    /**
     * 查询销量排名top10
     * @param begin 开始 时间
     * @param end 结束时间
     * @return Result<SalesTop10ReportVO>
     */
    @GetMapping("/top10")
    @ApiOperation("查询销量排名top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查询销量排名top10：{}到{}", begin, end);
        return Result.success(reportService.top10(begin, end));
    }
}
