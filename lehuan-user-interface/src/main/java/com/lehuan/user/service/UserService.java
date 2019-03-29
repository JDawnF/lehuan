package com.lehuan.user.service;
import java.util.List;
import com.lehuan.pojo.TbUser;

import entity.PageResult;
/**
 * 服务层接口
 * @author baichen
 *
 */
public interface UserService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbUser> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbUser user);
	
	
	/**
	 * 修改
	 */
	public void update(TbUser user);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbUser findOne(Long id);
	
	
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
	public PageResult findPage(TbUser user, int pageNum, int pageSize);
	
	
	/**
	 * 生成并发送发送短信验证码
	 * @param phone	手机号码
	 */
	public void createSmsCode(String phone);
	
	/**
	 * 判断短信验证码是否存在,前端用户提交的数据
	 * @param phone		手机号码
	 * @param code		验证密码
	 * @return
	 */
	public boolean checkSmsCode(String phone, String code);
	
}
