package com.example.Ecommerce_Muebleria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EcommerceMuebleriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceMuebleriaApplication.class, args);
	}

}
