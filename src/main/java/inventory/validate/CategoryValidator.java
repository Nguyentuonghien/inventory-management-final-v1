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
public class CategoryValidator implements Validator {

	@Autowired
	private ProductService productService;

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Category.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		// target: là đối tượng category, bao gồm các thông tin được gửi từ form lên bao
		// gồm các trường ta sẽ validate: name , code , description
		Category category = (Category) target;

		// 3 trường ta validate không được rỗng trên form
		ValidationUtils.rejectIfEmpty(errors, "code", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "name", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "description", "message.required");

		// code không được trùng nhau:
		if (category.getCode() != null) {
			List<Category> results = productService.findCategoryByProperty("code", category.getCode());
			// kiểm tra khi update: nếu có 1 record khác trong DB mà khác với bản ghi hiện tại mà code lại giống nhau --> báo lỗi
			if (results != null && !results.isEmpty()) {
				if (category.getId() != null && category.getId() != 0) {
					// 2 bản ghi khác nhau nhưng có code giống nhau
					if (results.get(0).getId() != category.getId()) {
						errors.rejectValue("code", "message.code.exist");
					}
				} else {
					errors.rejectValue("code", "message.code.exist");
				}
			}
		}

	}

}
