package inventory.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashingPassword {
	
	// khi ta băm password ra và thêm nhiều thứ vào, chuỗi SALT(là một chuỗi ngẫu nhiên được tạo ra cho mỗi chuỗi băm) 
	// sẽ làm tăng độ phức tạp của password và tránh việc truy ngược lại pass gốc 
	public static final String SALT = "inventory_management";
	public static String encript(String originPassword) {
		String result = null;
		byte[] salt = SALT.getBytes();
		try {
			// MessageDigest: để cấu hình sử dụng hàm băm SHA-512 với chuỗi salt đã tạo
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt);
			// đưa password gốc ban đầu về dạng byte, sau đó dùng Base64 để encode password này
			byte[] hashPassword = md.digest(originPassword.getBytes(StandardCharsets.US_ASCII));
			result = Base64.getEncoder().encodeToString(hashPassword).substring(0, 32);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args) {
		String rs = encript("1234567");
		System.out.println(rs);
	}
	
}





