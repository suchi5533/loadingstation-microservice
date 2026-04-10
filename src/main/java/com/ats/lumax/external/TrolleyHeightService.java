package com.ats.lumax.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "trolleyHeightService", path = "/api/trolleyHeight")
public interface TrolleyHeightService {

	@GetMapping("/findTrolleyHeight")
	public ResponseEntity<String> findTrolleyHeight();
}
