package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO 用户下单数据
     * @return OrderSubmitVO
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO 订单支付数据
     * @return OrderPaymentVO
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo 微信支付返回的订单号
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO 历史订单查询参数
     * @return PageResult
     */
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单详情
     * @param id 订单id
     * @return OrderVO
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 取消订单
     * @param id 订单id
     */
    void cancel(Long id);

    /**
     * 取消订单(商家)
     * @param ordersCancelDTO 取消订单参数
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 再来一单
     * @param id 订单id
     */
    void repetition(Long id);

    /**
     * 条件搜索订单
     * @param ordersPageQueryDTO 搜索条件
     * @return PageResult
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 统计订单数据
     * @return OrderStatisticsVO
     */
    OrderStatisticsVO statistics();


    /**
     * 确认订单
     * @param ordersConfirmDTO 订单确认参数
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO 拒单参数
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 派单
     * @param id 订单id
     */
    void delivery(Long id);

    /**
     * 完成订单
     * @param id 订单id
     */
    void complete(Long id);

    /**
     * 催单
     * @param id 订单id
     */
    void reminder(Long id);
}
