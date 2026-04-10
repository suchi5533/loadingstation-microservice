// package com.ats.lumax.serviceImpl;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.*;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import com.ats.lumax.dtos.TagValueDTO;
// import com.ats.lumax.entity.CurrentStockDetailsEntity;
// import com.ats.lumax.entity.MasterPalletInformationEntity;
// import com.ats.lumax.entity.MasterProductVariantDetailsEntity;
// import com.ats.lumax.external.ReadDataService;
// import com.ats.lumax.external.WriteDataService;
// import com.ats.lumax.repository.ICurrentStockDetailsRepository;
// import com.ats.lumax.repository.IMasterPalletInformationRepository;
// import com.ats.lumax.repository.IMasterProductVariantDetailsRepository;

// import java.util.List;
// import java.util.Collections;

// @ExtendWith(MockitoExtension.class)
// public class LoadingStationServiceImplTest {

// 	@Mock
// 	private ReadDataService readDataService;

// 	@Mock
// 	private WriteDataService writeDataService;

// 	@Mock
// 	private IMasterPalletInformationRepository masterPalletInformationRepository;

// 	@Mock
// 	private IMasterProductVariantDetailsRepository masterProductVariantDetailsRepository;

// 	@Mock
// 	private ICurrentStockDetailsRepository currentStockDetailsRepository;

// 	@Spy
// 	@InjectMocks
// 	private LoadingStationServiceImpl loadingStationService;

// 	@BeforeEach
// 	public void setUp() {
// 		MockitoAnnotations.openMocks(this);
// 	}

// 	private static final String TAG = "ns=3;s=\"PLC_To_WMS\".\"LOADING_STATION_PALLET_PRESENT (CH-01)\"";
// 	private static final String TAG1 = "ns=3;s=\"PLC_To_WMS\".\"LOADING_STATION_MATERIAL_CODE (CH-01)\"";
// 	private static final String VALID_BARCODE = "TROL1,MC1,Desc,CUST,5";
// 	private static final String SHORT_BARCODE = "A,B,C";
// 	private static final String NA_TROLLEY = "NA,MC1,Desc,CUST,5";

// 	@Test
// 	void testTagValuesListReturnedIsNullReturnsNotFound() {
	 
// 	    when(readDataService.readTagValuesSimplified(TAG)).thenReturn(null);

// 	    ResponseEntity<?> response = loadingStationService.fetchPalletPresent();

// 	    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status should be 404 for null tag values list");
// 	    assertEquals("Tag read returned null.", response.getBody(), "Message should explain null tag values list");

// 	    verify(readDataService).readTagValuesSimplified(TAG);
// 	}

// 	@Test
// 	void testTagValuesListIsEmptyReturnsNotFound() {
	
// 	    // Mock empty list returned for pallet present tag
// 	    when(readDataService.readTagValuesSimplified(TAG)).thenReturn(Collections.emptyList());

// 	    // Call method
// 	    ResponseEntity<?> response = loadingStationService.fetchPalletPresent();

// 	    // Assert status and body message match method's response for empty list
// 	    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status should be 404 for empty tag values list");
// 	    assertEquals("No data found for the tag.", response.getBody(), "Message should explain no data found for the tag");

// 	    // Verify the service method was called
// 	    verify(readDataService).readTagValuesSimplified(TAG);
// 	}

// 	@Test
// 	void testTagValueDTOValueIsNullReturnsNotFound() {

// 		TagValueDTO dto = new TagValueDTO();
// 		dto.setNodeId(TAG);
// 		dto.setValue(null);

// 		when(readDataService.readTagValuesSimplified(TAG)).thenReturn(List.of(dto));

// 		ResponseEntity<?> response = loadingStationService.fetchPalletPresent();

// 		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status should be 404 for null tag value in DTO");
// 		assertEquals("Tag value is null.", response.getBody(), "Message should explain null tag value in DTO");

// 		verify(readDataService).readTagValuesSimplified(TAG);
// 	}

// 	@Test
// 	void testTagValueIsZero() {
// 		// Mock for PALLET_PRESENT
// 		TagValueDTO dto = new TagValueDTO();
// 		dto.setNodeId(TAG);
// 		dto.setValue(0); // Value is 0

// 		// You can omit mocking TAG1, because it shouldn't be read

// 		when(readDataService.readTagValuesSimplified(TAG)).thenReturn(List.of(dto));

// 		ResponseEntity<?> response = loadingStationService.fetchPalletPresent();

// 		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode(), "Should return 422 for tag value 0");
// 		assertTrue(response.getBody().toString().contains("Integer"), "Should mention unexpected value type");
// 	}

// 	@Test
// 	void testTagValueIsOne() {
// 		// Mock for PALLET_PRESENT
// 		TagValueDTO dto = new TagValueDTO();
// 		dto.setNodeId(TAG);
// 		dto.setValue(1);

// 		// Mock for MATERIAL_CODE
// 		TagValueDTO materialCodeDto = new TagValueDTO();
// 		materialCodeDto.setNodeId(TAG1);
// 		materialCodeDto.setValue(VALID_BARCODE);

// 		when(readDataService.readTagValuesSimplified(TAG)).thenReturn(List.of(dto));
// 		when(readDataService.readTagValuesSimplified(TAG1)).thenReturn(List.of(materialCodeDto));

// 		ResponseEntity<?> response = loadingStationService.fetchPalletPresent();

// 		assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 for tag value 1");
// 		assertEquals(VALID_BARCODE, response.getBody(), "Expected correct material code response");

// 		// Additional check: barcode split and length
// 		String[] barcodeParts = VALID_BARCODE.split(",");
// 		assertTrue(barcodeParts.length >= 5, "Barcode should contain at least 5 parts separated by commas");

// 		// Optional: verify service interaction
// 		verify(readDataService).readTagValuesSimplified(TAG);
// 		verify(readDataService).readTagValuesSimplified(TAG1);
// 	}

// 	@Test
// 	void testExceptionDuringReadReturnsInternalServerError() {
	
// 	    when(readDataService.readTagValuesSimplified(TAG))
// 	        .thenThrow(new RuntimeException("Simulated error"));

// 	    ResponseEntity<?> response = loadingStationService.fetchPalletPresent();

// 	    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Should return 500 on exception");
// 	    assertTrue(response.getBody().toString().contains("Exception occurred"), "Response body should contain exception message");

// 	    verify(readDataService).readTagValuesSimplified(TAG);
// 	}

// 	@Test
// 	void testSubmitWorkDoneSuccess() {
// 		// Mock fetchPalletPresent to return valid barcode
// 		doReturn(ResponseEntity.ok(VALID_BARCODE)).when(loadingStationService).fetchPalletPresent();

// 		// Mock no existing trolley number duplicates
// 		when(masterPalletInformationRepository.findByTrolleyNumber("TROL1")).thenReturn(Collections.emptyList());

// 		// Mock product variant lookup
// 		MasterProductVariantDetailsEntity variant = new MasterProductVariantDetailsEntity();
// 		variant.setProductVariantId(1);
// 		variant.setProductVariantIsActive(true);
// 		when(masterProductVariantDetailsRepository.findByProductVariantCodeAndProductVariantIsDeleted("MC1", false))
// 				.thenReturn(variant);

// 		// Mock no existing pallets with pending missions (empty list)
// 		when(masterPalletInformationRepository
// 				.findByTrolleyNumberAndIsInfeedMissionGeneratedAndIsOutfeedMissionGeneratedAndIsTransferMissionGeneratedOrderByPalletInformationIdDesc(
// 						"TROL1", false, false, false))
// 				.thenReturn(Collections.emptyList());

// 		// Act
// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		// Assert
// 		assertEquals(HttpStatus.OK, response.getStatusCode());
// 		assertEquals(VALID_BARCODE, response.getBody());

// 		// Verify writeDataService was called
// 		verify(writeDataService).writeValue(any());

// 		// Verify masterPalletInformationRepository.save was called once for new pallet
// 		verify(masterPalletInformationRepository, times(1)).save(any(MasterPalletInformationEntity.class));
// 	}

// 	@Test
// 	void testSubmitWorkDoneBadFetchStatus() {
// 		doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fetch pallet present does not return 2x success"))
// 				.when(loadingStationService).fetchPalletPresent();

// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
// 		assertEquals("Invalid response status from fetchPalletPresent()", response.getBody());
// 	}

// 	@Test
// 	void testSubmitWorkDoneNullBody() {
// 		doReturn(ResponseEntity.ok(null)).when(loadingStationService).fetchPalletPresent();

// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
// 		assertEquals("No material code tag values found.", response.getBody());
// 	}

// 	@Test
// 	void testSubmitWorkDoneShortBarcode() {
// 		doReturn(ResponseEntity.ok(SHORT_BARCODE)).when(loadingStationService).fetchPalletPresent();

// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
// 		assertEquals("Invalid barcode format", response.getBody());
// 	}

// 	@Test
// 	void testSubmitWorkDoneInvalidTrolley() {
// 		doReturn(ResponseEntity.ok(NA_TROLLEY)).when(loadingStationService).fetchPalletPresent();

// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
// 		assertEquals("Invalid trolley number", response.getBody());
// 	}

// 	@Test
// 	void testSubmitWorkDoneTrolleyAlreadyExists() {
// 		doReturn(ResponseEntity.ok(VALID_BARCODE)).when(loadingStationService).fetchPalletPresent();

// 		when(masterPalletInformationRepository.findByTrolleyNumber("TROL1"))
// 				.thenReturn(List.of(new MasterPalletInformationEntity()));

// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
// 		assertEquals("Trolley number already exists", response.getBody());
// 	}

// 	@Test
// 	void testSubmitWorkDoneInvalidProductVariant() {
// 		doReturn(ResponseEntity.ok(VALID_BARCODE)).when(loadingStationService).fetchPalletPresent();

// 		when(masterPalletInformationRepository.findByTrolleyNumber("TROL1")).thenReturn(Collections.emptyList());

// 		when(masterProductVariantDetailsRepository.findByProductVariantCodeAndProductVariantIsDeleted("MC1", false))
// 				.thenReturn(null);

// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
// 		assertEquals("Invalid product variant code", response.getBody());
// 	}

// 	@Test
// 	void testSubmitWorkDoneInactiveProductVariant() {
// 		doReturn(ResponseEntity.ok(VALID_BARCODE)).when(loadingStationService).fetchPalletPresent();

// 		when(masterPalletInformationRepository.findByTrolleyNumber("TROL1")).thenReturn(Collections.emptyList());

// 		MasterProductVariantDetailsEntity variant = new MasterProductVariantDetailsEntity();
// 		variant.setProductVariantIsActive(false);

// 		when(masterProductVariantDetailsRepository.findByProductVariantCodeAndProductVariantIsDeleted("MC1", false))
// 				.thenReturn(variant);

// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
// 		assertEquals("Product variant code is not active", response.getBody());
// 	}

// 	@Test
// 	void testSubmitWorkDone_WhenMissionAlreadyGeneratedByStockDetails() {
// 		// Spy to override fetchPalletPresent()
// 		doReturn(ResponseEntity.ok("TROL1,MC1,Desc,CUST,5")).when(loadingStationService).fetchPalletPresent();

// 		// Stub product variant
// 		MasterProductVariantDetailsEntity variant = new MasterProductVariantDetailsEntity();
// 		variant.setProductVariantId(1);
// 		variant.setProductVariantIsActive(true);
// 		when(masterProductVariantDetailsRepository.findByProductVariantCodeAndProductVariantIsDeleted("MC1", false))
// 				.thenReturn(variant);

// 		// No trolley number duplicates
// 		when(masterPalletInformationRepository.findByTrolleyNumber("TROL1")).thenReturn(Collections.emptyList());

// 		// Mock existing pallet for mission check
// 		MasterPalletInformationEntity palletEntity = new MasterPalletInformationEntity();
// 		palletEntity.setPalletInformationId(1); // Set ID required for stock lookup
// 		when(masterPalletInformationRepository
// 				.findByTrolleyNumberAndIsInfeedMissionGeneratedAndIsOutfeedMissionGeneratedAndIsTransferMissionGeneratedOrderByPalletInformationIdDesc(
// 						"TROL1", false, false, false))
// 				.thenReturn(List.of(palletEntity));

// 		// Simulate stock record exists (so mission is considered generated)
// 		when(currentStockDetailsRepository.findTopByPalletInformationIdOrderByPalletInformationIdDesc(1))
// 				.thenReturn(new CurrentStockDetailsEntity());

// 		// Act
// 		ResponseEntity<?> response = loadingStationService.submitWorkDone(null);

// 		// Assert
// 		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
// 		assertEquals("Mission is already generated", response.getBody());
// 	}

// }
