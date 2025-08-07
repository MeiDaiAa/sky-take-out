package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders 订单数据
     */
    @Insert("insert into orders(" +
            "number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, phone, address, user_name, consignee, cancel_reason, rejection_reason, cancel_time, estimated_delivery_time, delivery_status, delivery_time, pack_amount, tableware_number, tableware_status) " +
            "values (" +
            "#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{checkoutTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, #{phone}, #{address}, #{userName}, #{consignee}, #{cancelReason}, #{rejectionReason}, #{cancelTime}, #{estimatedDeliveryTime}, #{deliveryStatus}, #{deliveryTime}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO 查询参数
     * @return Page<OrderVO>
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 分页查询订单详情
     * @param ordersPageQueryDTO 查询参数
     * @return Page<OrderVO>
     */
    Page<OrderVO> pageQueryWithOrderDetail(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单详情
     * @param id 订单id
     * @return OrderVO
     */
    OrderVO getByIdWithOrderDetail(Long id);

    /**
     * 条件搜索订单
     * @param ordersPageQueryDTO 搜索条件
     * @return Page<OrderVO>
     */
    Page<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 统计订单状态数量
     * @param status 订单状态
     * @return  Integer
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Integer status);
}
