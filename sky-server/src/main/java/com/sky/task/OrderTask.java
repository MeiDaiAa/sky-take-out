package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理订单超时
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟执行一次
    public void processTimeoutOrder(){
        log.info("处理超时订单");

        //获取超时订单
        LocalDateTime TargetTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, TargetTime);

        if(list != null && !list.isEmpty()){
            list.forEach(orders -> {
                orderMapper.update(Orders.builder()
                        .id(orders.getId())
                        .status(Orders.CANCELLED)
                        .cancelReason("支付超时")
                        .cancelTime(LocalDateTime.now())
                        .build()
                );//修改订单
            });
            log.info("{} 个超时订单已被处理", list.size());
        }
    }

    /**
     * 处理派送中订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点执行
    public void processDeliveryOrder(){
        log.info("处理派送中订单");

        LocalDateTime targetTime = LocalDateTime.now().minusMinutes(60);
        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, targetTime);

        if(list != null && !list.isEmpty()){
            list.forEach(orders -> {
                orderMapper.update(Orders.builder()
                        .id(orders.getId())
                        .status(Orders.COMPLETED)
                        .deliveryTime(LocalDateTime.now())
                        .build()
                );
            });
            log.info("{} 个订单处理完成", list.size());
        }
    }
}
