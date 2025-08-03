package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    DishService dishService;

    /**
     * 添加菜品
     * @param dishDTO 菜品参数
     * @return Result<Void>
     */
    @PostMapping
    @ApiOperation("添加菜品")
    //为何添加也要删除缓存：添加菜品时，该分类下的缓存并没有当前需要添加菜品的缓存数据，所以要删除缓存，重新生成含有当前菜品的缓存数据
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public Result<Void> save(@RequestBody DishDTO dishDTO) {
        log.info("添加菜品: {}", dishDTO);
        dishService.saveWithFlavors(dishDTO);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO 菜品查询参数
     * @return Result<PageResult>
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询: {}", dishPageQueryDTO);

        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除
     * @param ids 需要删除的菜品id数组
     * @return Result<Void>
     */
    @DeleteMapping
    @ApiOperation("批量删除")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<Void> delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品：{}", ids);

        dishService.deleteByIdsWithFlavors(ids);

        return Result.success();
    }

    /**
     * 根据id查询
     * @param id  菜品id
     * @return Result<DishVo>
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询菜品信息：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO 修改的菜品数据
     * @return Result<Void>
     */
    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public Result<Void> update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);

        dishService.updateWithFlavors(dishDTO);

        return Result.success();
    }


    /**
     * 根据分类id查询菜品
     * @param categoryId 分类id
     * @return Result<List<DishVO>>
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> getByCategoryId(Long categoryId){
        log.info("根据分类id查询菜品信息：{}", categoryId);

        List<DishVO> dishVOs= dishService.getByCategoryId(categoryId);

        return Result.success(dishVOs);
    }


    /**
     * 菜品起售停售
     * @param status 状态：1起售 0停售
     * @param id 菜品id
     * @return Result<Void>
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<Void> startOrStop(@PathVariable Integer status, Long id) {
        log.info("起售或停售菜品：{}", id);

        dishService.startOrStop(status, id);

        return Result.success();
    }
}
