 package com.ats.lumax.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ats.lumax.entity.MasterPalletInformationEntity;
import com.ats.lumax.external.OpcUaService;
import com.ats.lumax.external.ReadDataService;
import com.ats.lumax.service.ILoadingStationService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/loadingStation")
public class LoadingStationController {

	@Autowired
	private ILoadingStationService loadingStationService;

	@Autowired
	private ReadDataService readDataService;

	@Autowired
	private OpcUaService opcUaService;

	@GetMapping("/fetchPalletPresent")
	public ResponseEntity<?> fetchPalletPresent() {
		return loadingStationService.fetchPalletPresent();
	}

	@GetMapping("/browse")
	public ResponseEntity<List<String>> browseTags(@RequestParam(required = false) String startingNodeParam) {
		return readDataService.browseTags(startingNodeParam);
	}

	@GetMapping("/connect")
	public void connect() {
		opcUaService.connect();
	}

	@GetMapping("/readValue")
	public ResponseEntity<?> readValue(@RequestParam String nodeId) {
		return readDataService.readValue(nodeId);
	}

	@PostMapping("/submitWorkDone")
	public ResponseEntity<?> submitWorkDone(@RequestBody Object materialCodeValue) {
		return loadingStationService.submitWorkDone(materialCodeValue);
	}
	

	@GetMapping("/home")
	public String  home() {
		return "hi from spring boot";
	}

}
