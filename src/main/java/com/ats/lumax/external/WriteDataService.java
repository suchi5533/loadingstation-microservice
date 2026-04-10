package com.ats.lumax.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ats.lumax.dtos.WriteRequest;

@FeignClient(name = "writedata/api/write")
public interface WriteDataService {

	@PostMapping("/write-node")
	public ResponseEntity<?> writeValue(@RequestBody WriteRequest request);

}
