package com.ats.lumax.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "opcconnection", path = "/api/connection")
public interface OpcUaService {

	@GetMapping("/status")
	public ResponseEntity<String> getConnectionStatus();

	@GetMapping("/init")
	public void init();

	@GetMapping("/connect")
	public void connect();
}
