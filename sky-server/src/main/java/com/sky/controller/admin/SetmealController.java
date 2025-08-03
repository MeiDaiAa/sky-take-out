package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐管理相关接口")
public class SetmealController {
    @Autowired
    SetmealService setmealService;

    /**
     * 分页查询
     * @param setmealPageQueryDTO 套餐分页参数
     * @return Result<PageResult>
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐信息: {}", setmealPageQueryDTO);

        return Result.success(setmealService.pageQuery(setmealPageQueryDTO));
    }

    /**
     * 新增套餐
     * @param setmealDTO 套餐数据
     * @return Result<Void>
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result<Void> save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐: {}", setmealDTO);

        setmealService.saveWithSetmealDish(setmealDTO);

        return Result.success();
    }

    /**
     * 根据id查询
     * @param id  套餐id
     * @return Result<SetmealVO>
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐信息: {}", id);

        return Result.success(setmealService.getByIdWithSetmealDish(id));
    }


    /**
     * 套餐启售停售
     * @param status 状态 1起售 0停售
     * @param id 套餐id
     * @return Result<Void>
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售、停售")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<Void> startOrStop(@PathVariable Integer status, Long id){
        log.info("套餐: {} ,起售、停售: {}", status, id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 批量删除套餐
     * @param ids 套餐id
     * @return Result<Void>
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<Void> delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐: {}", ids);

        setmealService.deleteByIdsWithSetmealDish(ids);

        return Result.success();
    }

    /**
     * 修改套餐
     * @param setmealDTO 套餐数据
     * @return Result<Void>
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result<Void> update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐: {}", setmealDTO);

        setmealService.updateWithSetmealDish(setmealDTO);

        return Result.success();
    }
}
