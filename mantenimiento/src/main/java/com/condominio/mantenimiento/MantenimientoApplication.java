package com.condominio.mantenimiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MantenimientoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MantenimientoApplication.class, args);
	}

}