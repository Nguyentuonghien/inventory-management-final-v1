package inventory.validate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import inventory.model.Users;
import inventory.service.UserService;

@Component
public class UserValidator implements Validator{

	@Autowired
	private UserService userService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Users.class;
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		// target: đại diện cho đối tượng user bao gồm các thông tin được gửi từ form(login) lên bao gồm userName và password của user
		Users user = (Users) target;
		// validate cho 2 trường là userName và password
		ValidationUtils.rejectIfEmpty(errors, "userName", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "password", "message.required");
		
		// user rỗng ta sẽ báo lỗi
		if(user.getId() == null) {
			ValidationUtils.rejectIfEmpty(errors, "name", "message.required");
		}		
		// "userName" không được trùng nhau, nếu trùng userName ==> báo lỗi 
		List<Users> users = userService.findByProperty("userName", user.getUserName());
		if(users != null && !users.isEmpty()) {
			errors.rejectValue("userName", "message.username.exist");
		}
	}

}



