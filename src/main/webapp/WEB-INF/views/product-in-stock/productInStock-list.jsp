<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<script src="//cdnjs.cloudflare.com/ajax/libs/numeral.js/2.0.6/numeral.min.js"></script>

<style>
.price {
   font-size: 14px;
}
</style>

<div class="right_col" role="main">
	<div class="container">

		<div class="clearfix"></div>
		<div class="col-md-12 col-sm-12  ">
			<div class="x_panel">
				<div class="x_title">
					<h2>Product In Stock List</h2>
					<div class="clearfix"></div>
				</div>

				<div class="x_content">
					 
					 <div class="container" style="padding: 30px;">
					     <form:form modelAttribute="searchForm" cssClass="form-horizontal form-label-left" servletRelativeAction="/product-in-stock/list/1" method="POST">
							<div class="form-group">
								<label class="control-label col-md-3 col-sm-3 col-xs-12" for="code">Code</label>
								<div class="col-md-6 col-sm-6 col-xs-12">
								    <!-- trong productInStock chứa thông tin của productInfo, path="productInfo.code": code của productInfo
								         spring sẽ tự động binding vào object productInfo và field là code -->
									<form:input path="productInfo.code" cssClass="form-control col-md-7 col-xs-12" />
								</div>
							</div>
							<div class="form-group">
								<label class="control-label col-md-3 col-sm-3 col-xs-12" for="code">Category</label>
								<div class="col-md-6 col-sm-6 col-xs-12">
								    <!-- path="productInfo.category.name": tên của cateory trong productInfo , spring sẽ tự động binding vào object productInfo -->
								    <!-- và object category(productInfo chứa category) và field là name (tên của category)-->
									<form:input path="productInfo.category.name" cssClass="form-control col-md-7 col-xs-12" />
								</div>
							</div>
							<div class="form-group">
								<label class="control-label col-md-3 col-sm-3 col-xs-12" for="name">Name</label>
								<div class="col-md-6 col-sm-6 col-xs-12">
								    <!-- path="productInfo.name": tên của productInfo, spring sẽ tự động binding vào object productInfo và field là name -->
									<form:input path="productInfo.name" cssClass="form-control col-md-7 col-xs-12" />
								</div>
							</div>
							<div class="form-group">
								<div class="col-md-6 col-sm-6 col-xs-12 col-md-offset-3">
									<button type="submit" class="btn btn-success">Search</button>
								</div>
							</div>
					    </form:form>
					 </div>
					 
					<div class="table-responsive">
						<table class="table table-striped jambo_table bulk_action">
							<thead>
								<tr class="headings">
									<th class="column-title">#</th>
									<th class="column-title">Category</th>
									<th class="column-title">Code</th>
									<th class="column-title">Name</th>							
									<th class="column-title">Image</th>
									<th class="column-title">Quantity</th>
									<th class="column-title">Price</th>
								</tr>
							</thead>

							<tbody>
								<c:forEach items="${productInStocks}" var="productInStock" varStatus="loop">
								<c:choose>
								    <c:when test="${loop.index%2==0}">
								        <tr class="even pointer">
								    </c:when>
								    <c:otherwise>
								        <tr class="odd pointer">
								    </c:otherwise>
								</c:choose>
										<td class=" ">${pageInfo.getOffset()+loop.index+1}</td>
										<td class=" ">${productInStock.productInfo.category.name}</td>
										<td class=" ">${productInStock.productInfo.code}</td>
										<td class=" ">${productInStock.productInfo.name}</td>
										<td class=" "><img src='<c:url value="${productInStock.productInfo.imgUrl}"/>' width="100px" height="100px" /></td>
										<td class=" ">${productInStock.qty}</td>
										<td class="price">${productInStock.price}</td>
								</c:forEach>
							</tbody>
						</table>
						
						<jsp:include page="../layout/paging.jsp"></jsp:include>
						
					</div>
					
				</div>
			</div>
		</div>

	</div>
</div>

<script type="text/javascript">
	 function gotoPage(page){
		 $('#searchForm').attr('action','<c:url value="/product-in-stock/list/"/>'+page);
		 $('#searchForm').submit();
	 }
	 $(document).ready(function(){
		 processMessage();
		 $('.price').each(function(){
			 $(this).text(numeral($(this).text()).format('0,0'));
		 }) 
	 });
	 function processMessage(){
		 var msgSuccess = '${msgSuccess}';
		 var msgError = '${msgError}';
		 if(msgSuccess){
			 new PNotify({
                 title: ' Success',
                 text: msgSuccess,
                 type: 'success',
                 styling: 'bootstrap3'
             });
		 }
		 if(msgError){
			 new PNotify({
                 title: ' Error',
                 text: msgError,
                 type: 'error',
                 styling: 'bootstrap3'
             });
		 }
	 }
	
	
</script>

