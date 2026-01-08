package com.example.transaction_service_fraud_detection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransactionServiceFraudDetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionServiceFraudDetectionApplication.class, args);
	}

}
