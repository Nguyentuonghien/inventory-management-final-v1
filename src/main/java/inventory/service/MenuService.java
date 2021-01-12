package inventory.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import inventory.dao.AuthDAO;
import inventory.dao.MenuDAO;
import inventory.model.Auth;
import inventory.model.AuthForm;
import inventory.model.Menu;
import inventory.model.Paging;
import inventory.model.Role;

@Service
public class MenuService {
	
	static final Logger log = Logger.getLogger(MenuService.class);
	
	@Autowired
	private MenuDAO<Menu> menuDAO;
	
	@Autowired
	private AuthDAO<Auth> authDAO;
	
	public List<Menu> getMenuList(Menu menu, Paging paging) {
		log.info("show all menu list:");
		StringBuilder queryStr = new StringBuilder();
		queryStr.append(" or model.activeFlag=0");
		Map<String, Object> mapParams = new HashMap<>();
		if(menu != null) {
			if(!StringUtils.isEmpty(menu.getUrl())) {
				queryStr.append(" and model.url like :url");
				mapParams.put("url", "%"+menu.getUrl()+"%");
			}
		}
		return menuDAO.findAll(queryStr.toString(), mapParams, paging);
	}
	
	public void changeStatus(Integer id) throws Exception{
		Menu menu = menuDAO.findById(Menu.class, id);
		if(menu != null) {
			// nếu activeFlag==1 ta set == 0 còn activeFlag==0 ta set == 1 
			menu.setActiveFlag(menu.getActiveFlag()==1 ? 0 : 1);
			menu.setUpdateDate(new Date());
			menuDAO.update(menu);
		}
	}
	
	public void updatePermission(AuthForm authForm) throws Exception{
		int roleId = authForm.getRoleId();
		int menuId = authForm.getMenuId();
		int permission = authForm.getPermission();
		Auth auth = authDAO.findAuth(roleId, menuId);
		// nếu tìm thấy auth trong DB ta sẽ update nó với permisson giống bên menu-permission: nếu tích là Yes==>permission=1, còn No thì permission=0
        // còn k tìm thấy ta sẽ kiểm tra nếu permission=1 ==> ta sẽ insert nó vào DB (permission=0 mặc định không có trong bảng Auth)
		if(auth != null) {
			auth.setPermission(permission);
			authDAO.update(auth);
		}else {
			if(permission == 1) {
				auth = new Auth();
				Role role = new Role();
				role.setId(roleId);
				Menu menu = new Menu();
				menu.setId(menuId);
				auth.setMenu(menu);
				auth.setRole(role);
				auth.setPermission(1);
				auth.setActiveFlag(1);
				auth.setCreateDate(new Date());
				auth.setUpdateDate(new Date());
				authDAO.save(auth);
			}
		}
	}
	
}






