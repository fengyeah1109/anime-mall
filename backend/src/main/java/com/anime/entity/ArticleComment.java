package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_comment")
public class ArticleComment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
}

