package inventory.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import inventory.model.Invoice;
import inventory.model.Paging;
import inventory.model.ProductInfo;
import inventory.service.InvoiceReport;
import inventory.service.InvoiceService;
import inventory.service.ProductService;
import inventory.util.Constant;
import inventory.validate.InvoiceValidator;

@Controller
public class GoodsReceiptController  {
	
	private static final Logger log = Logger.getLogger(GoodsReceiptController.class);
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private InvoiceValidator invoiceValidator;
	
	@InitBinder
	private void initBinder(WebDataBinder binder) {
		if(binder.getTarget() == null) return;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		
		// trong class InvoiceValidator ta chỉ support Class validate là  Invoice(các class khác thì không) 
		// và binder sẽ kiểm tra nếu đúng là class Invoice --> sẽ setValidator cho invoiceValidator
		if(binder.getTarget().getClass() == Invoice.class) {
			binder.setValidator(invoiceValidator);
		}
	}
	
    @RequestMapping(value= {"/goods-receipt/list","/goods-receipt/list/"})
	public String redirect() {
		return "redirect:/goods-receipt/list/1";
	}
	
	// @ModelAttribute: khi form search submit lên --> mọi thông tin từ "searchForm" này sẽ được gán vào Invoice
	@RequestMapping(value = "/goods-receipt/list/{page}")      // page: trang hiện tại, từ page này ta sẽ tính ra  offset tương ứng
	public String showInvoiceList(Model model, HttpSession session, @ModelAttribute("searchForm") Invoice invoice, @PathVariable(value = "page") int page) {
		Paging paging = new Paging(5);
		paging.setIndexPage(page);
		
		if(invoice == null) {
			invoice = new Invoice();
		}
		// set type là goods-receipt(nhập hàng)
		invoice.setType(Constant.TYPE_GOODS_RECEIPT);
		
		List<Invoice> invoices = invoiceService.getListInvoice(invoice, paging);
		
		// nếu có thông báo(success hoặc error) được lưu trong session thì ta sẽ lấy nó ra và gửi nó qua "goods-receipt-list" 
		// và đồng thời xóa nó luôn trong session
		if(session.getAttribute(Constant.MSG_SUCCESS) != null) {
			model.addAttribute(Constant.MSG_SUCCESS, session.getAttribute(Constant.MSG_SUCCESS));
			session.removeAttribute(Constant.MSG_SUCCESS);
		}
		if(session.getAttribute(Constant.MSG_ERROR) != null) {
			model.addAttribute(Constant.MSG_ERROR, session.getAttribute(Constant.MSG_ERROR));
			session.removeAttribute(Constant.MSG_ERROR);
		}
		model.addAttribute("invoices", invoices);
		model.addAttribute("pageInfo", paging);
		return "goods-receipt-list";
	}
	
	@GetMapping("/goods-receipt/add")
	public String addInvoice(Model model) {
		// trả về 1 đối tượng Invoice rỗng cho người dùng điền vào các thông tin trên form
		model.addAttribute("modelForm", new Invoice());
		// gán title động tương ứng với 3 màn hình add, edit và view(tái sử dụng form vì màn hình add, edit và view giống nhau, chỉ khác nhau title)
		model.addAttribute("titlePage", "Add Invoice");
		// màn hình add thì được sửa các thông tin trên form(viewOnly==false)
		model.addAttribute("viewOnly", false);       
		model.addAttribute("mapProduct", initMapProduct());
		return "goods-receipt-action";
	}
	
	@GetMapping("/goods-receipt/edit/{id}")
	public String editInvoice(Model model, @PathVariable("id") int id) {
		log.info("Edit invoice with id: "+id);
		Invoice invoice = invoiceService.findInvoioceByProperty("id", id).get(0);      // tìm đối tượng invoice trong DB theo id
		if(invoice != null) {
			model.addAttribute("titlePage", "Edit Invoice");
			model.addAttribute("modelForm", invoice);         // lúc này ta đã có đối tượng invoice vừa tìm thấy trong DB rồi
			// màn hình edit thì được sửa các thông tin trên form(viewOnly==false)
			model.addAttribute("viewOnly", false);
			model.addAttribute("mapProduct", initMapProduct());
			return "goods-receipt-action";
		}	
		return "redirect:/goods-receipt/list";
	}
	
	@GetMapping("/goods-receipt/view/{id}")
	public String viewInvoice(Model model, @PathVariable("id") int id) {
		log.info("View invoice by id: "+id);
		Invoice invoice = invoiceService.findInvoioceByProperty("id", id).get(0);      // tìm đối tượng invoice trong DB theo id
		if(invoice != null) {
			model.addAttribute("titlePage", "View Invoice");
			model.addAttribute("modelForm", invoice);         // lúc này ta đã có đối tượng invoice vừa tìm thấy trong DB rồi
			// màn hình view thì không được sửa các thông tin trên form(viewOnly==true)
			model.addAttribute("viewOnly", true);
			return "goods-receipt-action";
		}	
		return "redirect:/goods-receipt/list";
	}
	
	// @ModelAttribute: khi form "goods-receipt-action" được submit lên ==> mọi thông tin từ "modelForm" này sẽ được gán vào Invoice
	@PostMapping("/goods-receipt/save")
	public String saveInvoice(Model model, @ModelAttribute("modelForm") @Validated Invoice invoice, BindingResult result, HttpSession session) {
		// 2 màn hình add và edit khi validate lỗi ==> quay trở lại form "goods-receipt-action" với đầy đủ các thông tin
		if(result.hasErrors()) {
			if(invoice.getId() != null) {
				model.addAttribute("titlePage", "Edit Invoioce");
			}else {
				model.addAttribute("titlePage", "Add Invoioce");
			}
			model.addAttribute("modelForm", invoice);
			model.addAttribute("viewOnly", false);
			model.addAttribute("mapProduct", initMapProduct());
			return "goods-receipt-action";
		}
		
		// set type là goods-receipt(nhập hàng)
		invoice.setType(Constant.TYPE_GOODS_RECEIPT);
		
		// vì màn hình add và edit dùng chung 1 form ==> để phân biệt add và edit: nếu là add thì không có id còn
		// nếu là edit ==> có id đi kèm(vì record đó đã tồn tại trong DB) và id đó khác 0 và khác null 
		// (Vd: id trong DB có giá trị = 1 nhưng nếu ta nhập vào id=10 ==> trả về null vì k tồn tại id=10 trong DB)
		if(invoice.getId() !=null && invoice.getId() != 0) {
			try {
				invoiceService.update(invoice);
				session.setAttribute(Constant.MSG_SUCCESS, "Update success!!!");     // lưu thông báo thành công trong session(không phải model)
			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
				session.setAttribute(Constant.MSG_ERROR, "Update has error!!!");     // lưu thông báo lỗi trong session
			}
		}else {
			try {
				invoiceService.save(invoice);
				session.setAttribute(Constant.MSG_SUCCESS, "Insert success!!!"); 
			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
				session.setAttribute(Constant.MSG_ERROR, "Insert has error!!!"); 
			}
		}
		return "redirect:/goods-receipt/list";
	}
	
	@GetMapping("/goods-receipt/export")
	public ModelAndView exportReport() {
		ModelAndView modelAndView = new ModelAndView();
		Invoice invoice = new Invoice();
		invoice.setType(Constant.TYPE_GOODS_RECEIPT);
		// lấy toàn bộ dữ liệu từ goods-recipt ra(không cần phân trang)
		List<Invoice> invoices = invoiceService.getListInvoice(invoice, null);
		modelAndView.addObject(Constant.KEY_GOODS_RECEIPT_REPORT, invoices);
		modelAndView.setView(new InvoiceReport());
		return modelAndView;
	}
	
	// khi nhập hóa đơn ta phải nhập mã hóa đơn sau đó lựa chọn sản phẩm trong hóa đơn đó(select-box)
	private Map<String, String> initMapProduct() {
		List<ProductInfo> producInfos = productService.getAllProductInfo(null, null);
		Map<String, String> mapProduct = new HashMap<>();
		for(ProductInfo productInfo : producInfos) {
			// ta put vào id và tên của productInfo đó
			mapProduct.put(productInfo.getId().toString(), productInfo.getName());
		}
		return mapProduct;
	}
}






