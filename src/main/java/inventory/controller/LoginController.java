package inventory.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import inventory.model.Auth;
import inventory.model.Menu;
import inventory.model.Role;
import inventory.model.UserRole;
import inventory.model.Users;
import inventory.service.UserService;
import inventory.util.Constant;
import inventory.validate.LoginValidator;

@Controller
public class LoginController {

	@Autowired
	private UserService userService;

	@Autowired
	private LoginValidator loginValidator;

	@InitBinder
	private void initBinder(WebDataBinder dataBinder) { // WebDataBinder: mang dữ liệu từ jsp lên
		if (dataBinder.getTarget() == null) return;
		// trong class LoginValidator ta chỉ support Class validate là Users(các class khác thì không)
		// và binder sẽ kiểm tra nếu đúng là class Users --> sẽ setValidator cho loginValidator
		if (dataBinder.getTarget().getClass() == Users.class) {
			dataBinder.setValidator(loginValidator);
		}
	}

	@GetMapping("/login")
	public String login(Model model) { 
		// khi login ta sẽ trả về 1 đối tượng Users rỗng để cho người dùng điền các thông tin vào form
		model.addAttribute("loginForm", new Users());
		return "login/login";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		// khi logout thì mọi thông tin về user và menu sẽ bị loại bỏ khỏi session
		session.removeAttribute(Constant.USER_INFO);
		session.removeAttribute(Constant.MENU_SESSION);
		return "redirect:/login";
	}
	
	@GetMapping("/access-denied")
	public String accessDenied() {
		return "access-denied";
	}
	
	// @ModelAttribute: khi form submit lên --> đẩy toàn bộ dữ liệu từ trong form này gồm(username, password) lên cho ta
	//                  và username, password này sẽ tự động được gán vào model là  Users
	// @Validated: sẽ gọi tới initBinder() --> và setValidator cho ta
	// BindingResult: kết quả trả về sau khi validated
	// HttpSession: khi user đăng nhập thành công --> save thông tin của user vào trong session(session sẽ chứa thông tin của user đó)
	@PostMapping("/processLogin")
	public String processLogin(Model model, @ModelAttribute("loginForm") @Validated Users users, BindingResult bindingResult, HttpSession session) {
		// khi ta nhập username và password trên form mà có lỗi
		if (bindingResult.hasErrors()) {
			return "login/login";
		}

		Users user = userService.findByProperty("userName", users.getUserName()).get(0);

		UserRole userRole = (UserRole) user.getUserRoles().iterator().next();

		// khai báo list menu cha
		List<Menu> menuList = new ArrayList<>();
		
		// khai báo list menu con
		List<Menu> menuChildList = new ArrayList<>();

		Role role = userRole.getRole();

		// vì role.getAuths() lấy ra 1 Set --> dùng Object
		for (Object object : role.getAuths()) {
			Auth auth = (Auth) object;
			Menu menu = auth.getMenu();
			// menu cha phải thỏa mãn 5 đk sau
			if (menu.getParentId() == 0 && menu.getOrderIndex() != -1 && menu.getActiveFlag() == 1 && auth.getPermission() == 1 && auth.getActiveFlag() == 1) {
				// url: "/category/list" sẽ biến thành "categorylistId" --> để ta gán vào thẻ id trên form html
				menu.setIdMenu(menu.getUrl().replace("/", "") + "Id"); // thay thế "/" = rỗng và thêm "Id"
				menuList.add(menu);
			} else if (menu.getParentId() != 0 && menu.getOrderIndex() != -1 && menu.getActiveFlag() == 1 && auth.getPermission() == 1 && auth.getActiveFlag() == 1) {
				menu.setIdMenu(menu.getUrl().replace("/", "") + "Id");
				menuChildList.add(menu);
			}
		}

		// dựa vào menu cha để ta add các menu con tương ứng của nó:
		for (Menu menu : menuList) {
			List<Menu> childList = new ArrayList<>();
			for (Menu childMenu : menuChildList) {
				// menu con lấy ra id = id menu cha của nó
				if (childMenu.getParentId() == menu.getId()) {
					childList.add(childMenu);   // add menu con
				}
			}
			menu.setChild(childList);
		}

		// sắp xếp menu cha trước
		sortMenu(menuList);

		// trong menu cha ta sắp xếp menu con:
		for (Menu menu : menuList) {
			sortMenu(menu.getChild());
		}

		// khi user login thành công ta sẽ có thông tin của user và menu --> lưu thông tin đó vào trong session
		session.setAttribute(Constant.MENU_SESSION, menuList);    // session.setAttribute("menuSession",menuList)
		session.setAttribute(Constant.USER_INFO, user);           // session.setAttribute("userInfo", user);

		return "redirect:/index";
	}

	// hàm sắp xếp menu theo thứ tự tăng dần
	private void sortMenu(List<Menu> menus) {
		Collections.sort(menus, new Comparator<Menu>() {
			public int compare(Menu o1, Menu o2) {
				return o1.getOrderIndex() - o2.getOrderIndex();  // tăng dần
			}
		});
	}
	
}






