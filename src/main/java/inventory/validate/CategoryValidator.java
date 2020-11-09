package inventory.validate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import inventory.model.Category;
import inventory.service.ProductService;

@Component
public class CategoryValidator implements Validator{

	@Autowired
	private ProductService productService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Category.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		// target: là đối tượng category bao gồm các thông tin được gửi từ form lên bao gồm các trường ta sẽ validate: name , code , description	
		Category category = (Category) target;
		// 3 trường ta validate không được rỗng trên form
		ValidationUtils.rejectIfEmpty(errors, "code", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "name", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "description", "message.required");
		
		// code không được trùng nhau
		if(category.getCode() != null) {
			List<Category> categories = productService.findCategoryByProperty("code", category.getCode());
			if(categories != null && !categories.isEmpty()) {
				// code này đã tồn tại trong Database ==> báo lỗi
				errors.rejectValue("code", "message.code.exist");
				
			}
		}
	}

}
