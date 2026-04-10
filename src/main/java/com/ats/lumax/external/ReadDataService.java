package com.ats.lumax.external;

import java.util.List;
import java.util.Map;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ats.lumax.dtos.TagValueDTO;

@FeignClient(name = "readdata/api/read")
public interface ReadDataService {

	@GetMapping("/readValue")
	public ResponseEntity<?> readValue(@RequestParam String nodeId);

	@GetMapping("/publishToBrowseData")
	public void publishToBrowseData(@RequestParam String identifier, @RequestParam DataValue dataValue);

	@GetMapping("/browse")
	public ResponseEntity<List<String>> browseTags(@RequestParam(required = false) String startingNodeParam);

	@GetMapping("/read-node")
	public Map<String, DataValue> readValuesUnderNode(@RequestParam(required = false) String nodeId);

	@GetMapping("/readTagValuesSimplified")
	public List<TagValueDTO> readTagValuesSimplified(@RequestParam(required = false) String startingNode);

//	@GetMapping("/status")
//	public ResponseEntity<String> getConnectionStatus();
//
//	@GetMapping("/init")
//	public void init();

}
