package inventory.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import inventory.dao.InvoiceDAO;
import inventory.model.Invoice;
import inventory.model.Paging;
import inventory.model.ProductInfo;
import inventory.util.Constant;

@Service
public class InvoiceService {
	
	@Autowired
	private HistoryService historyService;
	
	@Autowired
	private ProductInStockService productInStockService;
	
	@Autowired
	private InvoiceDAO<Invoice> invoiceDAO;
	
	public List<Invoice> getListInvoice(Invoice invoice, Paging paging) {
		StringBuilder queryStr = new StringBuilder();
		Map<String, Object> mapParams = new HashMap<>();
		if(invoice != null) {
			// vì invoiceService này ta dùng chung cho cả nhập và xuất hóa đơn(chỉ khác nhau ở type)
			if(invoice.getType() != 0) {
				queryStr.append(" and model.type=:type");
				mapParams.put("type", invoice.getType());
			}
			if(!StringUtils.isEmpty(invoice.getCode())) {
				queryStr.append(" and model.code=:code");
				mapParams.put("code", invoice.getCode());
			}
			// search theo updateDate(thời gian của hóa đơn cuối cùng)
			if(invoice.getFromDate() != null) {
				queryStr.append(" and model.updateDate >= :fromDate");
				mapParams.put("fromDate", invoice.getFromDate());
			}
			if(invoice.getToDate() != null) {
				queryStr.append(" and model.updateDate <= :toDate");
				mapParams.put("toDate", invoice.getToDate());
			}
		}
		return invoiceDAO.findAll(queryStr.toString(), mapParams, paging);
	}
	
	public void save(Invoice invoice) throws Exception {
		// tạo 1 đtượng ProductInfo và gán id của productInfo trong invoice cho nó 
		ProductInfo productInfo = new ProductInfo();
		productInfo.setId(invoice.getProductId());
		invoice.setProductInfo(productInfo);
		invoice.setActiveFlag(1);
		invoice.setCreateDate(new Date());
		invoice.setUpdateDate(new Date());
		invoiceDAO.save(invoice);
		// save vào historyService để lưu lại lịch sử và action tương ứng
		historyService.save(invoice, Constant.ACTION_ADD);
		// khi ta thêm hàng hóa thì productInStock sẽ cập nhật hàng hóa trong kho(số lượng, giá)
		productInStockService.saveOrUpdate(invoice);
	}
	
	public void update(Invoice invoice) throws Exception {
		// lấy ra số lượng hàng hóa ban đầu(gốc) của hóa đơn đang nhập hàng
		int originQuantity = invoiceDAO.findById(Invoice.class, invoice.getId()).getQty();
		ProductInfo productInfo = new ProductInfo();
		productInfo.setId(invoice.getProductId());
		invoice.setProductInfo(productInfo);
		invoice.setUpdateDate(new Date());
		
		Invoice newInvoice = new Invoice();
		// số lượng cập nhât = sl nhập ở form - sl ban đầu, VD: trên hóa đơn đấy số lượng đang là 10 mà ta sửa lại là = 5 ==> 5-10=-5 ==> giảm 5 
		newInvoice.setQty(invoice.getQty() - originQuantity);
		// ta cập nhật số lượng sp, còn thông tin và giá của sp ta giữ nguyên
		newInvoice.setPrice(invoice.getPrice());
		newInvoice.setProductInfo(invoice.getProductInfo());
		invoiceDAO.update(invoice);
		historyService.save(invoice, Constant.ACTION_EDIT);
		// cập nhật các hàng hóa có trong kho(voi hóa đơn mới)
		productInStockService.saveOrUpdate(newInvoice);
	}
	
	public List<Invoice> findInvoioceByProperty(String property, Object value) {
		return invoiceDAO.findByProperty(property, value);
	}
	
}



















