package com.lehuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lehuan.mapper.TbBrandMapper;
import com.lehuan.pojo.TbBrand;
import com.lehuan.pojo.TbBrandExample;
import com.lehuan.pojo.TbBrandExample.Criteria;
import com.lehuan.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @program: lehuan-parent
 * @description: 商品品牌实现类
 * @author: baichen
 *  2018-09-29 16:19
 **/
@Service
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
//        mybatis的分页，这样子写好之后会自动进行分页
        PageHelper.startPage(pageNum, pageSize);
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
//        返回总记录数和结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
        //        mybatis的分页，这样子写好之后会自动进行分页
        PageHelper.startPage(pageNum, pageSize);
//        创建查询条件
        TbBrandExample example = new TbBrandExample();
        Criteria criteria = example.createCriteria();
//        根据前端用户的选择，传过来不同的值，进行不同的模糊查询
        if (brand != null) {
            //  判断品牌名称是否为空，并且是否为空字符串
            if (brand.getName() != null && brand.getName().length() > 0) {
                criteria.andNameLike("%" + brand.getName() + "%");
            }
            // 判断品牌首字母是否为空，并且是否为空字符串
            if (brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        // 用分页Page类作为接受查询结果的类型
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
//        返回总记录数和结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(TbBrand brand) {
//        判断是否重名
        List<TbBrand> brandList = brandMapper.selectByExample(null);
        for (TbBrand tbBrand : brandList) {
            String name = tbBrand.getName();
            if (brand.getName().equals(name)) {
                return;
            }
        }
        brandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
