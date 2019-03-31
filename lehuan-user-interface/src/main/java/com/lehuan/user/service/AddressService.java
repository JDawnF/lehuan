package com.lehuan.user.service;
import java.util.List;
import com.lehuan.pojo.TbAddress;

import entity.PageResult;
/**
 * 地址服务层接口
 * @author baichen
 *
 */
public interface AddressService {

	/**
	 * 返回所有地址列表
	 * @return
	 */
	public List<TbAddress> findAll();
	
	
	/**
	 * 返回所有地址分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加地址
	*/
	public void add(TbAddress address);
	
	
	/**
	 * 修改地址
	 */
	public void update(TbAddress address);
	

	/**
	 * 根据ID获取地址实体
	 * @param id
	 * @return
	 */
	public TbAddress findOne(Long id);
	
	
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
	public PageResult findPage(TbAddress address, int pageNum, int pageSize);

	/**
	 * 根据用户查询地址
	 * @param userId
	 * @return
	 */
	public List<TbAddress> findListByUserId(String userId);
	
}
