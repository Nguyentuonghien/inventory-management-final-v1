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
import inventory.util.HashingPassword;

@Component
public class LoginValidator implements Validator{

	@Autowired
	private UserService userService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Users.class;  // class cần được support(Users)
	}

	// target: là đối tượng user bao gồm các thông tin được gửi từ form(login) lên bao gồm username và password của user
	@Override
	public void validate(Object target, Errors errors) {
		Users user = (Users) target;
	
		// kiểm tra xem 2 trường ta điền vào form có rỗng không(username và password), nếu rỗng sẽ báo lỗi
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "password", "message.required");
		
		// username và password không rỗng(đã qua bước kiểm tra bên trên)
		if(!StringUtils.isEmpty(user.getUserName()) && !StringUtils.isEmpty(user.getPassword())) {
			List<Users> users = userService.findByProperty("userName", user.getUserName());
			
			// kiểm tra xem username trả về có khác null và khác rỗng không?
			if(user != null && !users.isEmpty()) {
				// có username được trả về, lấy ra đối tượng đầu tiên(username không trùng nhau)
				// so sánh password ta nhập trong form(ta cũng sẽ mã hóa password) vs password lưu trong DB, nếu password không trùng nhau ==> thông báo lỗi
				if(!users.get(0).getPassword().equals(HashingPassword.encript(user.getPassword()))) {   
					errors.rejectValue("password", "message.wrong.password");
				}
			}else {
				// không có username nào được trả về --> báo lỗi
				errors.rejectValue("userName", "message.wrong.username");
			}
		}
		
	}

}





