package inventory.security;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;

import inventory.model.Auth;
import inventory.model.Menu;
import inventory.model.Role;
import inventory.model.UserRole;
import inventory.model.Users;
import inventory.util.Constant;

public class FilterSystem implements HandlerInterceptor{
	
	Logger logger = Logger.getLogger(FilterSystem.class);
	
	/**
	 * preHandle(): mọi request phải đi qua hàm này trước để xử lý trước khi tới controller, nếu trả về true thì request sẽ được gửi tới controller và ngược lại
	 * postHandle(): xử lý sau khi request tới controller
	 */
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		logger.info("[preHandle][" + request + "]" + "[" + request.getMethod() + "]" + request.getRequestURI());
		
		// để kiểm tra xem user đã login chưa? ta sẽ kiểm tra trong session(vì nếu đã login thì thông tin của user sẽ được lưu trong session)
		Users users = (Users) request.getAttribute(Constant.USER_INFO);
		
		// nếu k tìm thấy(không có trong session) --> trả về trang login với đường dẫn: http://localhost:8080/inventory_management/login
		if(users == null) {
			response.sendRedirect(request.getContextPath()+"/login");
			return false;
		}
		// nếu tìm thấy(có trong session) --> ta kiểm tra xem user đó có quyền truy cập vào menu nào?(là admin hay là user)
        if(users != null) {
        	String url = request.getServletPath();             //  trả về một giá trị chuỗi với đường dẫn của request
        	// kiểm tra nếu không có quyền --> trả về trang access-denied
        	if(!hasPermission(url, users)) {
        	   response.sendRedirect(request.getContextPath()+"/access-denied");
        	   return false;
        	}
        }		
		return true;
	}
	
	// hàm kiểm tra quyền truy cập
	private boolean hasPermission(String url, Users users) {
		
		// đây là các url cơ bản nên ta k cần check quyền mà mặc định sẽ bỏ qua
		if(url.contains("/index") || url.contains("/access-denied") || url.contains("/logout")) {
			return true;
		}
		
		// iterator().next(): trả về phần tử hiện tại và di chuyển con trỏ trỏ tới phần tử tiếp theo.
		UserRole userRole = (UserRole) users.getUserRoles().iterator().next(); 
		Role role = userRole.getRole();
		Set<Auth> auths = role.getAuths();
		for(Object object : auths){
			Auth auth = (Auth) object;
			Menu menu = auth.getMenu();
			// lấy ra url từ menu r kiểm tra xem url đấy có permission = 1 không? nếu = 1 sẽ trả về true --> được quyền access
			if(url.contains(menu.getUrl())) {            
				return auth.getPermission() == 1;
			}
		}
		
		return false;
	}
	
}






