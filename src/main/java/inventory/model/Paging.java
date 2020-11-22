package inventory.model;

public class Paging {

	private long totalRows;          // tổng số bản ghi
	private int totalPages;          // tổng số trang
	private int indexPage;           // trang hiện tại
	private int recordPerPage = 10;  // mặc định số bản ghi trong 1 page là 10
	private int offset;              // phần tủ đầu tiên ứng với mỗi page(0,10,20,...)

	public Paging(int recordPerPage) {
		this.recordPerPage = recordPerPage;
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

	public int getTotalPages() {
		// khi ta có nhiều hơn 1 record, giả sử ta có 20 record, mà mỗi 1 trang ta chỉ hiển thị 10 bản ghi --> ta có 20/10 = 2 trang
	    // tương tự: nếu totalRows = 8, recordPerPage = 3 --> totalPages= 8/3 = 2,667 trang = 3 trang(ta se làm tròn số)
		if(totalRows > 0) {
			totalPages = (int) Math.ceil(totalRows/(double)recordPerPage);
		}
		return totalPages;
	}

    public int getOffset() {
		if(indexPage > 0) {
			offset = indexPage * recordPerPage - recordPerPage;
		}
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getIndexPage() {
		return indexPage;
	}

	public void setIndexPage(int indexPage) {
		this.indexPage = indexPage;
	}

	public int getRecordPerPage() {
		return recordPerPage;
	}

	public void setRecordPerPage(int recordPerPage) {
		this.recordPerPage = recordPerPage;
	}

}
