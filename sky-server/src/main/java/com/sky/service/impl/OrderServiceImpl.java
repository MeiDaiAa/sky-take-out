package com.sky.service.impl;

import com.alibaba.druid.util.HttpClientUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.common.utils.HttpUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.BaiDuMapProperties;
import com.sky.properties.WeChatProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.WebSocketServer;
import com.sky.utils.DistanceUtil;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private static final String BAIDU_URL = "https://api.map.baidu.com/geocoding/v3/";

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
    private BaiDuMapProperties baiDuMapProperties;
    @Autowired
    private WebSocketServer webSocketServer;


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

        //获取当前用户地址数据
        StringBuilder addressSb = new StringBuilder();
        addressSb.append(addressBook.getProvinceName())
                .append(addressBook.getCityName())
                .append(addressBook.getDistrictName())
                .append(addressBook.getDetail());
        String address = addressSb.toString();
        if(address.length()>64)
            throw new OrderBusinessException(MessageConstant.ADDRESS_TOO_LONG);
        //通过httpClient调用百度地图接口，查询用户的经纬值坐标

        Map<String, String> map = new HashMap<>();//参数准备
        map.put("ak", baiDuMapProperties.getAk());
        map.put("output", baiDuMapProperties.getOutput());
        map.put("address", address);

        String JsonString = HttpClientUtil.doGet(BAIDU_URL, map);//发送httpClient请求，获取返回结果
        JSONObject jsonObject = JSON.parseObject(JsonString);
        if(jsonObject.getInteger("status") != 0)//获取经纬度失败
            throw new OrderBusinessException(MessageConstant.GET_LOCATION_FAIL);
        //查看用户距离店家的距离
        JSONObject object = jsonObject.getObject("result", JSONObject.class).getObject("location", JSONObject.class);
        double haversine = DistanceUtil.haversine(object.getFloat("lat"), object.getFloat("lng"),
                baiDuMapProperties.getShopLatitude(), baiDuMapProperties.getShopLongitude());
        log.info("两地距离为：" + haversine);
        if(haversine > 5)//距离过远
            throw new OrderBusinessException(MessageConstant.DELIVERY_OUT_OF_RANGE);


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

        paySuccess(ordersPaymentDTO.getOrderNumber());//修改订单状态为支付成功
        //通知店家订单完成
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if(orders ==  null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + ordersPaymentDTO.getOrderNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

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

    /**
     * 取消订单
     * @param id 订单id
     */
    @Override
    public void cancel(Long id) {
//        Orders orders = new Orders();
//        orders.setId(id);
//        orders.setStatus(Orders.CANCELLED);
//        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(Orders.builder()
                                    .id(id)
                                    .status(Orders.CANCELLED)
                                    .cancelTime(LocalDateTime.now())
                                    .cancelReason("用户取消")
                                    .build()
        );
    }

    /**
     * 取消订单（商家）
     * @param ordersCancelDTO 取消订单参数
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
//        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
//        orders.setStatus(Orders.CANCELLED);
//        orders.setCancelTime(LocalDateTime.now());
//        orders.setCancelReason(ordersCancelDTO.getCancelReason());

        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if(orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus() == null ||
                (
                        !Orders.PENDING_PAYMENT.equals(orders.getStatus())
                        && !Orders.TO_BE_CONFIRMED.equals(orders.getStatus())
                        && !Orders.CONFIRMED.equals(orders.getStatus())
                        && !Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())
                )
        )
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        orderMapper.update(Orders.builder()
                                    .id(ordersCancelDTO.getId())
                                    .status(Orders.CANCELLED)
                                    .cancelReason(ordersCancelDTO.getCancelReason())
                                    .cancelTime(LocalDateTime.now())
                                    .build()
        );
    }

    /**
     * 再来一单
     * @param id 订单id
     */
    @Override
    public void repetition(Long id) {
        // 查询当前订单
        OrderVO orderVO = orderMapper.getByIdWithOrderDetail(id);

        //构建新的订单数据
        OrdersSubmitDTO ordersSubmitDTO = new OrdersSubmitDTO();
        BeanUtils.copyProperties(orderVO, ordersSubmitDTO);
        //设置下单时间为当前时间+1小时
        ordersSubmitDTO.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));

        //将订单数据中的订单详情数据转换成购物车数据
        Long userId = BaseContext.getCurrentId();

        List<OrderDetail> orderDetailList = orderVO.getOrderDetailList();//订单详情数据
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());//创建购物车数据
        shoppingCartMapper.insertBatch(shoppingCartList);

        // 调用保存订单
        submit(ordersSubmitDTO);
    }


    /**
     * 条件搜索
     * @param ordersPageQueryDTO 搜索条件
     * @return PageResult
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.conditionSearch(ordersPageQueryDTO);
        page.forEach(orderVO -> {
            StringBuilder orderDishes = new StringBuilder();
            orderVO.getOrderDetailList().forEach(orderDetail -> {
                orderDishes.append(orderDetail.getName()).append(" *").append(orderDetail.getNumber()).append("，");
            });
            orderVO.setOrderDishes(orderDishes.substring(0, orderDishes.length() - 1));
        });

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 统计订单数据
     * @return OrderStatisticsVO
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.countStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(orderMapper.countStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));

        return orderStatisticsVO;
    }

    /**
     * 确认订单
     * @param ordersConfirmDTO 订单确认参数
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
//        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
//
//        if(orders == null)
//            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
//
//        orders.setStatus(Orders.CONFIRMED);
//        orderMapper.update(orders);
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if(orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus() == null || !Orders.TO_BE_CONFIRMED.equals(orders.getStatus()))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);


        orderMapper.update(Orders.builder()
                                    .id(ordersConfirmDTO.getId())
                                    .status(Orders.CONFIRMED)
                                    .build()
        );
    }

    /**
     * 拒单
     * @param ordersRejectionDTO 拒单参数
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
//        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());//获取订单
//
//        if(orders == null)//订单不存在
//            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
//
//        orders.setStatus(Orders.CANCELLED);
//        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
//        orders.setCancelTime(LocalDateTime.now());
//
//        orderMapper.update(orders);

        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if(orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus() == null || !Orders.TO_BE_CONFIRMED.equals(orders.getStatus()))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        orderMapper.update(Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build()
        );
    }

    /**
     * 派送订单
     * @param id 订单id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus() == null || !Orders.CONFIRMED.equals(orders.getStatus()))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build()
        );
    }

    /**
     * 完成订单
     * @param id 订单id
     */
    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus() == null || !Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus()))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build()
        );
    }
}
