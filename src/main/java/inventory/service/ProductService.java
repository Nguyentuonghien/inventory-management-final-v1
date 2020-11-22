package inventory.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import inventory.dao.CategoryDAO;
import inventory.model.Category;
import inventory.model.Paging;

@Service
public class ProductService {
	
	private static Logger log = Logger.getLogger(ProductService.class);
	
	@Autowired
	private CategoryDAO<Category> categoryDAO;
	
	public void saveCategory(Category category) throws Exception{
		log.info("Insert category "+category.toString());
		category.setActiveFlag(1);
		category.setCreateDate(new Date());
		category.setUpdateDate(new Date());
		categoryDAO.save(category);
	}
	
	public void updateCategory(Category category) throws Exception{
		log.info("Update category "+category.toString());
		category.setUpdateDate(new Date());
		categoryDAO.update(category);
	}
	
	public void deleteCategory(Category category) throws Exception{
		log.info("Delete category "+category.toString());
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
		
	// search và phân trang
	public List<Category> getAllCategory(Category category, Paging page) {
		log.info("Show all category");
		
		StringBuilder queryStr = new StringBuilder();		
		// mapParams sẽ lưu các param mà ta truyền vào(giả sử ta truyền vào param: id=1,name=Apple,code=ProXsmax) và phía DAO sẽ set giá trị cho các params này
		Map<String, Object> mapParams = new HashMap<>();
		if(category != null) {
			if(category.getId() != null && category.getId() != 0) {
				// nối với câu lệnh của hàm findAll() bên BaseDAOImpl: from category as model where model.activeFlag=1 and model.id=:id
				queryStr.append(" and model.id=:id");     
				mapParams.put("id", category.getId());
			}
			if(category.getCode() != null && !StringUtils.isEmpty(category.getCode())) {
				// nối với câu lệnh của hàm findAll() bên BaseDAOImpl: from category as model where model.activeFlag=1 and model.id=:id
				queryStr.append(" and model.code=:code");     
				mapParams.put("code", category.getCode());
			}
			if(category.getName() != null && !StringUtils.isEmpty(category.getName())) {
				// nối với câu lệnh của hàm findAll() bên BaseDAOImpl: from category as model where model.activeFlag=1 and model.id=:id
				queryStr.append(" and model.name like :name");     
				mapParams.put("name", "%"+category.getName()+"%");
			}
		}
		
		return categoryDAO.findAll(queryStr.toString(), mapParams, page);
	}
	
	public Category findById(int id) {
		log.info("Find category by id: "+id);
		return categoryDAO.findById(Category.class, id);
	}
	
}






