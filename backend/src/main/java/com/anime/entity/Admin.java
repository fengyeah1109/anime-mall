package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin")
public class Admin {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String avatar;
    private Integer role;
    private Integer status;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}

