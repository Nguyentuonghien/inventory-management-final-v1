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

import inventory.model.Category;
import inventory.model.Paging;
import inventory.model.ProductInfo;
import inventory.service.ProductService;
import inventory.util.Constant;
import inventory.validate.ProductInfoValidator;

/** @InitBinder: nếu ta muốn tùy chỉnh request được gửi đến controller, nó được định nghĩa trong controller, giúp kiểm soát và định dạng mọi request đến với nó. 
 *               Nó còn được sử dụng với các phương thức khởi tạo WebDataBinder và hoạt động như một bộ xử lý trước(preprocessor) cho mỗi request đến controller.
 */

@Controller
public class ProductInfoController {
	
	private static final Logger log = Logger.getLogger(ProductInfoController.class);
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ProductInfoValidator productInfoValidator;
	
	@InitBinder
	private void initBinder(WebDataBinder binder) {
		if(binder.getTarget() == null) return;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		
		// trong class productInfoValidator ta chỉ support Class validate là  ProductInfo(các class khác thì không) 
		// và binder sẽ kiểm tra nếu đúng là class ProductInfo --> sẽ setValidator cho productInfoValidator
		if(binder.getTarget().getClass() == ProductInfo.class) {
			binder.setValidator(productInfoValidator);
		}
	}
	
	// với những url này thì mặc định ta sẽ chuyên hướng về trang đầu tiên(page=1)
    @RequestMapping(value= {"/product-info/list","/product-info/list/"})	
	public String redirect() {
		return "redirect:/product-info/list/1";
	}
	
	// @ModelAttribute: khi form search submit lên --> mọi thông tin từ "searchForm" này sẽ được gán vào ProductInfo
	@RequestMapping(value = "/product-info/list/{page}")      // page: trang hiện tại, từ page này ta sẽ tính ra  offset tương ứng
	public String showProductInfoList(Model model, HttpSession session, @ModelAttribute("searchForm") ProductInfo productInfo, @PathVariable(value = "page") int page) {
		Paging paging = new Paging(5);
		paging.setIndexPage(page);
		List<ProductInfo> productInfos = productService.getAllProductInfo(productInfo, paging);
		
		// nếu có thông báo(success hoặc error) được lưu trong session thì ta sẽ lấy nó ra và gửi nó qua "productInfo-list" 
		// và đồng thời xóa nó luôn trong session
		if(session.getAttribute(Constant.MSG_SUCCESS) != null) {
			model.addAttribute(Constant.MSG_SUCCESS, session.getAttribute(Constant.MSG_SUCCESS));
			session.removeAttribute(Constant.MSG_SUCCESS);
		}
		if(session.getAttribute(Constant.MSG_ERROR) != null) {
			model.addAttribute(Constant.MSG_ERROR, session.getAttribute(Constant.MSG_ERROR));
			session.removeAttribute(Constant.MSG_ERROR);
		}
		model.addAttribute("productInfos", productInfos);
		model.addAttribute("pageInfo", paging);
		return "productInfo-list";
	}
	
	@GetMapping("/category/add")
	public String addProductInfo(Model model) {
		// trả về 1 đối tượng Category rỗng cho người dùng điền vào các thông tin trên form(code, name và description)
		model.addAttribute("modelForm", new Category());
		// gán title động tương ứng với 3 màn hình add, edit và view(tái sử dụng form vì màn hình add, edit và view giống nhau, chỉ khác nhau title)
		model.addAttribute("titlePage", "Add Category");
		// màn hình add thì được sửa các thông tin trên form(viewOnly==false)
		model.addAttribute("viewOnly", false);  
		
		// khi ta add 1 productInfo --> ta sẽ chọn category(dạng select-box) cho productInfo đó
		List<Category> categories = productService.getAllCategory(null, null);
		Map<String, String> mapCategory = new HashMap<>();
		for(Category category : categories) {
			// ta gán key:id và value:tên của category đó
			mapCategory.put(String.valueOf(category.getId()), category.getName());
		}
		model.addAttribute("mapCategory", mapCategory);
		return "productInfo-action";
	}
	
	@GetMapping("/product-info/edit/{id}")
	public String editProductInfo(Model model, @PathVariable("id") int id) {
		log.info("Edit product-info with id: "+id);
		ProductInfo productInfo = productService.findProductInfoById(id);       // tìm đối tượng productInfo trong DB theo id
		if(productInfo != null) {
			// khi ta edit 1 productInfo --> ta sẽ edit category(dạng select-box) tương ứng cho productInfo đó
			List<Category> categories = productService.getAllCategory(null, null);
			Map<String, String> mapCategory = new HashMap<>();
			for(Category category : categories) {
				// ta gán key:id và value:tên của category đó
				mapCategory.put(String.valueOf(category.getId()), category.getName());
			}
			model.addAttribute("mapCategory", mapCategory);
			model.addAttribute("titlePage", "Edit ProductInfo");
			model.addAttribute("modelForm", productInfo);                      // lúc này ta đã có đối tượng productInfo vừa tìm thấy trong DB rồi
			// màn hình edit thì được sửa các thông tin trên form(viewOnly==false)
			model.addAttribute("viewOnly", false);
			return "productInfo-action";
		}			
		return "redirect:/product-info/list";
	}
	
	@GetMapping("/product-info/view/{id}")
	public String viewProductInfo(Model model, @PathVariable("id") int id) {
		log.info("View product-info by id: "+id);
		ProductInfo productInfo = productService.findProductInfoById(id);       // tìm đối tượng productInfo trong DB theo id
		if(productInfo != null) {
			model.addAttribute("titlePage", "View ProductInfo");
			model.addAttribute("modelForm", productInfo);                          // lúc này ta đã có đối tượng productInfo vừa tìm thấy trong DB rồi
			// màn hình view thì không được sửa các thông tin trên form(viewOnly==true)
			model.addAttribute("viewOnly", true);
			return "productInfo-action";
		}	
		return "redirect:/product-info/list";
	}
	
	@PostMapping("/product-info/save")
	public String saveProductInfo(Model model, @ModelAttribute("modelForm") @Validated ProductInfo productInfo, BindingResult result, HttpSession session) {
		// 2 màn hình add và edit khi validate lỗi (khi ta nhập code,name,description trên form mà có lỗi)
		if(result.hasErrors()) {
			if(productInfo.getId() != null) {
				model.addAttribute("titlePage", "Edit ProductInfo");
			}else {
				model.addAttribute("titlePage", "Add ProductInfo");
			}
			List<Category> categories = productService.getAllCategory(null, null);
			Map<String, String> mapCategory = new HashMap<>();
			for(Category category : categories) {
				// ta gán key:id và value:tên của category đó
				mapCategory.put(String.valueOf(category.getId()), category.getName());
			}
			model.addAttribute("mapCategory", mapCategory);
			model.addAttribute("modelForm", productInfo);
			model.addAttribute("viewOnly", false);
			return "productInfo-action";
		}
		
		// vì màn hình add và edit dùng chung 1 form ==> để phân biệt add và edit: nếu là add thì không có id còn
		// nếu là edit ==> có id đi kèm(vì record đó đã tồn tại trong DB) và id đó khác 0 và khác null 
		// (Vd: id trong DB có giá trị = 1 nhưng nếu ta nhập vào id=10 ==> trả về null vì k tồn tại id=10 trong DB)
		if(productInfo.getId() != null && productInfo.getId() != 0) {
			try {
				productService.updateProductInfo(productInfo);
				session.setAttribute(Constant.MSG_SUCCESS, "Update success!!!");     // lưu thông báo thành công trong session(không phải model)
			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
				session.setAttribute(Constant.MSG_ERROR, "Update has error!!!");     // lưu thông báo lỗi trong session
			}
		}else {
			try {
				productService.saveProductInfo(productInfo);
				session.setAttribute(Constant.MSG_SUCCESS, "Insert success!!!"); 
			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
				session.setAttribute(Constant.MSG_ERROR, "Insert has error!!!"); 
			}
		}
		return "redirect:/product-info/list";
	}
	
	@GetMapping("/product-info/delete/{id}")
	public String deleteProductInfo(Model model, @PathVariable("id") int id, HttpSession session) {
		log.info("Delete product-info by id: "+id);
		ProductInfo productInfo = productService.findProductInfoById(id);
		if(productInfo != null) {
			try {
				productService.deleteProductInfo(productInfo);
				session.setAttribute(Constant.MSG_SUCCESS, "Delete success!!!"); 
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute(Constant.MSG_ERROR, "Delete has error!!!"); 
			}
		}
		return "redirect:/product-info/list";
	}
	
}








