package com.lehuan.sellergoods.service;
import java.util.List;

import com.lehuan.group.Goods;
import com.lehuan.pojo.TbGoods;

import com.lehuan.pojo.TbItem;
import entity.PageResult;
/**
 * 服务层接口
 * @author baichen
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();


	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加,接收组合实体类
	 */
	public void add(Goods goods);


	/**
	 * 修改
	 */
	public void update(Goods goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);
	/**
	 * 批量修改状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long[] ids, String status);

	/**
	 * 根据SPU的商品ID和状态查询Item表信息,SKU列表
	 * @param goodsIds		SPU的ID集合
	 * @param status		商品状态
	 * @return				商品列表
	 */
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status);



}
