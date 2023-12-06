package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMappper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from sky_take_out.user where openid = #{openid}")
    User selectUser(String openid);

    /**
     * 插入用户
     * @param user1
     */
    void insertUser(User user1);

    @Select("select * from sky_take_out.user where id = #{id}")
    User getById(Long id);
}
