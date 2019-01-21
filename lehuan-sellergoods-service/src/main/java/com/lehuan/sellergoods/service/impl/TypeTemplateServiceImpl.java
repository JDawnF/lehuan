package com.lehuan.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lehuan.mapper.TbSpecificationOptionMapper;
import com.lehuan.mapper.TbTypeTemplateMapper;
import com.lehuan.pojo.TbSpecificationOption;
import com.lehuan.pojo.TbSpecificationOptionExample;
import com.lehuan.pojo.TbTypeTemplate;
import com.lehuan.pojo.TbTypeTemplateExample;
import com.lehuan.pojo.TbTypeTemplateExample.Criteria;
import com.lehuan.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;
    //    规格选项
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;
//    @Autowired
//    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);

        //存入数据到缓存
//        saveToRedis();
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 将品牌列表与规格列表放入缓存
     */
//    private void saveToRedis() {
//        List<TbTypeTemplate> templateList = findAll();    //查出所有模板数据
//        //循环模板
//        for (TbTypeTemplate template : templateList) {
//            //得到品牌列表，模板ID：brandList，是一个json数据，转换为列表
//            List brandList = JSON.parseArray(template.getBrandIds(), Map.class);
//            redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);
//            //得到规格列表，需要根据规格ID查询规格选项
//            List<Map> specList = findSpecList(template.getId());
//            redisTemplate.boundHashOps("specList").put(template.getId(), specList);
//
//        }
//        //System.out.println("缓存品牌列表");
//    }

    @Override
    public List<Map> findSpecList(Long id) {
        //根据ID查询到模板对象
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        // 获得规格的数据spec_ids
        // [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();
        // 将specIds的字符串转成JSON的List<Map>
        List<Map> list = JSON.parseArray(specIds, Map.class);
        // 获得每条记录,注意类型是map:
        for (Map map : list) {
            // 根据规格的ID 查询规格选项的数据:
            // 设置查询条件,根据规格表的id查询:
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(new Long((Integer) map.get("id")));
//          条件查询
            List<TbSpecificationOption> specOptionList = specificationOptionMapper.selectByExample(example);
//          再给这个map添加一个key-value，//{"id":27,"text":"网络",options:[{id：xxx,optionName:移动2G}]}
            map.put("options", specOptionList);
        }
        return list;
    }

}
