package inventory.validate;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import inventory.model.ProductInfo;
import inventory.service.ProductService;

@Component
public class ProductInfoValidator implements Validator{

	@Autowired 
	private ProductService productService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == ProductInfo.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		ProductInfo productInfo = (ProductInfo) target;
		
		ValidationUtils.rejectIfEmpty(errors, "code", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "name", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "description", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "multipartFile", "message.required");
		
		// code không được trùng nhau:
		if (productInfo.getCode() != null) {
			List<ProductInfo> results = productService.findProductInfoByProperty("code", productInfo.getCode());
			// kiểm tra khi update: nếu có 1 record khác trong DB mà khác với bản ghi hiện
			// tại mà code lại giống nhau --> báo lỗi
			if (results != null && !results.isEmpty()) {
				if (productInfo.getId() != null && productInfo.getId() != 0) {
					// 2 bản ghi khác nhau nhưng có code giống nhau
					if (results.get(0).getId() != productInfo.getId()) {
						errors.rejectValue("code", "message.code.exist");
					}
				} else {
					errors.rejectValue("code", "message.code.exist");
				}
			}
		}
		// ta chỉ support các file có đuôi là "jpg" và "png"(kayn.jpg, kayn.png), còn các file khác ta sẽ k support
		if(productInfo.getMultipartFile() != null) {
			String extension = FilenameUtils.getExtension(productInfo.getMultipartFile().getOriginalFilename());
			if(!extension.equals("jpg") || !extension.equals("png")) {
				errors.rejectValue("multipartFile", "message.file.extension.error");
			}
		}
		
		
	}

}
