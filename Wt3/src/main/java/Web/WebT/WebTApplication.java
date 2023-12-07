package Web.WebT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class WebTApplication {
	public static void main(String[] args) throws IOException, TimeoutException {
		SpringApplication.run(WebTApplication.class, args);
	}
}
