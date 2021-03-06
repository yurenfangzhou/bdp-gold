package com.platform.api.controller.shop;

import com.platform.bean.constant.factory.PageFactory;
import com.platform.bean.entity.shop.ShopUser;
import com.platform.bean.exception.ApplicationException;
import com.platform.bean.exception.ApplicationExceptionEnum;
import com.platform.bean.vo.front.Rets;
import com.platform.bean.vo.query.SearchFilter;
import com.platform.service.shop.CartService;
import com.platform.service.shop.OrderService;
import com.platform.service.shop.ShopUserService;
import com.platform.utils.DateUtil;
import com.platform.utils.Maps;
import com.platform.utils.StringUtil;
import com.platform.utils.factory.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shop/user")
public class ShopUserController {
	private  Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ShopUserService shopUserService;
	@Autowired
	private CartService cartService;
	@Autowired
	private OrderService orderService;

	@RequestMapping(value = "/list",method = RequestMethod.GET)
	public Object list(@RequestParam(required = false) String mobile, @RequestParam(required = false) String nickName,
					   @RequestParam(required = false) String startRegDate,
					   @RequestParam(required = false) String endRegDate,
					   @RequestParam(required = false) String startLastLoginTime,
					   @RequestParam(required = false) String endLastLoginTime) {
		Page<ShopUser> page = new PageFactory<ShopUser>().defaultPage();
		page.addFilter("mobile",mobile);
		page.addFilter("nickName",nickName);
		if(StringUtil.isNotEmpty(startRegDate)) {
			page.addFilter("createTime", SearchFilter.Operator.GTE, DateUtil.parseTime(startRegDate+" 00:00:00"));
		}
		if(StringUtil.isNotEmpty(endRegDate)) {
			page.addFilter("createTime", SearchFilter.Operator.LTE, DateUtil.parseTime(endRegDate+" 23:59:59"));
		}
		if(StringUtil.isNotEmpty(startLastLoginTime)) {
			page.addFilter("lastLoginTime", SearchFilter.Operator.GTE, DateUtil.parseTime(startLastLoginTime+" 00:00:00"));
		}
		if(StringUtil.isNotEmpty(endLastLoginTime)) {
			page.addFilter("lastLoginTime", SearchFilter.Operator.LTE, DateUtil.parseTime(endLastLoginTime+" 23:59:59"));
		}
		page = shopUserService.queryPage(page);
		return Rets.success(page);
	}

	@RequestMapping(value="{id}",method = RequestMethod.GET)
	public Object get(@PathVariable("id") Long id){
		if (id == null) {
			throw new ApplicationException(ApplicationExceptionEnum.REQUEST_NULL);
		}
		return Rets.success(shopUserService.get(id));
	}

	@RequestMapping(value="/info/{id}",method = RequestMethod.GET)
	public Object getInfo(@PathVariable("id") Long id){
		if (id == null) {
			throw new ApplicationException(ApplicationExceptionEnum.REQUEST_NULL);
		}
		ShopUser shopUser = shopUserService.get(id);

		SearchFilter filter = SearchFilter.build("idUser",id);
		Long  cartCount = cartService.count(filter);
		Long orderCount = orderService.count(filter);
		ShopUser shopUser1 = new ShopUser();
		BeanUtils.copyProperties(shopUser,shopUser1,"password","salt");
		Map<String,Object> data = Maps.newHashMap(
				"cartCount",cartCount,
				"orderCount",orderCount,
				"info",shopUser1
		);
		return Rets.success(data);
	}
}
