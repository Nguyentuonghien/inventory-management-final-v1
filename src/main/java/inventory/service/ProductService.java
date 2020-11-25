package inventory.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import inventory.dao.CategoryDAO;
import inventory.dao.ProductInfoDAO;
import inventory.model.Category;
import inventory.model.Paging;
import inventory.model.ProductInfo;
import inventory.util.ConfigLoader;

@Service
public class ProductService {
	
	private static Logger log = Logger.getLogger(ProductService.class);
	
	@Autowired
	private CategoryDAO<Category> categoryDAO;
	
	@Autowired
	private ProductInfoDAO<ProductInfo> productInfoDAO;
	
	// CATEGORY SERVICE
	
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
	
	// PRODUCT-INFO SERVICE
	
	public void saveProductInfo(ProductInfo productInfo) throws Exception{
		log.info("Insert ProductInfo "+productInfo.toString());
		productInfo.setActiveFlag(1);
		productInfo.setCreateDate(new Date());
		productInfo.setUpdateDate(new Date());
		processUploadFile(productInfo.getMultipartFile());
		// khi ta save ảnh vào DB, ta chỉ lưu đường dẫn tương đối của nó và ta sẽ tạo ra 1 đường dẫn để ảnh lưu vào trong DB
		productInfo.setImgUrl("/upload/"+System.currentTimeMillis()+"_"+productInfo.getMultipartFile().getOriginalFilename());
		productInfoDAO.save(productInfo);
	}
	
	public void updateProductInfo(ProductInfo productInfo) throws Exception{
		log.info("Update ProductInfo "+productInfo.toString());
		processUploadFile(productInfo.getMultipartFile());
		// edit: ta chọn 1 file ảnh khác với file ảnh hiện tại
		// nếu khác rỗng --> ta đã chọn(thay đổi) 1 ảnh khác rồi --> ta sẽ sửa lại đường dẫn lưu trong DB cho nó
		if(productInfo.getMultipartFile() != null) {    // 
			productInfo.setImgUrl("/upload/"+System.currentTimeMillis()+"_"+productInfo.getMultipartFile().getOriginalFilename());
		}
		productInfo.setUpdateDate(new Date());
		productInfoDAO.update(productInfo);
	}
	
	public void deleteProductInfo(ProductInfo productInfo) throws Exception{
		log.info("Delete ProductInfo "+productInfo.toString());
		// ta sẽ k xóa record mà chỉ cập nhật trạng thái cho nó(0 là không tồn tại)
		productInfo.setActiveFlag(0);
		productInfo.setUpdateDate(new Date());
		productInfoDAO.update(productInfo);
	}
	
	public List<ProductInfo> findProductInfoByProperty(String property, Object value) {
		log.info("===Find ProductInfo by property start===");
		log.info("property= " + property + "value= "+value.toString());
		List<ProductInfo> productInfos = productInfoDAO.findByProperty(property, value);
		return productInfos;
	}
		
	// search và phân trang
	public List<ProductInfo> getAllProductInfo(ProductInfo productInfo, Paging page) {
		log.info("Show all ProductInfo");
		
		StringBuilder queryStr = new StringBuilder();		
		// mapParams sẽ lưu các param mà ta truyền vào(giả sử ta truyền vào param: id=1,name=Apple,code=ProXsmax) và phía DAO sẽ set giá trị cho các params này
		Map<String, Object> mapParams = new HashMap<>();
		if(productInfo != null) {
			if(productInfo.getId() != null && productInfo.getId() != 0) {
				// nối với câu lệnh của hàm findAll() bên BaseDAOImpl: from product_info as model where model.activeFlag=1 and model.id=:id
				queryStr.append(" and model.id=:id");     
				mapParams.put("id", productInfo.getId());
			}
			if(productInfo.getCode() != null && !StringUtils.isEmpty(productInfo.getCode())) {
				// nối với câu lệnh của hàm findAll() bên BaseDAOImpl: from product_info as model where model.activeFlag=1 and model.id=:id
				queryStr.append(" and model.code=:code");     
				mapParams.put("code", productInfo.getCode());
			}
			if(productInfo.getName() != null && !StringUtils.isEmpty(productInfo.getName())) {
				// nối với câu lệnh của hàm findAll() bên BaseDAOImpl: from product_info as model where model.activeFlag=1 and model.id=:id
				queryStr.append(" and model.name like :name");     
				mapParams.put("name", "%"+productInfo.getName()+"%");
			}
		}
		
		return productInfoDAO.findAll(queryStr.toString(), mapParams, page);
	}
	
	public ProductInfo findProductInfoById(int id) {
		log.info("Find ProductInfo by id: "+id);
		return productInfoDAO.findById(ProductInfo.class, id);
	}
	
	private void processUploadFile(MultipartFile multipartFile) throws IllegalStateException, IOException {
		// đã có file được upload lên
		if(multipartFile != null) {
			// "upload.location": đường dẫn đến file ta tạo ra bên phía server (/F:/fileupload)
			File dir = new File(ConfigLoader.getInstance().getValue("upload.location"));
			// dir chưa được tạo --> sẽ tạo dir
			if(!dir.exists()) {
				dir.mkdirs();
			}
			// tránh trường hợp upload file mà tên ảnh trùng nhau(vd tên ảnh: kayn.jpg)
			// tạo ra 1 file rỗng với tên file= fileName
			String fileName = System.currentTimeMillis()+"_"+multipartFile.getOriginalFilename();
			File file = new File(ConfigLoader.getInstance().getValue("upload.location"), fileName);
			multipartFile.transferTo(file);
		}
	}
	
}
















