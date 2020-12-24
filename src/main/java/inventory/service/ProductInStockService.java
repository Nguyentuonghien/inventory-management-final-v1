package inventory.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import inventory.dao.ProductInStockDAO;
import inventory.model.Invoice;
import inventory.model.Paging;
import inventory.model.ProductInStock;
import inventory.model.ProductInfo;

@Service
public class ProductInStockService {
	
	@Autowired
	private ProductInStockDAO<ProductInStock> productInStockDAO;
	
	private static Logger log = Logger.getLogger(ProductInStockService.class);
	
	public List<ProductInStock> getAllProductInStock(ProductInStock productInStock, Paging page) {
    log.info("Show all Product In Stock");
		
		StringBuilder queryStr = new StringBuilder();		
		// mapParams sẽ lưu các param mà ta truyền vào(giả sử ta truyền vào param: id=1,name=Apple,code=ProXsmax) và phía DAO sẽ set giá trị cho các params này
		Map<String, Object> mapParams = new HashMap<>();
		
		// Form search không rỗng và các trường liên quan đến productInfo mà ta search không rỗng(code, category, name) bên product-in-stock.jsp
		if(productInStock != null && productInStock.getProductInfo() != null) {
			// search theo "tên category" của productInfo
			if (!StringUtils.isEmpty(productInStock.getProductInfo().getCategory().getName())) {
				queryStr.append(" and model.productInfo.category.name like: cateName");
				mapParams.put("cateName", "%"+productInStock.getProductInfo().getCategory().getName()+"%");
			}
			// search theo code của productInfo
			if(!StringUtils.isEmpty(productInStock. getProductInfo().getCode())) {
				// nối với câu lệnh của hàm findAll() bên BaseDAOImpl: from ... as model where model.activeFlag=1 and model.productInfo.code=:code
				queryStr.append(" and model.productInfo.code=:code");     
				mapParams.put("code", productInStock.getProductInfo().getCode());
			}
			// search theo tên của productInfo
			if(!StringUtils.isEmpty(productInStock.getProductInfo().getName())) {
				queryStr.append(" and model.productInfo.name like :name");     
				mapParams.put("name", "%"+productInStock.getProductInfo().getName()+"%");
			}
		}
		
		return productInStockDAO.findAll(queryStr.toString(), mapParams, page);
	}
	
	// trong Invoice có ProductInfo, khi ta nhập hàng hóa thì đồng thời ta cũng sẽ save hóa đơn(Invoice) vào trong productInStock 
	// để nó cập nhật số lượng hàng hóa còn trong kho, giá hiện tại khi ta nhập vào kho. Nếu mà sản phẩm chưa có trong kho --> nó sẽ thêm mới
	public void saveOrUpdate(Invoice invoice) throws Exception{
		log.info("product in stock:");
		
		// Nếu các trường liên quan đến ProductInfo có trong hóa đơn không rỗng --> ta sẽ tìm sản phẩm trong kho(ProductInStock) theo id của nó trong hóa đơn
		if(invoice.getProductInfo() != null) {
			int id = invoice.getProductInfo().getId();
			List<ProductInStock> productInStocks = productInStockDAO.findByProperty("productInfo.id", id);
			ProductInStock productInStock = null;
			// nếu tìm thấy ProductInfo trong kho, ta chỉ cần update lại 2 trường là số lượng và giá cho ProductInfo có trong kho 
			// còn không tìm thấy ProductInfo, ta sẽ insert nó
			if(productInStocks != null && !productInStocks.isEmpty()) {
				productInStock = productInStocks.get(0);
				log.info("update quantity = "+invoice.getQty() + "and price = "+invoice.getPrice());
				if(invoice.getType() == 2) {
					// xuất hàng: số lượng sp được update = số lượng sp hiện tại có trong kho - số lượng sp trong hóa đơn
					productInStock.setQty(productInStock.getQty() - invoice.getQty());
				}else {
					// nhập hàng: số lượng sp được update = số lượng sp hiện tại có trong kho + số lượng sp trong hóa đơn
					productInStock.setQty(productInStock.getQty() + invoice.getQty());
				}
				// update giá: ta chỉ update giá khi nhập hàng(type=1) còn xuất hàng không cần(type=2)
				if(invoice.getType() == 1) {
					productInStock.setPrice(invoice.getPrice());
				}
				productInStock.setUpdateDate(new Date());
				productInStockDAO.update(productInStock);
			}else if(invoice.getType() == 1){
				log.info("insert to stock quantity=" + invoice.getQty() + "and price=" + invoice.getPrice());
				productInStock = new ProductInStock();
				ProductInfo productInfo = new ProductInfo();
				// thêm mới sp từ hóa đơn vào kho, id là id của sp trong hóa đơn
				productInfo.setId(invoice.getProductInfo().getId());
				productInStock.setProductInfo(productInfo);
				productInStock.setPrice(invoice.getPrice());
				productInStock.setQty(invoice.getQty());
				productInStock.setActiveFlag(1);
				productInStock.setCreateDate(new Date());
				productInStock.setUpdateDate(new Date());
				productInStockDAO.save(productInStock);
			}
		}
	}
	
}










