package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String category;
    private Integer viewCount;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}

