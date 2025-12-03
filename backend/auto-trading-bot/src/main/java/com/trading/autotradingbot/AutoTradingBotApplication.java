package com.trading.autotradingbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoTradingBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoTradingBotApplication.class, args);
	}

}
