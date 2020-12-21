package inventory.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import inventory.model.Invoice;
import inventory.util.Constant;
import inventory.util.DateUtil;

public class GoodsReceiptReport extends AbstractXlsxView{

	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//response.setHeader("Content-Disposition", "attachment;filename=\"goods-receipt-export.xlsx\"");
		String fileName = "invoice-export-"+System.currentTimeMillis()+".xlsx";
		response.setHeader("Content-Disposition", "attachment;filename=\""+fileName+"\"");
		
		Sheet sheet = workbook.createSheet("dataExcel");
		Row rowHeader = sheet.createRow(0);
		rowHeader.createCell(0).setCellValue("#");
		rowHeader.createCell(1).setCellValue("Code");
		rowHeader.createCell(2).setCellValue("Quantity");
		rowHeader.createCell(3).setCellValue("Price");
		rowHeader.createCell(4).setCellValue("Product");
		rowHeader.createCell(5).setCellValue("Update Date");
		
		List<Invoice> invoices =(List<Invoice>) model.get(Constant.KEY_GOODS_RECEIPT_REPORT);
		int rowNum=1;
		for(Invoice invoice :invoices) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(rowNum-1);
			row.createCell(1).setCellValue(invoice.getCode());
			row.createCell(2).setCellValue(invoice.getQty());
			row.createCell(3).setCellValue(invoice.getPrice().toString());
			row.createCell(4).setCellValue(invoice.getProductInfo().getName());
			row.createCell(5).setCellValue(DateUtil.dateToString(invoice.getUpdateDate()));			
		}
	}
}



