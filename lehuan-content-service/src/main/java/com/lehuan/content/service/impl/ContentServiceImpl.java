package com.lehuan.content.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lehuan.mapper.TbContentMapper;
import com.lehuan.pojo.TbContent;
import com.lehuan.pojo.TbContentExample;
import com.lehuan.pojo.TbContentExample.Criteria;
import com.lehuan.content.service.ContentService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        contentMapper.insert(content);
        //新增广告后清除缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }


    /**
     * 修改
     * 考虑到用户可能会修改广告的分类，这样需要把原分类的缓存和新分类的缓存都清除掉。
     * 新的分类要通过传进去的TbContent得到分类ID
     */
    @Override
    public void update(TbContent content) {
        //查询修改前的分类ID
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //清除原分组的缓存
        redisTemplate.boundHashOps("content").delete(categoryId);
        contentMapper.updateByPrimaryKey(content);
        //如果分类ID发生了修改,清除修改后的分类ID的缓存，这里的longValue是获取引用类型的值，因为categoryId是Long类型的，清除修改后的缓存
        if (categoryId.longValue() != content.getCategoryId().longValue()) {
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }


    /**
     * 批量删除
     * 删除广告后清除缓存
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //在删除之前清除缓存
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            redisTemplate.boundHashOps("content").delete(categoryId);
            contentMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }


    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
//        查询缓存，如果没有就查数据库
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (contentList == null) {
            System.out.println("从数据库读取数据放入缓存");
            //根据广告分类ID查询广告列表,创建条件
            TbContentExample example = new TbContentExample();
            Criteria criteria = example.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId);  //制定条件分类ID
            criteria.andStatusEqualTo("1");     // 制定广告状态：有效
            example.setOrderByClause("sort_order");     // 制定按照排序状态展示
            contentList = contentMapper.selectByExample(example);
//            存入缓存,否则从缓存中读取数据
            redisTemplate.boundHashOps("content").put(categoryId, contentList);
        } else {
            System.out.println("从缓存中读取数据");
        }
        return contentList;
    }
}
