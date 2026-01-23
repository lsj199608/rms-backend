package hrj.rms.rms_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RmsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RmsBackendApplication.class, args);
	}

}
