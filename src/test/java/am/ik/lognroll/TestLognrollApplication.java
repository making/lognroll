package am.ik.lognroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestLognrollApplication {

	public static void main(String[] args) {
		SpringApplication.from(LognrollApplication::main).with(TestLognrollApplication.class).run(args);
	}

}
