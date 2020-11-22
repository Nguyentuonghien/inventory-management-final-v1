package inventory.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import inventory.service.ProductService;
import inventory.util.Constant;
import inventory.validate.CategoryValidator;

/** @InitBinder: nếu ta muốn tùy chỉnh request được gửi đến controller, nó được định nghĩa trong controller, giúp kiểm soát và định dạng mọi request đến với nó. 
 *               Nó còn được sử dụng với các phương thức khởi tạo WebDataBinder và hoạt động như một bộ xử lý trước(preprocessor) cho mỗi request đến controller.
 */

@Controller
public class CategoryControler {
	
	private static final Logger log = Logger.getLogger(CategoryControler.class);
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private CategoryValidator categoryValidator;
	
	@InitBinder
	private void initBinder(WebDataBinder binder) {
		if(binder.getTarget() == null) return;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		
		// trong class CategoryValidator ta chỉ support Class validate là  Category(các class khác thì không) 
		// và binder sẽ kiểm tra nếu đúng là class Category --> sẽ setValidator cho categoryValidator
		if(binder.getTarget().getClass() == Category.class) {
			binder.setValidator(categoryValidator);
		}
	}
	
	// với những url này thì mặc định ta sẽ chuyên hướng về trang đầu tiên(page=1)
    @RequestMapping(value= {"/category/list","/category/list/"})	
	public String redirect() {
		return "redirect:/category/list/1";
	}
	
	// @ModelAttribute: khi form search submit lên --> mọi thông tin từ "searchForm" này sẽ được gán vào Category
	@RequestMapping(value = "/category/list/{page}")      // page: trang hiện tại, từ page này ta sẽ tính ra  offset tương ứng
	public String showCategoryList(Model model, HttpSession session, @ModelAttribute("searchForm") Category category, @PathVariable(value = "page") int page) {
		Paging paging = new Paging(1);
		paging.setIndexPage(page);
		List<Category> categories = productService.getAllCategory(category, paging);
		
		// nếu có thông báo(success hoặc error) được lưu trong session thì ta sẽ lấy nó ra và gửi nó qua "category-list" 
		// và đồng thời xóa nó luôn trong session
		if(session.getAttribute(Constant.MSG_SUCCESS) != null) {
			model.addAttribute(Constant.MSG_SUCCESS, session.getAttribute(Constant.MSG_SUCCESS));
			session.removeAttribute(Constant.MSG_SUCCESS);
		}
		if(session.getAttribute(Constant.MSG_ERROR) != null) {
			model.addAttribute(Constant.MSG_ERROR, session.getAttribute(Constant.MSG_ERROR));
			session.removeAttribute(Constant.MSG_ERROR);
		}
		model.addAttribute("categories", categories);
		model.addAttribute("pageInfo", paging);
		return "category-list";
	}
	
	@GetMapping("/category/add")
	public String addCategory(Model model) {
		// trả về 1 đối tượng Category rỗng cho người dùng điền vào các thông tin trên form(code, name và description)
		model.addAttribute("modelForm", new Category());
		// gán title động tương ứng với 3 màn hình add, edit và view(tái sử dụng form vì màn hình add, edit và view giống nhau, chỉ khác nhau title)
		model.addAttribute("titlePage", "Add Category");
		// màn hình add thì được sửa các thông tin trên form(viewOnly==false)
		model.addAttribute("viewOnly", false);            
		return "category-action";
	}
	
	@GetMapping("/category/edit/{id}")
	public String editCategory(Model model, @PathVariable("id") int id) {
		log.info("Edit category with id: "+id);
		Category category = productService.findById(id);       // tìm đối tượng category trong DB theo id
		if(category != null) {
			model.addAttribute("titlePage", "Edit Category");
			model.addAttribute("modelForm", category);         // lúc này ta đã có đối tượng category vừa tìm thấy trong DB rồi
			// màn hình edit thì được sửa các thông tin trên form(viewOnly==false)
			model.addAttribute("viewOnly", false);
			return "category-action";
		}	
		return "redirect:/category/list";
	}
	
	@GetMapping("/category/view/{id}")
	public String viewCategory(Model model, @PathVariable("id") int id) {
		log.info("View category by id: "+id);
		Category category = productService.findById(id);       // tìm đối tượng category trong DB theo id
		if(category != null) {
			model.addAttribute("titlePage", "View Category");
			model.addAttribute("modelForm", category);         // lúc này ta đã có đối tượng category vừa tìm thấy trong DB rồi
			// màn hình view thì không được sửa các thông tin trên form(viewOnly==true)
			model.addAttribute("viewOnly", true);
			return "category-action";
		}	
		return "redirect:/category/list";
	}
	
	@PostMapping("/category/save")
	public String saveCategory(Model model, @ModelAttribute("modelForm") @Validated Category category, BindingResult result, HttpSession session) {
		// 2 màn hình add và edit khi validate lỗi (khi ta nhập code,name,description trên form mà có lỗi)
		if(result.hasErrors()) {
			if(category.getId() != null) {
				model.addAttribute("titlePage", "Edit Category");
			}else {
				model.addAttribute("titlePage", "Add Category");
			}
			model.addAttribute("modelForm", category);
			model.addAttribute("viewOnly", false);
			return "category-action";
		}
		
		// vì màn hình add và edit dùng chung 1 form ==> để phân biệt add và edit: nếu là add thì không có id còn
		// nếu là edit ==> có id đi kèm(vì record đó đã tồn tại trong DB) và id đó khác 0 và khác null 
		// (Vd: id trong DB có giá trị = 1 nhưng nếu ta nhập vào id=10 ==> trả về null vì k tồn tại id=10 trong DB)
		if(category.getId() != null && category.getId() != 0) {
			try {
				productService.updateCategory(category);
				session.setAttribute(Constant.MSG_SUCCESS, "Update success!!!");     // lưu thông báo thành công trong session(không phải model)
			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
				session.setAttribute(Constant.MSG_ERROR, "Update has error!!!");     // lưu thông báo lỗi trong session
			}
		}else {
			try {
				productService.saveCategory(category);
				session.setAttribute(Constant.MSG_SUCCESS, "Insert success!!!"); 
			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
				session.setAttribute(Constant.MSG_ERROR, "Insert has error!!!"); 
			}
		}
		return "redirect:/category/list";
	}
	
	@GetMapping("/category/delete/{id}")
	public String deleteCategory(Model model, @PathVariable("id") int id, HttpSession session) {
		log.info("Delete category by id: "+id);
		Category category = productService.findById(id);
		if(category != null) {
			try {
				productService.deleteCategory(category);
				session.setAttribute(Constant.MSG_SUCCESS, "Delete success!!!"); 
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute(Constant.MSG_ERROR, "Delete has error!!!"); 
			}
		}
		return "redirect:/category/list";
	}
	
}








