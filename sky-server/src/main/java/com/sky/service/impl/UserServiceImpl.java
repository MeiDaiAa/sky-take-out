package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private static final String USER_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 用户登录
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        UserLoginVO userLoginVO = new UserLoginVO();

        String openid = getOpenid(userLoginDTO);
        log.info("微信登录，获取openid：{}", openid);

        if(openid == null)
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);

        userLoginVO.setOpenid(openid);

        //查找数据库中是否存有该用户
        User user = userMapper.getByOpenid(openid);

        if(user == null){//用户不存在，新建用户
            log.info("新用户，开始注册...");

            User newUser = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.insert(newUser);

            userLoginVO.setId(newUser.getId());
        }else{//用户已存在,保存用户id
            userLoginVO.setId(user.getId());
        }


        //生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userLoginVO.getId());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        userLoginVO.setToken(jwt);

        return userLoginVO;
    }

    /**
     * 获取微信用户的openid
     * @param userLoginDTO
     * @return
     */
    private String getOpenid(UserLoginDTO userLoginDTO) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");


        String jsonResponse = HttpClientUtil.doGet(USER_LOGIN_URL, map);

        JSONObject jsonObject = JSON.parseObject(jsonResponse);
        return jsonObject.getString("openid");
    }
}
