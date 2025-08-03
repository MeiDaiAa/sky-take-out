package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO 菜品数据
     */
    @Override
    public void saveWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //将菜品添加到Dish库中
        dishMapper.insert(dish);
        //获取到回显的菜品id
        Long dishId = dish.getId();

        //将菜品口味添加到DishFlavor中
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            //将回显的菜品id添加到flavors中
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            //批量插入口味
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO 菜品查询参数
     * @return PageResult
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> list = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(list.getTotal(), list.getResult());
    }

    /**
     * 批量删除
     * @param ids 菜品id数组
     */
    @Override
    public void deleteByIdsWithFlavors(List<Long> ids) {
        //判断参数
        if(ids == null || ids.isEmpty())
            throw  new BaseException(MessageConstant.NO_ITEMS_TO_DELETE);


        //如果ids集合中包含分类状态为1的，则提示错误信息，这些分类不能删除
        List<Dish> dishList = dishMapper.getByIds(ids);
        dishList.forEach(dish -> {
            if(dish.getStatus() == 1)
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        });
        //如果套餐中包含分类，则提示错误信息，这些分类不能删除

        if(!setmealDishMapper.getByDishIds(ids).isEmpty())
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        //删除分类数据
        dishMapper.deleteByIds(ids);
        //删除口味 数据
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询
     * @param id 菜品id
     * @return DishVO
     */
    @Override
    public DishVO getByIdWithFlavors(Long id) {
        DishVO dishVo = new DishVO();

        //先获取菜品基本数据
        Dish dish = dishMapper.getById(id);
        if(dish != null)
            BeanUtils.copyProperties(dish,dishVo);
        //再获取菜品口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        if(dishFlavors != null && !dishFlavors.isEmpty()){
            dishVo.setFlavors(dishFlavorMapper.getByDishId(id));
        }

        return dishVo;
    }


    /**
     * 修改菜品及对应的口味数据
     * @param dishDTO 菜品数据
     */
    @Override
    @Transactional //开启事务
    public void updateWithFlavors(DishDTO dishDTO) {
        //获得菜品id
        Long dishId = dishDTO.getId();
        if(dishId == null)//菜品id为空
            throw new DeletionNotAllowedException(MessageConstant.UNKNOWN_ERROR);

        //修改前先删除该菜品对应的口味数据
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dishDTO.getId()));

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //更新菜品数据
        dishMapper.update(dish);

        //添加新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));//设置菜品id
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }
    }

    /**
     * 根据分类id查询菜品数据
     * @param categoryId 分类id
     * @return List<DishVO>
     */
    @Override
    public List<DishVO> getByCategoryId(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }


    /**
     * 条件查询菜品和口味
     * @param dish 菜品对象
     * @return List<DishVO>
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 菜品起售、停售
     * @param status 状态 1起售 0停售
     * @param id 菜品id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                        .status(status)
                        .id(id)
                        .build();

        dishMapper.update(dish);
    }
}
