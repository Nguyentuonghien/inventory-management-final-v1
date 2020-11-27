package inventory.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
	
	private Properties properties = null;
	private static ConfigLoader instance = null;
	String fileName = "config.properties";
	
	public ConfigLoader() {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
		// file đọc khác null
		if(stream != null) {
			properties = new Properties();
			try {
				properties.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// vì hàm khởi tạo của ta là private, ta sẽ sử dụng singleton cho đơn luồng:
	public static ConfigLoader getInstance() {
		if(instance == null) {
			instance = new ConfigLoader();
		}
		return instance;
	}
	
	// hàm đọc các file properties(ở đây là config.properties):
	public String getValue(String key) {
		if(properties.containsKey(key)) {
			return properties.getProperty(key);
		}
		return null;
	}	
	
}
