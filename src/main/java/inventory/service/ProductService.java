package inventory.service;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import inventory.dao.CategoryDAO;
import inventory.model.Category;

@Service
public class ProductService {
	
	private static Logger log = Logger.getLogger(ProductService.class);
	
	@Autowired
	private CategoryDAO<Category> categoryDAO;
	
	public void saveCategory(Category category) {
		log.info("insert category "+category.toString());
		category.setActiveFlag(1);
		category.setCreateDate(new Date());
		category.setUpdateDate(new Date());
		categoryDAO.save(category);
	}
	
	public void updateCategory(Category category) {
		log.info("update category "+category.toString());
		category.setUpdateDate(new Date());
		categoryDAO.update(category);
	}
	
	public void delete(Category category) {
		log.info("delete category "+category.toString());
		// ta sẽ k xóa record mà chỉ cập nhật trạng thái cho nó(0 là không tồn tại)
		category.setActiveFlag(0);
		category.setUpdateDate(new Date());
		categoryDAO.update(category);
	}
	
	public List<Category> findCategoryByProperty(String property, Object value) {
		log.info("===Find category by property start===");
		log.info("property= " + property + "value= "+value.toString());
		List<Category> categories = categoryDAO.findByProperty(property, value);
		return categories;
	}
	
}






