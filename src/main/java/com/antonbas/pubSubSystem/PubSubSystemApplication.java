package com.antonbas.pubSubSystem;

import com.antonbas.pubSubSystem.ws.WsMessagesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PubSubSystemApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(PubSubSystemApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(PubSubSystemApplication.class);
	}

	@Bean
	public WsMessagesConfig wsMessagesConfig() {
		return new WsMessagesConfig();
	}

}
