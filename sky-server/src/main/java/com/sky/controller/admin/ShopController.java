package com.sky.controller.admin;

import com.sky.config.RedisConfiguration;
import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {
    private static final String KEY = "SHOP_STATUS";

    @Autowired
    @Qualifier("myRedisTemplate")// 指定使用myRedisTemplate
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 修改营业状态
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("修改营业状态")
    public Result setStatus(@PathVariable String status){
        log.info("修改营业状态: {}", status);

        redisTemplate.opsForValue().set(KEY, status);

        return Result.success();
    }

    /**
     * 获取营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result getStatus(){
        log.info("获取营业状态");

        String status = (String) redisTemplate.opsForValue().get(KEY);

        if(status == null)
            throw new BaseException(MessageConstant.SHOP_STATUS_ERROR);

        return Result.success(Integer.parseInt(status));
    }
}
