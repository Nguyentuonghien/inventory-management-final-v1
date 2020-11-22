package inventory.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import inventory.model.Paging;

@Repository
@Transactional(rollbackFor = Exception.class)
public class BaseDAOImpl<E> implements BaseDAO<E>{

	private static Logger log = Logger.getLogger(BaseDAOImpl.class);
	
	/**
	 * tự động liên kết với bean "sessionFactory" ở file : spring-mvc-servlet.xml
	 */
	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public List<E> findAll(String queryStr, Map<String, Object> mapParams, Paging page) {
		log.info("Find all record from database:");
		
		StringBuilder queryString = new StringBuilder("");
		// câu query: from category as model where model.activeFlag=1 (lấy ra tất cả các bản ghi từ bảng category có activeFlag = 1)
		queryString.append(" from ").append(getGenericName()).append(" as model where model.activeFlag=1");
		
		StringBuilder countQueryString = new StringBuilder();
		// đếm xem có bao nhiêu bản ghi:
		countQueryString.append(" select count(*) from ").append(getGenericName()).append(" as model where model.activeFlag=1");
		
		// nếu queryStr khác null và không rỗng:
		// ta search dữ liệu bằng cách nối với câu query bên phía service: from category as model where model.activeFlag=1 and model.id=:id
		// đồng thời ta phân trang luôn:                                   select count(*) from category as model where model.activeFlag=1 and model.id=:id
		if(queryStr != null && !queryStr.isEmpty()) {
			queryString.append(queryStr);
			countQueryString.append(queryStr);
		}
		
		Query<E> query = sessionFactory.getCurrentSession().createQuery(queryString.toString());
		Query<E> countQuery = sessionFactory.getCurrentSession().createQuery(countQueryString.toString());
	
		// khi params khác null và k rỗng --> ta sẽ set các params được truyền từ service vào trong câu query
		if(mapParams != null && !mapParams.isEmpty()) {
			for(String key : mapParams.keySet()) {
				// set param cho 1 Map(là mapParams) vào câu query, ta sử dụng dạng setParameter(key, value)  
				query.setParameter(key, mapParams.get(key));
				countQuery.setParameter(key, mapParams.get(key));
			}
		}
		// list đang phân trang, và offset bắt đầu từ 0 --> kq đầu tiên sẽ bắt đầu từ 0
		if(page != null) {   		
			// 2 lệnh bên dưới tương đương câu query: from category as model where model.activeFlag=1 limit(0,10)
			// chú ý: tùy vào giá trị của offset có thể limit(0,10) or limit(10,10) or limit(20,10)
 			query.setFirstResult(page.getOffset());            
			query.setMaxResults(page.getRecordPerPage());     
			
			long totalRecord = (long) countQuery.uniqueResult();
			page.setTotalRows(totalRecord);
		}
		
		log.info("Query find all ====> "+queryString.toString());
		return query.list();
	}

	@Override
	public E findById(Class<E> e, Serializable id) {		
		log.info("Find by ID: ");
		
		return sessionFactory.getCurrentSession().get(e, id);
	}

	@Override
	public List<E> findByProperty(String property, Object value) {
		log.info("Find by property: ");
		
		StringBuilder queryString = new StringBuilder();
		queryString.append(" from ").append(getGenericName()).append(" as model where model.activeFlag=1 and model.")
		                            .append(property).append("=?");
		
		log.info("Query find by property ====> "+queryString.toString());
		// Câu query tương ứng với giả sử property là userName ==> from user as model where model.activeFlag=1 and model.userName=?
		// "?" la 1 parameter
		Query<E> query = sessionFactory.getCurrentSession().createQuery(queryString.toString());
		// set parameter(thay cho dau ?)
		query.setParameter(0, value);
		
		return query.getResultList();
	}

	@Override
	public void save(E instance) {
		log.info("save instance");
		sessionFactory.getCurrentSession().persist(instance);
	}

	@Override
	public void update(E instance) {
		log.info("update instance");
		sessionFactory.getCurrentSession().update(instance);
	}

	// lấy ra tên generic của class mà ta truyền vào
	// vì khi ta dùng hibernate để query nếu truyền chữ E thì hibernate sẽ k thể thực thi được
	// mà ta phải truyền vào tên class đấy như : Category, User, Product,...
	public String getGenericName() {
		String s = getClass().getGenericSuperclass().toString();
		Pattern pattern = Pattern.compile("\\<(.*?)\\>");
		Matcher m = pattern.matcher(s);
		String generic="null";
		if(m.find()) {
			generic = m.group(1);
		}
		return generic;
	}
	
}
