package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMappper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //https调用
    private static final String USER_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMappper userMappper;
    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User userLogin(UserLoginDTO userLoginDTO) {
        //获取用户openid
        String openid = getOpenid(userLoginDTO.getCode());
        //判断openid是否获得
        if(openid == null){
            throw new LoginFailedException(MessageConstant.USER_NOT_LOGIN);
        }
        //判断是否为新用户
        User user = userMappper.selectUser(openid);
        if(user == null){
            //无用户则添加用户
            User user1 = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMappper.insertUser(user1);
            return user1;
        }
        return user;
    }

    //获取用户openid
    public String getOpenid(String code){
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        //获得json格式的字符串
        String json = HttpClientUtil.doGet(USER_LOGIN_URL, map);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
