package com.lehuan.sellergoods.service;

import com.lehuan.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 商品品牌接口
 * @author Administrator
 *
 */
public interface BrandService {
//	返回品牌列表
	public List<TbBrand> findAll();

	/**
	 * 返回品牌分页列表
	 * @param pageNum   当前页面
	 * @param pageSize	每页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	/**
	 * 按条件返回品牌分页列表,因为有多个查询条件，所以用TbBrand类型参数接收
	 * @param pageNum   当前页面
	 * @param pageSize	每页记录数
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);
//	增加品牌接口
	public void add(TbBrand brand);
//	根据ID查询实体
	public TbBrand findOne(Long id);
//	修改
	public void update(TbBrand brand);
//	批量删除
	public void delete(Long[] ids);
	/**
	 * 品牌下拉框数据
	 */
	List<Map> selectOptionList();
}
