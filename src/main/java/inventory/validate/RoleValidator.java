package inventory.validate;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import inventory.model.Role;

@Component
public class RoleValidator implements Validator{

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Role.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		Role role = (Role) target;
		// validate cho 2 trường là roleName và description
		ValidationUtils.rejectIfEmpty(errors, "roleName", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "description", "message.required");
	}
	
}
