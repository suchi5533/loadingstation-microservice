package com.ats.lumax.serviceImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ats.lumax.dtos.ProcessBrowseDataRequest;
import com.ats.lumax.dtos.TagValueDTO;
import com.ats.lumax.dtos.WriteRequest;
import com.ats.lumax.entity.CurrentStockDetailsEntity;
import com.ats.lumax.entity.MasterPalletInformationEntity;
import com.ats.lumax.entity.MasterProductVariantDetailsEntity;
import com.ats.lumax.external.KafkaBrowseService;
import com.ats.lumax.external.ReadDataService;
import com.ats.lumax.external.TrolleyHeightService;
import com.ats.lumax.external.WriteDataService;
import com.ats.lumax.repository.ICurrentStockDetailsRepository;
import com.ats.lumax.repository.IMasterPalletInformationRepository;
import com.ats.lumax.repository.IMasterProductVariantDetailsRepository;
import com.ats.lumax.service.ILoadingStationService;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class LoadingStationServiceImpl implements ILoadingStationService {

	@Autowired
	private IMasterPalletInformationRepository masterPalletInformationRepository;

	@Autowired
	private IMasterProductVariantDetailsRepository masterProductVariantDetailsRepository;

	@Autowired
	private ICurrentStockDetailsRepository currentStockDetailsRepository;

	@Autowired
	private ReadDataService readDataService;

	@Autowired
	private WriteDataService writeDataService;

	@Autowired
	private KafkaBrowseService kafkaBrowseService;

	@Autowired
	private TrolleyHeightService trolleyHeightService;

//	@Autowired
//	private KafkaTopicProperties kafkaTopicProperties;

	@Value("${plc.kafka.loadingStationTrolleyHeight}")
	private String loadingStationTrolleyHeight;

	@Override
//	@Retry(name = "fetchPalletPresentRetry", fallbackMethod = "fetchPalletPresentFallback")
	@Retry(name = "Retry", fallbackMethod = "fetchPalletPresentFallback")
	@CircuitBreaker(name = "Retry", fallbackMethod = "fetchPalletPresentFallback")
	public ResponseEntity<?> fetchPalletPresent() {
		System.out.println("IN FETCH PALLET PRESENT");
		String fullTagNodeId = "ns=3;s=\"PLC_To_WMS\".\"LOADING_STATION_PALLET_PRESENT (CH-01)\"";
		List<TagValueDTO> values = readDataService.readTagValuesSimplified(fullTagNodeId);

		String fullTagNodeId5 = "ns=3;s=\"PLC_To_WMS\".\"NEWM\".\"TASK_TYPE\"";
		List<TagValueDTO> values5 = readDataService.readTagValuesSimplified(fullTagNodeId5);

		if (values == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag read returned null.");
		}
		if (values.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the tag.");
		}
		TagValueDTO dto = values.get(0);
		if (dto.getValue() == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag value is null.");
		}
		if (dto.getValue() instanceof Number && dto.getValue().toString().trim().equals("1")) {
			String targetTag = "ns=3;s=\"PLC_To_WMS\".\"LOADING_STATION_MATERIAL_CODE (CH-01)\"";
			List<TagValueDTO> values1 = readDataService.readTagValuesSimplified(targetTag);
			if (values1 != null && !values1.isEmpty()) {
				TagValueDTO materialCodeDto = values1.get(0); // assuming only one relevant tag
				Object materialCodeValue = materialCodeDto.getValue();
				String[] barcode = materialCodeValue.toString().split(",");
				if (barcode.length >= 5) {
					return ResponseEntity.status(HttpStatus.OK).body(materialCodeValue);
				}
			}
//		} catch (FeignException e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body("FeignException occurred: " + e.getMessage());
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body("Exception occurred: " + e.getMessage());
//		}
		}
		return null;
	}

//	public ResponseEntity<?> fetchPalletPresentFallback(FeignException e) {
//		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//				.body("Retry failed in fetchPalletPresent due to FeignException: " + e.getMessage());
//	}

	private static final int MAX_RETRIES = 5;

//	@Override
//	public ResponseEntity<?> submitWorkDone(Object ignoredInput) {
//		return submitWorkDone(0);
//	}

//	private ResponseEntity<?> submitWorkDone(int attempt) {
//		LocalDateTime currentDateTime1 = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
//		if (attempt >= MAX_RETRIES) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body("Max retries reached for fetchPalletCode()");
//		}
//		ResponseEntity<?> fetchPalletPresent = fetchPalletPresent();
//		if (!fetchPalletPresent.getStatusCode().is2xxSuccessful()) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body("Invalid response status from fetchPalletPresent()");
//		}
//		Object body = fetchPalletPresent.getBody();
//		if (body == null) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No material code tag values found.");
//		}
//		String materialCodeValue = body.toString();
//		String[] barcode = materialCodeValue.split(",");
//		if (barcode.length < 5) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid barcode format");
//		} else {
//			String trolleyNumber = barcode[0];
//			String materialCode = barcode[1];
//			String materialDescription = barcode[2];
//			String customer = barcode[3];
//			int quantity = Integer.parseInt(barcode[4]);
//			if (trolleyNumber.equalsIgnoreCase("NA")) {
//				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid trolley number");
//			}
//			List<MasterPalletInformationEntity> findByTrolleyNumber = masterPalletInformationRepository
//					.findByTrolleyNumber(trolleyNumber);
//			if (!findByTrolleyNumber.isEmpty()) {
//				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Trolley number already exists");
//			}
//			MasterProductVariantDetailsEntity productVariant = masterProductVariantDetailsRepository
//					.findByProductVariantCodeAndProductVariantIsDeleted(materialCode, false);
//			if (productVariant == null) {
//				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid product variant code");
//			}
//			if (!productVariant.getProductVariantIsActive()) {
//				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//						.body("Product variant code is not active");
//			}
//			Date date = new Date();
//			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//			String currentDateTime = dateFormat.format(date);
//			String wmsTransferOrderId = trolleyNumber + "_" + currentDateTime;
//			List<MasterPalletInformationEntity> existingPallet = masterPalletInformationRepository
//					.findByTrolleyNumberAndIsInfeedMissionGeneratedAndIsOutfeedMissionGeneratedAndIsTransferMissionGeneratedOrderByPalletInformationIdDesc(
//							trolleyNumber, false, false, false);
//			if (!existingPallet.isEmpty()) {
//				CurrentStockDetailsEntity currentStockDetails = currentStockDetailsRepository
//						.findTopByPalletInformationIdOrderByPalletInformationIdDesc(
//								existingPallet.get(0).getPalletInformationId());
//				if (currentStockDetails != null) {
//					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mission is already generated");
//				} else {
//					existingPallet.get(0).setInfeedMissionGenerated(false);
//					existingPallet.get(0).setOutfeedMissionGenerated(false);
//					existingPallet.get(0).setTransferMissionGenerated(false);
//					masterPalletInformationRepository.save(existingPallet.get(0));
//				}
//			}
//			MasterPalletInformationEntity masterPalletInformationEntity = new MasterPalletInformationEntity();
//			masterPalletInformationEntity.setProductVariantId(productVariant.getProductVariantId());
//			masterPalletInformationEntity.setStationId(1);
//			masterPalletInformationEntity.setQuantity(quantity);
//			masterPalletInformationEntity.setPalletStatusId(1);
//			masterPalletInformationEntity.setPalletCDateTime(currentDateTime1);
//			masterPalletInformationEntity.setUserId(1);
//			masterPalletInformationEntity.setPalletCode(Arrays.toString(barcode));
//			masterPalletInformationEntity.setTrolleyNumber(trolleyNumber);
//			masterPalletInformationEntity.setCustomer(customer);
//			masterPalletInformationEntity.setBatchNumber("NA");
//			masterPalletInformationEntity.setTrolleyHeight("NA");
//			masterPalletInformationEntity.setWmsTransferOrderId(wmsTransferOrderId);
//			masterPalletInformationEntity.setInfeedMissionGenerated(true);
//			masterPalletInformationEntity.setOutfeedMissionGenerated(false);
//			masterPalletInformationEntity.setTransferMissionGenerated(false);
//			masterPalletInformationEntity.setPalletInformationIsDeleted(false);
//			masterPalletInformationEntity.setTrolleyDoorClosed(false);
//			masterPalletInformationEntity.setUnLoadingStationWorkDone(false);
//			WriteRequest writeRequest = new WriteRequest();
//			writeRequest.setNodeId("ns=3;s=\"WMS_TO_PLC\".\"LOADING_STATION_WORK_DONE (CH-01)\"");
//
//			writeRequest.setValue("true");
//			writeDataService.writeValue(writeRequest);
//			masterPalletInformationEntity.setLoadingStationWorkDone(true);
//			masterPalletInformationRepository.save(masterPalletInformationEntity);
//			Map<String, Object> browseData = new HashMap<>();
//			browseData.put("PalletInformationId", masterPalletInformationEntity.getPalletInformationId());
//			browseData.put("ProductVariantId", masterPalletInformationEntity.getProductVariantId());
//			browseData.put("PalletStatusId", masterPalletInformationEntity.getPalletStatusId());
//			browseData.put("WmsTransferOrderId", masterPalletInformationEntity.getWmsTransferOrderId());
//			browseData.put("BatchNumber", masterPalletInformationEntity.getBatchNumber());
//			browseData.put("TrolleyNumber", barcode[0]);
//			browseData.put("CurrentDateTime", currentDateTime1);
//			ProcessBrowseDataRequest processBrowseDataRequest = new ProcessBrowseDataRequest();
//			processBrowseDataRequest.setTopicName(loadingStationTrolleyHeight);
//			processBrowseDataRequest.setBrowseData(browseData);
//			processBrowseDataRequest.setNodeId(loadingStationTrolleyHeight);
//			try {
//				kafkaBrowseService.processBrowseData(processBrowseDataRequest);
//			} catch (Exception e) {
//				e.printStackTrace();
//				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//						.body("Error sending data to LoadingStation_TrolleyHeight_Topic");
//			}
//			ResponseEntity<String> trolleyHeightResponse = trolleyHeightService.findTrolleyHeight();
//			if (!trolleyHeightResponse.getStatusCode().is2xxSuccessful()) {
//				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch trolley height");
//			}
//			Object trolleyHeightBody = trolleyHeightResponse.getBody();
//			if (trolleyHeightBody == null) {
//				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trolley height response is empty");
//			}
//
//		} else {
//			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
//					.body("Pallet not present: " + dto.getValue().getClass().getSimpleName());
//
//		}
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error: ");
//	}

	public ResponseEntity<?> fetchPalletPresentFallback(FeignException e) {
		System.out.println("In fetchPalletPresentFallback");
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Retry failed in fetchPalletPresent while reading tags due to FeignException ");
	}

	@Transactional
	public ResponseEntity<?> submitWorkDone(Object object) {
		LocalDateTime currentDateTime1 = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
 
		// 1. Read the pallet present signal
		ResponseEntity<?> fetchPalletPresent = fetchPalletPresent();
		if (!fetchPalletPresent.getStatusCode().is2xxSuccessful()
				&& !fetchPalletPresent.getBody().toString().isEmpty()) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Invalid response status from fetchPalletPresent()");
		}
 
		Object body = fetchPalletPresent.getBody();
		if (body == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No material code tag values found.");
		}
 
		// 2. Parse the barcode data
		String materialCodeValue = body.toString().replace("[", "").replace("]", "");
		String[] barcode = materialCodeValue.split(",");
 
		if (barcode.length < 5) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid barcode format");
		}
 
		String trolleyNumber = barcode[0].trim();
		String materialCode = barcode[1].trim();
		String materialDescription = barcode[2].trim();
		String customer = barcode[3].trim();
		int quantity = Integer.parseInt(barcode[4].trim());
 
		if (trolleyNumber.equalsIgnoreCase("NA")) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid trolley number");
		}
		String wmsTransferOrderId = trolleyNumber + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String palletCodeValue = String.join(",", barcode);
		List<MasterPalletInformationEntity> findByTrolleyNumberAndIsInfeedMissionGenerated = masterPalletInformationRepository
				.findByTrolleyNumberAndIsInfeedMissionGenerated("9999", false);
		if (!findByTrolleyNumberAndIsInfeedMissionGenerated.isEmpty()) {
			findByTrolleyNumberAndIsInfeedMissionGenerated.get(0).setTrolleyNumber(trolleyNumber);
			findByTrolleyNumberAndIsInfeedMissionGenerated.get(0).setPalletCode(palletCodeValue);
			findByTrolleyNumberAndIsInfeedMissionGenerated.get(0).setQuantity(quantity);
			findByTrolleyNumberAndIsInfeedMissionGenerated.get(0).setCustomer(customer);
			findByTrolleyNumberAndIsInfeedMissionGenerated.get(0).setWmsTransferOrderId(wmsTransferOrderId);
			findByTrolleyNumberAndIsInfeedMissionGenerated.get(0).setPalletCDateTime(currentDateTime1);
			findByTrolleyNumberAndIsInfeedMissionGenerated.get(0).setInfeedMissionGenerated(false);
			masterPalletInformationRepository.save(findByTrolleyNumberAndIsInfeedMissionGenerated.get(0));
		} else {
			// 3. Validate against existing trolley numbers
			List<MasterPalletInformationEntity> findByTrolleyNumber = masterPalletInformationRepository
					.findByTrolleyNumber(trolleyNumber);
			if (!findByTrolleyNumber.isEmpty()) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Trolley number already exists");
			}
 
			// 4. Validate product variant
			MasterProductVariantDetailsEntity productVariant = masterProductVariantDetailsRepository
					.findByProductVariantCodeAndProductVariantIsDeleted(materialCode, false);
			if (productVariant == null || !productVariant.getProductVariantIsActive()) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Invalid or inactive product variant code");
			}
 
			// String wmsTransferOrderId = trolleyNumber + "_" + new
			// SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
 
			// 5. Check for existing missions for this trolley
			List<MasterPalletInformationEntity> existingPallet = masterPalletInformationRepository
					.findByTrolleyNumberAndIsInfeedMissionGeneratedAndIsOutfeedMissionGeneratedAndIsTransferMissionGeneratedOrderByPalletInformationIdDesc(
							trolleyNumber, false, false, false);
 
			if (!existingPallet.isEmpty()) {
				CurrentStockDetailsEntity currentStockDetails = currentStockDetailsRepository
						.findTopByPalletInformationIdOrderByPalletInformationIdDesc(
								existingPallet.get(0).getPalletInformationId());
 
				if (currentStockDetails != null) {
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mission is already generated");
				} else {
					existingPallet.get(0).setInfeedMissionGenerated(false);
					existingPallet.get(0).setOutfeedMissionGenerated(false);
					existingPallet.get(0).setTransferMissionGenerated(false);
					masterPalletInformationRepository.save(existingPallet.get(0));
				}
			}
 
			// 6. Create new Pallet Information record
			MasterPalletInformationEntity masterPalletInformationEntity = new MasterPalletInformationEntity();
			masterPalletInformationEntity.setProductVariantId(productVariant.getProductVariantId());
			masterPalletInformationEntity.setStationId(1);
			masterPalletInformationEntity.setQuantity(quantity);
			masterPalletInformationEntity.setPalletStatusId(1);
			masterPalletInformationEntity.setPalletCDateTime(currentDateTime1);
			masterPalletInformationEntity.setUserId(1);
			masterPalletInformationEntity.setPalletCode(palletCodeValue);
			masterPalletInformationEntity.setTrolleyNumber(trolleyNumber);
			masterPalletInformationEntity.setCustomer(customer);
			masterPalletInformationEntity.setBatchNumber("NA");
			masterPalletInformationEntity.setTrolleyHeight("NA");
			masterPalletInformationEntity.setWmsTransferOrderId(wmsTransferOrderId);
			masterPalletInformationEntity.setInfeedMissionGenerated(false);
			masterPalletInformationEntity.setOutfeedMissionGenerated(false);
			masterPalletInformationEntity.setTransferMissionGenerated(false);
			masterPalletInformationEntity.setPalletInformationIsDeleted(false);
			masterPalletInformationEntity.setTrolleyDoorClosed(false);
			masterPalletInformationEntity.setUnLoadingStationWorkDone(false);
 
			// 7. Write to PLC
			try {
				WriteRequest writeRequest = new WriteRequest();
				writeRequest.setNodeId("ns=3;s=\"WMS_TO_PLC\".\"LOADING_STATION_WORK_DONE (CH-01)\"");
				writeRequest.setValue("true");
				writeDataService.writeValue(writeRequest);
				masterPalletInformationEntity.setLoadingStationWorkDone(true);
			} catch (Exception e) {
				e.printStackTrace();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Fallback: Failed to write to PLC via writeDataService");
			}
 
			// 8. Save to database
			masterPalletInformationRepository.save(masterPalletInformationEntity);
 
			// 9. Wait indefinitely until pallet present tag becomes "1"
			while (true) {
				ResponseEntity<?> readPalletPresentTag = readPalletPresentTag();
				if (readPalletPresentTag.getStatusCode().is2xxSuccessful()) {
					Object value = readPalletPresentTag.getBody();
					if (value != null && value.toString().trim().equals("1")) {
						break; // Continue processing
					}
				}
 
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body("Thread interrupted while waiting for pallet present tag");
				}
			}
 
			// 10. Send Kafka Message
			try {
				Map<String, Object> browseData = new HashMap<>();
				browseData.put("PalletInformationId", masterPalletInformationEntity.getPalletInformationId());
				browseData.put("ProductVariantId", masterPalletInformationEntity.getProductVariantId());
				browseData.put("PalletStatusId", masterPalletInformationEntity.getPalletStatusId());
				browseData.put("WmsTransferOrderId", masterPalletInformationEntity.getWmsTransferOrderId());
				browseData.put("BatchNumber", masterPalletInformationEntity.getBatchNumber());
				browseData.put("TrolleyNumber", trolleyNumber);
				browseData.put("CurrentDateTime", currentDateTime1);
 
				ProcessBrowseDataRequest processBrowseDataRequest = new ProcessBrowseDataRequest();
				processBrowseDataRequest.setTopicName(loadingStationTrolleyHeight);
				processBrowseDataRequest.setBrowseData(browseData);
				processBrowseDataRequest.setNodeId(loadingStationTrolleyHeight);
 
				kafkaBrowseService.processBrowseData(processBrowseDataRequest);
			} catch (Exception e) {
				e.printStackTrace();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Fallback: Failed to send data to Kafka topic");
			}
 
			// 11. Fetch Trolley Height
			try {
				Thread.sleep(2000);
//				ResponseEntity<String> trolleyHeightResponse = trolleyHeightService.findTrolleyHeight();
//				if (!trolleyHeightResponse.getStatusCode().is2xxSuccessful()) {
//					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//							.body("Fallback: Trolley height service failed with non-2xx response");
//				}
 //added by Uday Raut    Date:- 17092025
				while (true) {
					try {
						ResponseEntity<String> trolleyHeightResponse = trolleyHeightService.findTrolleyHeight();
 
						// Check for HTTP 204 No Content response
						if (trolleyHeightResponse.getStatusCode() != HttpStatus.NO_CONTENT) {
							return trolleyHeightResponse; // Exit when 204 No Content is received
						}
 
						System.out.println(
								"Trolley height service call did not return 204 No Content. Retrying immediately...");
 
					} catch (Exception e) {
						System.out.println("Exception occurred during trolley height service call: " + e.getMessage());
						// Optionally log the stacktrace or handle the exception
					}
				}
 
//				if (trolleyHeightResponse.getBody() == null) {
//					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//					return ResponseEntity.status(HttpStatus.NOT_FOUND)
//							.body("Fallback: Trolley height response body is null");
//				}
			} catch (Exception e) {
				e.printStackTrace();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Fallback: Exception occurred while calling trolley height service");
			}
		}
		// 12. Final Success Response
		return ResponseEntity.ok("Data loaded at loading station: " + materialCodeValue);
	}

	public ResponseEntity<?> readPalletPresentTag() {
		String fullTagNodeId = "ns=3;s=\"PLC_To_WMS\".\"STKR1_Pick-up Position Pallet Present\"";
		List<TagValueDTO> values = readDataService.readTagValuesSimplified(fullTagNodeId);

		if (values == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag read returned null.");
		}
		if (values.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the tag.");
		}
		TagValueDTO dto = values.get(0);
		if (dto.getValue() == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag value is null.");
		}

		return ResponseEntity.status(HttpStatus.OK).body(dto.getValue());

	}

}
