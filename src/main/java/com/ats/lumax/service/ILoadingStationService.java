package com.ats.lumax.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ILoadingStationService {

	public ResponseEntity<?> fetchPalletPresent();

//	public ResponseEntity<?> submitWorkDone(Object materialCodeValue);
	
	public ResponseEntity<?> submitWorkDone(Object object);

}
