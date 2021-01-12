package inventory.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import inventory.dao.UserDAO;
import inventory.dao.UserRoleDAO;
import inventory.model.Paging;
import inventory.model.Role;
import inventory.model.UserRole;
import inventory.model.Users;
import inventory.util.HashingPassword;

@Service
public class UserService {
	
	public static final Logger log = Logger.getLogger(UserService.class);
	
	@Autowired
	private UserDAO<Users> userDAO;
	
	@Autowired
	private UserRoleDAO<UserRole> userRoleDAO;
	
	public List<Users> findByProperty(String property, Object value) {	
		log.info("Find user by property start: ");	
		return userDAO.findByProperty(property, value);
	}
	
	public Users findUserById(int id) {
		log.info("Find user by id: ");
		return userDAO.findById(Users.class, id);
	}
	
	public void saveUser(Users user) {
		user.setActiveFlag(1);
		user.setCreateDate(new Date());
		user.setUpdateDate(new Date());
		// khi save, ta sẽ mã hóa luôn password từ jsp của user nhập vào
		user.setPassword(HashingPassword.encript(user.getPassword()));
		userDAO.save(user);
		// khi save vaò db xong ta sẽ có được id của user --> lấy id đó để insert vào bảng user_role
		UserRole userRole = new UserRole();
		Role role = new Role();
		role.setId(user.getRoleID());
		userRole.setRole(role);
		userRole.setUsers(user);
		userRole.setActiveFlag(1);
		userRole.setCreateDate(new Date());
		userRole.setUpdateDate(new Date());
		userRoleDAO.save(userRole);
	}
	
	public void updateUser(Users users) {
		// trong form edit có 3 fields của user (name, email, username) và 1 field của user_role(là role: admin hoặc staff)
		// tìm user trong db theo id, nếu user tồn tại ta sẽ update user_role
		Users user = findUserById(users.getId());
		if(user != null) {
			UserRole userRole = (UserRole)user.getUserRoles().iterator().next();
			Role role = userRole.getRole();
			role.setId(users.getRoleID());
			userRole.setRole(role);
			userRole.setUpdateDate(new Date());
			user.setName(users.getName());
			user.setEmail(users.getEmail());
			user.setUserName(users.getUserName());
			user.setUpdateDate(new Date());
			userRoleDAO.update(userRole);
		}
		userDAO.update(user);
	}
	
	public void deleteUser(Users user) {
		user.setActiveFlag(0);
		user.setUpdateDate(new Date());
		userDAO.update(user);
	}
	
	public List<Users> getListUser(Users user, Paging paging) {
		StringBuilder querryString = new StringBuilder();
		Map<String, Object> mapParams = new HashMap<>();
		if(user != null) {
			if(!StringUtils.isEmpty(user.getName())) {
				querryString.append(" and model.name like :name");
				mapParams.put("name", "%"+user.getName()+"%");
			}
			if(!StringUtils.isEmpty(user.getUserName())) {
				querryString.append(" and model.userName like :userName");
				mapParams.put("userName", "%"+user.getUserName()+"%");
			}
		}
		return userDAO.findAll(querryString.toString(), mapParams, paging);
	}
	
}


