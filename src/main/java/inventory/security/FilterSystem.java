package inventory.security;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;

import inventory.model.Auth;
import inventory.model.UserRole;
import inventory.model.Users;
import inventory.util.Constant;

public class FilterSystem implements HandlerInterceptor{
	
	/**
	 * preHandle(): mọi request phải đi qua hàm này trước để xử lý trước khi tới controller, nếu trả về true thì request sẽ được gửi tới controller và ngược lại
	 * postHandle(): xử lý sau khi request tới controller
	 */
	
	Logger logger = Logger.getLogger(FilterSystem.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		logger.info("Request URI = "+request.getRequestURI());	
		// để kiểm tra xem user đã login chưa? ta sẽ kiểm tra trong session(vì nếu đã login thì thông tin của user sẽ được lưu trong session)
		// nếu k tìm thấy(không có trong session) --> trả về trang login với đường dẫn: http://localhost:8080/inventory_management/login
		// nếu tìm thấy(có trong session) --> ta kiểm tra xem user đó có quyền truy cập vào menu nào?(là admin hay là user)
		Users users = (Users)request.getSession().getAttribute(Constant.USER_INFO);
		if(users == null) {
			response.sendRedirect(request.getContextPath()+"/login");
			return false;
		}
        if(users != null) {
        	String url = request.getServletPath();        
        	// kiểm tra nếu không có quyền --> trả về trang access-denied
        	if(!hasPermission(url, users)) {
        	   logger.error("ACCESS DENIED URI ="+request.getRequestURI());
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
		UserRole userRole =(UserRole) users.getUserRoles().iterator().next();
		Set<Auth> auths = userRole.getRole().getAuths();
		for(Object obj : auths) {
			Auth auth = (Auth) obj;
			// lấy ra url từ menu r kiểm tra xem url đấy có permission = 1 không? nếu = 1 sẽ trả về true --> được quyền access
			if(url.contains(auth.getMenu().getUrl())) {
				return auth.getPermission() == 1;
			}
		}
		return false;
	}
	
}

