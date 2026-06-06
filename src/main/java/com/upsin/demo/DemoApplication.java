package com.upsin.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class DemoApplication {

	@PostConstruct
	public void init() {
		// Obliga a todo el servidor a usar el horario del Pacífico de México
		TimeZone.setDefault(TimeZone.getTimeZone("America/Mazatlan"));
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
