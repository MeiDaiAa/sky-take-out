package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO 套餐查询参数
     * @return PageResult
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO 套餐数据
     */
    @Override
    @Transactional // 开启事务
    public void saveWithSetmealDish(SetmealDTO setmealDTO) {
        //查询当前套餐包含的菜品数量
        List<Long> dishIds = new ArrayList<>();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //获取菜品id
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> dishIds.add(setmealDish.getDishId()));
        }

        //通过菜品id查询菜品数量
        List<Dish> dishes = dishMapper.getByIds(dishIds);
        if(dishes != null && !dishes.isEmpty()){
            //如果查出的菜品数量和参数中的菜品数量不一致，则提示错误
            assert setmealDishes != null;
            if(dishes.size() != setmealDishes.size()){
                throw new BaseException(MessageConstant.UNKNOWN_ERROR);
            }

            //遍历菜品，查看菜品是否都在售
            dishes.forEach(dish -> {
                if(dish.getStatus().equals(StatusConstant.DISABLE)){
                    throw new BaseException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }

        //插入套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        //插入套餐和菜品的关联数据
        Long SetmealId = setmeal.getId();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(SetmealId);
            });
            setmealMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 根据id查询套餐数据
     * @param id 套餐id
     * @return SetmealVO
     */
    @Override
    public SetmealVO getByIdWithSetmealDish(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        //先查询套餐数据
        Setmeal setmeal = setmealMapper.getById(id);
        BeanUtils.copyProperties(setmeal, setmealVO);
        //再查询套餐和菜品的关联数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 套餐启售停售
     * @param status 状态 1起售 0停售
     * @param id 套餐id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 批量删除套餐
     * @param ids 套餐id数组
     */
    @Override
    public void deleteByIdsWithSetmealDish(List<Long> ids) {
        //如果ids内并没有值，则返回
        if(ids == null || ids.isEmpty())
            throw new BaseException(MessageConstant.NO_ITEMS_TO_DELETE);

        //查询套餐状态，判断是否可以删除,如果套餐处于起售状态，则不能删除
        List<Setmeal> setmealList = setmealMapper.getByIds(ids);

        if(setmealList != null && !setmealList.isEmpty()){
            setmealList.forEach(setmeal -> {
                if(StatusConstant.ENABLE.equals(setmeal.getStatus()))
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            });
        }

        //删除套餐数据
        setmealMapper.deleteByIds(ids);
        //删除套餐和菜品的关联数据
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 修改套餐
     * @param setmealDTO 套餐数据
     */
    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDTO setmealDTO) {
        //获取到id
        Long setmealId = setmealDTO.getId();
        //判断id是否为空
        if(setmealId == null){
            throw new DeletionNotAllowedException(MessageConstant.UNKNOWN_ERROR);
        }

        //修改套餐前先删除套餐和菜品的关联数据
        setmealDishMapper.deleteBySetmealIds(Collections.singletonList(setmealDTO.getId()));

        //更新套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            //将id值插入到setmealDishes中
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 条件查询
     * @param setmeal 套餐数据
     * @return 符合条件的套餐数据 List<Setmeal>
     */
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据id查询菜品选项
     * @param id 套餐id
     * @return List<DishItemVO>
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
