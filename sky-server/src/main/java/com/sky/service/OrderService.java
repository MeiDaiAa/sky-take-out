package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO 用户下单数据
     * @return OrderSubmitVO
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}
