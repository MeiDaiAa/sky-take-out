package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细数据
     * @param orderDetailList 订单明细数据列表
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单明细
     * @param id 订单id
     * @return List<OrderDetail>
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long id);

    /**
     * 查询指定时间区间内订单明细数据
     * @param begin 时间区间开始
     * @param end 时间区间结束
     * @param status 订单状态
     * @return List<GoodsSalesDTO>
     */
    List<GoodsSalesDTO> getDishesSalesTop10ByDates(LocalDateTime begin, LocalDateTime end, Integer status);
}
