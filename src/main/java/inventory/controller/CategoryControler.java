package inventory.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

import inventory.model.Category;
import inventory.service.ProductService;
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
		// trong class CategoryValidator ta chỉ support Class validate là  Category(các class khác thì không) 
		// và binder sẽ kiểm tra nếu đúng là class Category --> sẽ setValidator cho categoryValidator
		if(binder.getTarget().getClass() == Category.class) {
			binder.setValidator(categoryValidator);
		}
	}
	
	@GetMapping("/category/list")
	public String showCategoryList(Model model) {
		List<Category> categories = productService.getAllCategory();
		model.addAttribute("categories", categories);
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
	public String saveCategory(@ModelAttribute("modelForm") @Validated Category category, Model model, BindingResult result) {
		// validate lỗi (khi ta nhập code,name,description trên form mà có lỗi)
		if(result.hasErrors()) {
			return "category-action";
		}
		// vì màn hình add và edit dùng chung 1 form ==> để phân biệt add và edit: nếu là add thì không có id còn
		// nếu là edit ==> có id đi kèm(vì nó record đó đã tồn tại trong DB) và id đó khác 0 và khác null 
		// (Vd: id trong DB có giá trị = 1 nhưng nếu ta nhập vào id=10 ==> trả về null vì k tồn tại id=10 trong DB)
		if(category.getId() != 0 && category.getId() != null) {
			productService.updateCategory(category);
			model.addAttribute("message", "Update success!!!");
		} else {
			productService.saveCategory(category);
			model.addAttribute("message", "Insert success!!!");
		}
		return "category-list";
	}
	
	@GetMapping("/category/delete/{id}")
	public String deleteCategory(Model model, @PathVariable("id") int id) {
		log.info("Delete category by id: "+id);
		Category category = productService.findById(id);
		if(category != null) {
			productService.deleteCategory(category);
		}
		return "redirect:/category/list";
	}
	
}








