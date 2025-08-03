package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "用户购物车相关接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车数据
     * @return Result<Void>
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result<Void> add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("用户添加购物车：{}", shoppingCartDTO);

        shoppingCartService.add(shoppingCartDTO);

        return Result.success();
    }

    /**
     * 查看购物车
     * @return Result<List<ShoppingCart>>
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        log.info("用户查看购物车，用户id:{}", BaseContext.getCurrentId());

        return Result.success(shoppingCartService.list());
    }

    /**
     * 清空购物车
     * @return Result<Void>
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result<Void> clean(){
        log.info("用户清空购物车，用户id:{}", BaseContext.getCurrentId());

        shoppingCartService.clean();

        return Result.success();
    }
}
