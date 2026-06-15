package com.upsin.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling  //  enciende el motor del reloj interno
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
