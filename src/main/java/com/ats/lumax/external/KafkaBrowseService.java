package com.ats.lumax.external;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ats.lumax.dtos.HasChangedRequest;
import com.ats.lumax.dtos.ProcessBrowseDataRequest;

@FeignClient(name = "kafka", path = "/api/kafkaBrowse")
public interface KafkaBrowseService {

	@PostMapping("/processBrowseData")
	void processBrowseData(@RequestBody ProcessBrowseDataRequest request);

	@PostMapping("/hasChanged")
	boolean hasChanged(@RequestBody HasChangedRequest request);
}
