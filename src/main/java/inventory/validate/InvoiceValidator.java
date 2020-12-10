package inventory.validate;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import inventory.model.Invoice;
import inventory.service.InvoiceService;

@Component
public class InvoiceValidator implements Validator{

	@Autowired
	private InvoiceService invoiceService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Invoice.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		Invoice invoice = (Invoice) target;
		
		ValidationUtils.rejectIfEmpty(errors, "code", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "qty", "message.required");
		ValidationUtils.rejectIfEmpty(errors, "price", "message.required");
		
		// code không được trùng nhau:
		if (invoice.getCode() != null) {
			List<Invoice> invoices = invoiceService.findInvoioceByProperty("code", invoice.getCode());
			// kiểm tra khi update: nếu có 1 record khác trong DB mà khác với bản ghi hiện
			// tại mà code lại giống nhau --> báo lỗi
			if (invoices != null && !invoices.isEmpty()) {
				if (invoice.getId() != null && invoice.getId() != 0) {       // edit
					// 2 bản ghi khác nhau nhưng có code giống nhau
					if (invoices.get(0).getId() != invoice.getId()) {
						errors.rejectValue("code", "message.code.exist");
					}
				} else {                                                     // add
					errors.rejectValue("code", "message.code.exist");
				}
			}
		}
		if(invoice.getQty() < 0) {
			errors.rejectValue("qty", "message.wrong.format");
		}
		// price phải > 0
		if (invoice.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			errors.rejectValue("price", "msg.wrong.format");
		}
		// nếu fromDate mà ở sau toDate ==> báo lỗi fromDate k hợp lệ
		if(invoice.getFromDate() != null && invoice.getToDate() != null) {
			if(invoice.getFromDate().after(invoice.getToDate())) {
				errors.rejectValue("fromDate", "message.wrong.date");
			}
		}
	
	}

}



