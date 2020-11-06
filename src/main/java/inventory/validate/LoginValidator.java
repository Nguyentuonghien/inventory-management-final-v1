package inventory.validate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import inventory.model.Users;
import inventory.service.UserService;

@Component
public class LoginValidator implements Validator{

	@Autowired
	private UserService userService;
	
	// class cần được support(Users)
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Users.class;
	}

	// target: là các thông tin được gửi từ form(login) lên bao gồm username và password
	@Override
	public void validate(Object target, Errors errors) {
		Users user = (Users) target;
	
		// kiểm tra xem 2 trường ta điền vào form có rỗng không(username và password), nếu rỗng sẽ báo lỗi
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "message.required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "message.required");
		
		// username và password không rỗng(đã qua bước kiểm tra bên trên)
		if(!StringUtils.isEmpty(user.getUserName()) && !StringUtils.isEmpty(user.getPassword())) {
			List<Users> users = userService.findByProperty("userName", user.getUserName());
			
			// kiểm tra xem username trả về có khác null và khác rỗng không?
			if(user != null && !users.isEmpty()) {
				// có username được trả về, lấy ra đối tượng đầu tiên(username không trùng nhau) và ta so sánh password
				// users.get(0).getPassword() là password ta đã lưu trong DB và ta lấy ra,  user.getPassword() là password ta nhập từ form
				if(!users.get(0).getPassword().equals(user.getPassword())) {   
					// password không trùng nhau --> thông báo lỗi
					errors.rejectValue("password", "message.wrong.password");
				}
			}else {
				// không có username nào được trả về --> báo lỗi
				errors.rejectValue("userName", "message.wrong.username");
			}
		}
		
	}

}





