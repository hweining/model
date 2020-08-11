package xyz.fusheng.model.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.fusheng.model.common.enums.StateEnums;
import xyz.fusheng.model.common.utils.Page;
import xyz.fusheng.model.common.utils.SecurityUtil;
import xyz.fusheng.model.core.entity.Comment;
import xyz.fusheng.model.core.mapper.ArticleMapper;
import xyz.fusheng.model.core.mapper.CommentMapper;
import xyz.fusheng.model.core.service.CommentService;
import xyz.fusheng.model.core.vo.CommentVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @FileName: CommentServiceImpl
 * @Author: code-fusheng
 * @Date: 2020/6/3 0:11
 * @version: 1.0
 * Description: 评论业务逻辑接口实现类
 */

@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ArticleMapper articleMapper;

    private List<Integer> types = new ArrayList<>(16);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveComment(Comment comment) {
        // 保存评论（先执行再更新，数据真实性）
        commentMapper.insert(comment);
        // 获取评论类型
        int commentType = comment.getCommentType();
        types.clear();
        switch (commentType) {
            // 文章的评论，文章评论数更新
            case 0:
                // 更新文章评论数
                types.add(StateEnums.ARTICLE_COMMENT.getCode());
                types.add(StateEnums.COMMENT_COMMENT.getCode());
                commentMapper.updateArticleCommentCount(comment.getCommentTarget(), commentMapper.getCountByRid(comment.getCommentRoot(), types));
                break;
            case 1:
                // 更新评论评论数
                types.add(StateEnums.COMMENT_COMMENT.getCode());
                commentMapper.updateCommentCommentCount(comment.getCommentTarget(), commentMapper.getCountByTid(comment.getCommentTarget(), types));
                types.add(StateEnums.ARTICLE_COMMENT.getCode());
                commentMapper.updateArticleCommentCount(comment.getCommentRoot(), commentMapper.getCountByRid(comment.getCommentRoot(), types));
            default:
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        // 先查找实体、再删除、最后计算变动数据
        Comment comment = commentMapper.selectById(id);
        commentMapper.deleteById(id);
        int commentType = comment.getCommentType();
        types.clear();
        // 判断删除的评论是否是文章评论
        if (commentType == StateEnums.ARTICLE_COMMENT.getCode()) {
            // 更新文章评论数
            types.add(StateEnums.ARTICLE_COMMENT.getCode());
            types.add(StateEnums.COMMENT_COMMENT.getCode());
            commentMapper.updateArticleCommentCount(comment.getCommentRoot(), commentMapper.getCountByRid(comment.getCommentRoot(), types));
            // 判断删除评论是否是文章评论的评论
        } else if (commentType == StateEnums.COMMENT_COMMENT.getCode()) {
            // 更新文章评论的评论数
            types.add(StateEnums.COMMENT_COMMENT.getCode());
            commentMapper.updateCommentCommentCount(comment.getCommentTarget(), commentMapper.getCountByTid(comment.getCommentTarget(), types));
            // 更新文章的评论数
            types.add(StateEnums.ARTICLE_COMMENT.getCode());
            commentMapper.updateArticleCommentCount(comment.getCommentRoot(), commentMapper.getCountByRid(comment.getCommentRoot(), types));
        } else {
            return;
        }
    }

    @Override
    public Page<CommentVo> getByPage(Page<CommentVo> page) {
        // 查询数据
        List<CommentVo> commentList = commentMapper.getCommentList(page);
        // 获取登录用户id
        Long uid = SecurityUtil.getUserId();
        commentList.forEach(commentVo -> {
            commentVo.setGoodCommentFlag(false);
        });
        page.setList(commentList);
        // 统计总数
        int totalCount = commentMapper.getCountByPage(page);
        page.setTotalCount(totalCount);
        return page;
    }


}
