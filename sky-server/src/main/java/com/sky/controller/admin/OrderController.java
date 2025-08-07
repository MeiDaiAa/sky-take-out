package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO 订单条件搜索信息
     * @return Result<PageResult>
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索：{}", ordersPageQueryDTO);

        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        log.info("各个状态的订单数量统计");

        OrderStatisticsVO orderStatisticsVO = orderService.statistics();

        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * @param id 订单id
     * @return Result<PageResult>
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("查询订单详情：{}", id);

        OrderVO orderDetail = orderService.getOrderDetail(id);

        return Result.success(orderDetail);
    }


    /**
     * 确认订单
     * @param ordersConfirmDTO 订单id
     * @return Result<Void>
     */
    @PutMapping("/confirm")
    @ApiOperation("确认订单")
    public Result<Void> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("确认订单：{}", ordersConfirmDTO);

        orderService.confirm(ordersConfirmDTO);

        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO 拒单参数
     * @return Result<Void>
     */
    @PutMapping("/rejection")
    @ApiOperation("取消订单")
    public Result<Void> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("订单取消：{}", ordersRejectionDTO);

        orderService.rejection(ordersRejectionDTO);

        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("订单取消：{}", ordersCancelDTO);

        orderService.cancel(ordersCancelDTO);

        return Result.success();
    }
}
