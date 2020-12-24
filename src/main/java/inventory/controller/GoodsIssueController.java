package inventory.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

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
public class GoodsIssueController {
	
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
		
		if(binder.getTarget().getClass() == Invoice.class) {
			binder.setValidator(invoiceValidator);
		}
	}
	
	@GetMapping(value = {"/goods-issue/list", "/goods-issue/list/"})
	public String redirect() {
		return "redirect:/goods-issue/list/1";
	}
	
	@GetMapping("/goods-issue/list/{page}")
	public String showInvoiceList(@ModelAttribute("searchForm") Invoice invoice, @PathVariable("page") int page, Model model, HttpSession session) {
		Paging paging = new Paging(5);
		paging.setIndexPage(page);
		List<Invoice> invoices = invoiceService.getListInvoice(invoice, paging);
		invoice.setType(Constant.TYPE_GOODS_ISSUES);
		if(session.getAttribute(Constant.MSG_SUCCESS) != null) {
			model.addAttribute(Constant.MSG_SUCCESS, session.getAttribute(Constant.MSG_SUCCESS));
			session.removeAttribute(Constant.MSG_SUCCESS);
		}
		if(session.getAttribute(Constant.MSG_ERROR) != null) {
			model.addAttribute(Constant.MSG_ERROR, session.getAttribute(Constant.MSG_ERROR));
			session.removeAttribute(Constant.MSG_ERROR);
		}
		model.addAttribute("searchForm", new Invoice());
		model.addAttribute("invoices", invoices);
		model.addAttribute("pageInfo", paging);
		return "goods-issue-list";
	}
	
	@GetMapping("/goods-issue/add")
	public String addInvoice(Model model) {
		model.addAttribute("viewOnly", false);
		model.addAttribute("titlePage", "Add Invoice");
		model.addAttribute("mapProducts", initMapProduct());
		return "goods-issue-list";
	}
	
	@GetMapping("/goods-issue/edit/{id}")
	public String editInvoice(Model model, @PathVariable("id") int id) {
		Invoice invoice = invoiceService.findInvoioceByProperty("id", id).get(0);
		if(invoice != null) {
			model.addAttribute("viewOnly", false);
			model.addAttribute("titlePage", "Edit Invoice");
			model.addAttribute("mapProducts", initMapProduct());
		}
		return "goods-issue-list";
	}
	
	@GetMapping("/goods-issue/view/{id}")
	public String viewInvoice(Model model, @PathVariable("id") int id) {
		Invoice invoice = invoiceService.findInvoioceByProperty("id", id).get(0);
		if(invoice != null) {
			model.addAttribute("viewOnly", true);
			model.addAttribute("titlePage", "View Invoice");
		}
		return "goods-issue-list";
	}
	
	@PostMapping("/goods-issue/save")
	public String saveInvoice(@ModelAttribute("modelForm") @Validated Invoice invoice, Model model, BindingResult result, HttpSession session) {
		if (result.hasErrors()) {
			if (invoice.getId() != null) {
				model.addAttribute("titlePage", "Edit Invoice");
			}else {
				model.addAttribute("titlePage", "Add Invoice");
			}
			model.addAttribute("modelForm", invoice);
			model.addAttribute("viewOnly", false);
			return "goods-issue-list";
		}
		
		invoice.setType(Constant.TYPE_GOODS_ISSUES);
		
		if (invoice.getId() != null && invoice.getId() != 0) {
			try {
				invoiceService.update(invoice);
				session.setAttribute(Constant.MSG_SUCCESS, "Update success!!!");
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute(Constant.MSG_ERROR, "Update has error!!!");
			}
			
		}else {
			try {
				invoiceService.save(invoice);
				session.setAttribute(Constant.MSG_SUCCESS, "Insert success!!!");
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute(Constant.MSG_ERROR, "Insert has error!!!");
			}
		}
		return "redirect:/goods-issue/list";
	}
	
	@GetMapping("/goods-issue/export")
	public ModelAndView exportReport() {
		ModelAndView modelAndView = new ModelAndView();
		Invoice invoice = new Invoice();
		invoice.setType(Constant.TYPE_GOODS_ISSUES);		
		List<Invoice> invoices = invoiceService.getListInvoice(invoice, null);
		modelAndView.addObject("invoices", invoices);
		modelAndView.setView(new InvoiceReport());
		return modelAndView;
	}
	
	// khi nhập hóa đơn ta phải nhập mã hóa đơn sau đó lựa chọn sản phẩm trong hóa đơn đó(select-box)
	private Map<String, String> initMapProduct() {
		List<ProductInfo> producInfos = productService.getAllProductInfo(null, null);
		Map<String, String> mapProduct = new HashMap<>();
		for (ProductInfo productInfo : producInfos) {
			// ta put vào id và tên của productInfo đó
			mapProduct.put(productInfo.getId().toString(), productInfo.getName());
		}
		return mapProduct;
	}
	
}









