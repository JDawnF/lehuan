package com.lehuan.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import com.lehuan.group.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lehuan.mapper.TbSpecificationMapper;
import com.lehuan.mapper.TbSpecificationOptionMapper;
import com.lehuan.pojo.TbSpecification;
import com.lehuan.pojo.TbSpecificationExample;
import com.lehuan.pojo.TbSpecificationExample.Criteria;
import com.lehuan.pojo.TbSpecificationOption;
import com.lehuan.pojo.TbSpecificationOptionExample;
//import com.lehuan.pojo.group.Specification;
import com.lehuan.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 *
 * @author baichen
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {
        // 保存规格,会返回一个主键，可以看mapper.xml
        specificationMapper.insert(specification.getSpecification());
        // 遍历并保存规格选项
        for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
            // 设置规格的ID:
            specificationOption.setSpecId(specification.getSpecification().getId());
            // 新增规格
            specificationOptionMapper.insert(specificationOption);
        }
    }


    /**
     * 修改，可能是增加或者删除规格选项
     */
    @Override
    public void update(Specification specification) {
        // 获取规格实体
        TbSpecification tbSpecification = specification.getSpecification();
        // 修改规格
        specificationMapper.updateByPrimaryKey(tbSpecification);

        // 先删除规格选项，再添加规格选项
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(tbSpecification.getId());
        specificationOptionMapper.deleteByExample(example);

        // 获取规格选项集合
        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
        // 保存规格选项
        for (TbSpecificationOption specificationOption : specificationOptionList) {
            // 设置规格的ID:
            specificationOption.setSpecId(tbSpecification.getId());
            // 新增规格
            specificationOptionMapper.insert(specificationOption);
        }
    }

    /**
     * 根据ID获取规格组合实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification();
        // 根据规格ID查询获得规格对象
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        specification.setSpecification(tbSpecification);

        // 根据规格的ID查询获得规格选项列表，创建查询条件
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> list = specificationOptionMapper.selectByExample(example);
        specification.setSpecificationOptionList(list);
//      组合实体类
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            // 删除规格
            specificationMapper.deleteByPrimaryKey(id);
            // 删除规格选项:
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
