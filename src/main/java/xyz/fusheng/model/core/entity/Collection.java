package xyz.fusheng.model.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @FileName: Collection
 * @Author: code-fusheng
 * @Date: 2020/8/10 21:46
 * @version: 1.0
 * Description: 收藏实体类
 */

@Data
@TableName("m_collection")
public class Collection {

    /**
     * 收藏id
     */
    @TableId
    private Long collectionId;

    /**
     * 收藏用户id (收藏操作执行者)
     */
    private Long collectionUserId;

    /**
     * 收藏对象id
     */
    private Long collectionTarget;

    /**
     * 收藏类型
     * 0 - 默认；1 - 文章；2 - 其他；
     */
    private Integer collectionType;

    /**
     * 收藏时间
     */
    private String collectionTime;

}
