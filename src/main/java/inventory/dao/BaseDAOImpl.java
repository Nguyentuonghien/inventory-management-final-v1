package inventory.dao;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
	public List<E> findAll() {
		log.info("Find all record from database:");
		
		StringBuilder queryString = new StringBuilder("");
		// câu query: from category as model where model.activeFlag=1
		// lấy ra tất cả các bản ghi từ category có activeFlag = 1
		queryString.append(" from ").append(getGenericName()).append(" as model where model.activeFlag=1");
		
		log.info("Query find all ====> "+queryString.toString());
		return sessionFactory.getCurrentSession().createQuery(queryString.toString()).getResultList();
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
		// Câu query tương ứng với giả sử property là userName ==> from user as model where model.activeFlag=1 and model.userName=?
		// "?" la 1 parameter
		queryString.append(" from ").append(getGenericName()).append(" as model where model.activeFlag=1 and model.")
		                            .append(property).append("=?");
		
		log.info("Query find by property ====> "+queryString.toString());
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
