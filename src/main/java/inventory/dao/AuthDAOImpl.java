package inventory.dao;

import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import inventory.model.Auth;

@Repository
@Transactional(rollbackFor = Exception.class)
public class AuthDAOImpl extends BaseDAOImpl<Auth> implements AuthDAO<Auth>{
	
	@Override
	public Auth findAuth(int roleId, int menuId) {
		StringBuilder queryHql = new StringBuilder();
		queryHql.append("from Auth as model where model.role.id=:roleId and model.menu.id=:menuId");
		Query<Auth> query = sessionFactory.getCurrentSession().createQuery(queryHql.toString());
		query.setParameter("roleId", roleId);
		query.setParameter("menuId", menuId);
		List<Auth> auths = query.getResultList();
		// khi tìm theo cặp (roleId và menuId) ta chỉ có 1 bản ghi duy nhất trong DB thỏa mãn ==> lấy ra p/tử đầu tiên của list trả về, nếu rỗng thì trả về null
		if(!CollectionUtils.isEmpty(auths)) {
			return auths.get(0);
		}
		return null;
	}
		
}
