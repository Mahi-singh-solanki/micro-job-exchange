package com.microjob.microjob_exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement // <--- CRITICAL: Must be present
public class MicrojobApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrojobApplication.class, args);
	}

}
