package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.WeChatProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.apache.commons.lang.RandomStringUtils;
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
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WeChatProperties weChatProperties;


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


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO 订单支付参数
     * @return OrderPaymentVO
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal("0.01"), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;

        //不调用微信支付接口，测试使用
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = RandomStringUtils.randomNumeric(32);
        String packageSign = "temp";

        paySuccess(ordersPaymentDTO.getOrderNumber());

        return OrderPaymentVO.builder()
                .nonceStr(nonceStr)
                .paySign(packageSign)
                .timeStamp(timeStamp)
                .signType("RSA")
                .build();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo 微信支付成功返回的订单号
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }


    /**
     * 历史订单查询
     * @param ordersPageQueryDTO 历史订单查询参数
     * @return PageResult
     */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        //获取到用户id
        Long userId = BaseContext.getCurrentId();

        ordersPageQueryDTO.setUserId(userId);

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

//        //查询订单基本数据
//        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
//        //查询订单明细数据
//        page.forEach(orderVO -> {
//            orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orderVO.getId()));
//        });

        //使用leftJoin + resultMap
        Page<OrderVO> page = orderMapper.pageQueryWithOrderDetail(ordersPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 订单详情
     * @param id 订单id
     * @return OrderVO
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        return orderMapper.getByIdWithOrderDetail(id);
    }
}
