package com.sky.properties;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.exception.OrderBusinessException;
import com.sky.utils.HttpClientUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "sky.map.baidu")
@Data
public class BaiDuMapProperties {
    private String BAIDU_URL = "https://api.map.baidu.com/geocoding/v3/";

    private String ak;// 百度地图的ak
    private String output; // 返回数据格式
    private String ShopLocation;// 店铺地址

    private Float shopLongitude;// 经度
    private Float shopLatitude;// 纬度

    @PostConstruct// 初始化
    public void init() {
        //通过httpClient调用百度地图接口，查询商家的经纬值坐标

        Map<String, String> map = new HashMap<>();//参数准备
        map.put("ak", ak);
        map.put("output",output);
        map.put("address", ShopLocation);

        String JsonString = HttpClientUtil.doGet(BAIDU_URL, map);//发送httpClient请求，获取返回结果
        JSONObject jsonObject = JSON.parseObject(JsonString);
        if(jsonObject.getInteger("status") != 0)//获取经纬度失败
            throw new OrderBusinessException(MessageConstant.GET_LOCATION_FAIL);
        //获取经纬度
        JSONObject object = jsonObject.getObject("result", JSONObject.class)
                .getObject("location", JSONObject.class);
        shopLongitude = object.getFloat("lng");
        shopLatitude = object.getFloat("lat");
    }
}
