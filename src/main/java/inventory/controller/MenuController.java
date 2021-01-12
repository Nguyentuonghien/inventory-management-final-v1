package inventory.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import inventory.model.Auth;
import inventory.model.AuthForm;
import inventory.model.Menu;
import inventory.model.Paging;
import inventory.model.Role;
import inventory.service.MenuService;
import inventory.service.RoleService;
import inventory.util.Constant;

@Controller
public class MenuController {
	
	public static final Logger log = Logger.getLogger(MenuController.class);
	
	@Autowired
	private MenuService menuService;
	
	@Autowired
	private RoleService roleService;
	
	@RequestMapping(value = {"/menu/list/", "/menu/list"})
	public String ridirect() {
		return "redirect:/menu/list/1";
	}
	
	@RequestMapping("/menu/list/{page}")
	public String showMenuList(Model model, @ModelAttribute("searchForm") Menu menu, @PathVariable("page") int page, HttpSession session) {
		Paging paging = new Paging(15);
		paging.setIndexPage(page);
		List<Menu> menuList = menuService.getMenuList(menu, paging);
		List<Role> roleList = roleService.getRoleList(null, null);
		Collections.sort(roleList, (o1,o2) -> o1.getId()-o2.getId());
		// mỗi 1 menu sẽ chứa 1 map<roleId, permission>(ta dùng TreeMap<>:hỗ trợ lưu theo thứ tự), ta sẽ duyệt từng menu trong menuList 
		// rồi put các roleId và permission vào trong map<> của menu đó
		for(Menu menuItem : menuList) {
			Map<Integer, Integer> mapAuth = new TreeMap<>();
			for(Role role : roleList) {
				// khi ta chưa xét quyền -->permission mặc định = 0, giả sử sau vòng lặp này mapAuth có gtrị: (1-0),(2-0),(3-0)
				mapAuth.put(role.getId(), 0);
			}
			for(Object obj: menuItem.getAuths()) { 
				// trong Menu chứa Set<Auth>, sau vòng lặp này g/sử ta set quyền cho admin(permission=1) thì mapAuth có gtrị: (1-1),(2-0),(3-0)
				Auth auth = (Auth) obj;
				mapAuth.put(auth.getRole().getId(), auth.getPermission());
			}
			menuItem.setMapAuth(mapAuth);
		}
		if(session.getAttribute(Constant.MSG_SUCCESS) != null) {
			model.addAttribute(Constant.MSG_SUCCESS, session.getAttribute(Constant.MSG_SUCCESS));
			session.removeAttribute(Constant.MSG_SUCCESS);
		}
		if(session.getAttribute(Constant.MSG_ERROR) != null) {
			model.addAttribute(Constant.MSG_ERROR, session.getAttribute(Constant.MSG_ERROR));
			session.removeAttribute(Constant.MSG_ERROR);
		}
		model.addAttribute("pageInfo", paging);
		model.addAttribute("menuList", menuList);
		model.addAttribute("roleList", roleList);
		return "menu-list";
	}
	
	@GetMapping("/menu/change-status/{id}")
	public String changeStatus(@PathVariable("id") int id, HttpSession session) {
		try {
			// 
			menuService.changeStatus(id);
			session.setAttribute(Constant.MSG_SUCCESS, "Change status success!!!");
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute(Constant.MSG_ERROR, "Change status has error!!!");
		}
		return "redirect:/menu/list";
	}
	
	@GetMapping("/menu/permission")
	public String permission(Model model) {
		List<Role> roles = roleService.getRoleList(null, null);
		Map<Integer, String> mapRole = new HashMap<>();
		for(Role role : roles) {
			mapRole.put(role.getId(), role.getRoleName());
		}
		List<Menu> menus = menuService.getMenuList(null, null);
		Map<Integer, String> mapMenu = new HashMap<>();
		for(Menu menu : menus) {
			mapMenu.put(menu.getId(), menu.getUrl());
		}
		model.addAttribute("modelForm", new AuthForm());
		model.addAttribute("mapRole", mapRole);
		model.addAttribute("mapMenu", mapMenu);
		return "menu-permission";
	}
	
	@PostMapping("/menu/update-permission")
	public String updatePermission(Model model, @ModelAttribute("modelForm") AuthForm authForm, HttpSession session) {
		try {
			menuService.updatePermission(authForm);
			session.setAttribute(Constant.MSG_SUCCESS, "Update success!!!");
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute(Constant.MSG_ERROR, "Update has error!!!");
		}
		return "redirect:/menu/list";
	}
	
}





