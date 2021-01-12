package inventory.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import inventory.model.Paging;
import inventory.model.Role;
import inventory.model.UserRole;
import inventory.model.Users;
import inventory.service.RoleService;
import inventory.service.UserService;
import inventory.util.Constant;
import inventory.validate.UserValidator;

@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private UserValidator userValidator;
	
	public static final Logger log = Logger.getLogger(UserController.class);
	
	@InitBinder
	private void initBinder(WebDataBinder binder) {
		if(binder.getTarget()==null) {
			return;
		}
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
		// binder sẽ kiểm tra nếu đúng là class Users --> sẽ setValidator cho userValidator
		if(binder.getTarget().getClass()== Users.class) {
			binder.setValidator(userValidator);
		}
	} 
	
	@RequestMapping(value = {"/user/list", "/user/list/"})
	public String redirect() {
		return "redirect:/user/list/1";
	}
	
	@RequestMapping("/user/list/{page}")
	public String showUserList(@ModelAttribute("searchForm") Users user, @PathVariable("page") int page, Model model, HttpSession session) {
		Paging paging = new Paging(5);
		paging.setIndexPage(page);
		List<Users> users = userService.getListUser(user, paging);
		
		// nếu có thông báo(success or error) được lưu trong session ==> lấy nó ra và gửi nó qua "user-list" để hiển thị và đồng thời xóa nó luôn trong session
		if(session.getAttribute(Constant.MSG_SUCCESS) != null) {
			model.addAttribute(Constant.MSG_SUCCESS, session.getAttribute(Constant.MSG_SUCCESS));
			session.removeAttribute(Constant.MSG_SUCCESS);
		}
		if(session.getAttribute(Constant.MSG_ERROR) != null) {
			model.addAttribute(Constant.MSG_ERROR, session.getAttribute(Constant.MSG_ERROR));
			session.removeAttribute(Constant.MSG_ERROR);
		}
		
		model.addAttribute("users", users);
		model.addAttribute("pageInfo", paging);
		return "user-list";
	}
	
	@GetMapping("/user/add")
	public String addUser(Model model) {
		List<Role> roles = roleService.getRoleList(null, null);
		Map<String, String> mapRoles = new HashMap<>();
		for(Role role : roles) {
			mapRoles.put(String.valueOf(role.getId()), role.getRoleName());
		}
		model.addAttribute("mapRoles", mapRoles);
		model.addAttribute("modelForm", new Users());
		model.addAttribute("viewOnly", false);
		model.addAttribute("titlePage", "Add User");
		return "user-action";
	}
	
	@GetMapping("/user/edit/{id}")
	public String editUser(Model model, @PathVariable("id") int id) {
		log.info("Edit user with id="+id);
		Users user = userService.findUserById(id);
		if(user != null) {
			List<Role> roles = roleService.getRoleList(null, null);
			Map<String, String> mapRoles = new HashMap<>();
			for(Role role : roles) {
				mapRoles.put(String.valueOf(role.getId()), role.getRoleName());
			}
			UserRole userRole = user.getUserRoles().iterator().next();
			//user.setRoleID(userRole.getRole().getId());
		    Role role = userRole.getRole();
		    user.setRoleID(role.getId());
			model.addAttribute("mapRoles", mapRoles);
			model.addAttribute("modelForm", user);
			model.addAttribute("titlePage", "Edit User");
			model.addAttribute("viewOnly", false);
			// khi edit ta sẽ ẩn password đi để tránh việc sửa đổi password
		    model.addAttribute("editMode", true);
		    return "user-action";
		}
		return "redirect:/user/list";
	}
	
	@GetMapping("/user/view/{id}")
	public String viewUser(Model model, @PathVariable("id") int id) {
		log.info("View user with id="+id);
		Users user = userService.findUserById(id);
		if(user != null) {
			List<Role> roles = roleService.getRoleList(null, null);
			Map<String, String> mapRoles = new HashMap<>();
			for(Role role : roles) {
				mapRoles.put(String.valueOf(role.getId()), role.getRoleName());
			}
			model.addAttribute("mapRoles", mapRoles);
			model.addAttribute("modelForm", user);
			model.addAttribute("titlePage", "View User");
			model.addAttribute("viewOnly", true);
			// khi edit ta sẽ ẩn password đi để tránh việc sửa đổi password
		    model.addAttribute("editMode", true);
		    return "user-action";
		}
		return "redirect:/user/list";
	}
	
	@PostMapping("/user/save")
	public String saveUser(Model model, @ModelAttribute("modelForm") @Validated Users user, HttpSession session, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			if(user.getId() != null) {
				model.addAttribute("titlePage", "Edit User");
				model.addAttribute("editMode", true);
			}else {
				model.addAttribute("titlePage", "Add User");
			}
			List<Role> roles = roleService.getRoleList(null, null);
			Map<String, String> mapRoles = new HashMap<>();
			for(Role role : roles) {
				mapRoles.put(String.valueOf(role.getId()), role.getRoleName());
			}
			model.addAttribute("mapRoles", mapRoles);
			model.addAttribute("viewOnly", false);
			model.addAttribute("modelForm", user);
			return "user-action";
		}
		
		if(user.getId() != null && user.getId() != 0) {
			try {
				userService.updateUser(user);
				session.setAttribute(Constant.MSG_SUCCESS, "Update success!!!");	
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute(Constant.MSG_ERROR, "Update has error!!!");	
			}
		}else {
			try {
				userService.saveUser(user);
				session.setAttribute(Constant.MSG_SUCCESS, "Insert success!!!");
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute(Constant.MSG_ERROR, "Insert has error!!!");
			}
		}  
		return "redirect:/user/list";
	}
	
	@GetMapping("/user/delete/{id}")
	public String deleteUser(Model model , @PathVariable("id") int id, HttpSession session) {
		log.info("Delete user with id="+id);
		Users user = userService.findUserById(id);
		if(user != null) {
			try {
				userService.deleteUser(user);
				session.setAttribute(Constant.MSG_SUCCESS, "Delete success!!!");
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute(Constant.MSG_ERROR, "Delete has error!!!");
			}
		}
		return "redirect:/user/list";
	}
	
}













