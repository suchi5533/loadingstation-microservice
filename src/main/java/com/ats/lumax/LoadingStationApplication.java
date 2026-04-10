package com.ats.lumax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.bind.annotation.CrossOrigin;
//import com.ats.lumax.config.KafkaTopicProperties;



@SpringBootApplication
@EnableFeignClients
@EnableRetry
//@EnableConfigurationProperties(KafkaTopicProperties.class)
public class LoadingStationApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoadingStationApplication.class, args);




	}

}
