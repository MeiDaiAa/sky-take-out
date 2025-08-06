package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO 用户下单信息
     * @return Result<OrderSubmitVO>
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}", ordersSubmitDTO);

        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO 订单支付信息
     * @return Result<OrderPaymentVO>
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查看历史订单
     * @param ordersPageQueryDTO 查询参数
     * @return Result<PageResult>
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查看历史订单")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("查看历史订单: {}", ordersPageQueryDTO);

        PageResult pageResult = orderService.historyOrders(ordersPageQueryDTO);

        return Result.success(pageResult);
    }


    /**
     * 查看订单详情
     * @param id 订单id
     * @return Result<OrderVO>
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查看订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        log.info("查看订单详情: {}", id);

        OrderVO orderVO = orderService.getOrderDetail(id);

        return Result.success(orderVO);
    }


    /**
     * 取消订单
     * @param id 订单id
     * @return Result<Void>
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@PathVariable Long id){
        log.info("取消订单: {}", id);

        orderService.cancel(id);

        return Result.success();
    }
}
