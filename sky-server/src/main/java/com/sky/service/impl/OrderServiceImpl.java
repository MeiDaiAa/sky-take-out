package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;


    /**
     * 订单提交
     * @param ordersSubmitDTO 用户端提交的订单数据
     * @return OrderSubmitVO
     */
    @Override
    @Transactional//事务
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        //数据校验
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook ==null)//用户地址为空
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.getByUserId(userId);
        if(shoppingCartList == null || shoppingCartList.isEmpty())//购物车为空
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);


        User user = userMapper.getById(userId);
        //创建订单
        Orders orders = Orders.builder()
                .number(String.valueOf(System.currentTimeMillis()))
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .orderTime(LocalDateTime.now())
                .payStatus(Orders.UN_PAID)
                .userName(user.getName())
                .phone(addressBook.getPhone())
                .address(addressBook.getDetail())
                .consignee(addressBook.getConsignee())
                .build();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        //插入订单数据
        orderMapper.insert(orders);


        //插入订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        shoppingCartList.forEach(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());

            orderDetailList.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetailList);

        //清理购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }
}
