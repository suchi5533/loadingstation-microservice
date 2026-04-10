// package com.ats.lumax.controller;

// import com.ats.lumax.external.OpcUaService;
// import com.ats.lumax.external.ReadDataService;
// import com.ats.lumax.service.ILoadingStationService;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.List;

// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(LoadingStationController.class)
// class LoadingStationControllerTest {

// 	@Autowired
// 	private MockMvc mockMvc;

// 	@MockBean
// 	private ILoadingStationService loadingStationService;

// 	@MockBean
// 	private ReadDataService readDataService;

// 	@MockBean
// 	private OpcUaService opcUaService;

// 	@SuppressWarnings("unchecked")
//     @Test
//     void testFetchPalletPresent() throws Exception {
//         when(loadingStationService.fetchPalletPresent())
//             .thenReturn((ResponseEntity) ResponseEntity.ok("Pallet Found")); // raw cast

//         mockMvc.perform(get("/loadingStation/fetchPalletPresent"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("Pallet Found"));
//     }

// 	@SuppressWarnings("unchecked")
// 	@Test
// 	void testBrowseTags() throws Exception {
// 		List<String> mockTags = List.of("Tag1", "Tag2");
// 		when(readDataService.browseTags("root")).thenReturn((ResponseEntity) ResponseEntity.ok(mockTags)); // cast to
// 																											// raw
// 																											// ResponseEntity

// 		mockMvc.perform(get("/loadingStation/browse").param("startingNodeParam", "root")).andExpect(status().isOk())
// 				.andExpect(jsonPath("$[0]").value("Tag1")).andExpect(jsonPath("$[1]").value("Tag2"));
// 	}

// 	@Test
// 	void testConnect() throws Exception {
// 		doNothing().when(opcUaService).connect();

// 		mockMvc.perform(get("/loadingStation/connect")).andExpect(status().isOk());
// 	}

// 	@SuppressWarnings("unchecked")
//     @Test
//     void testReadValue() throws Exception {
//         when(readDataService.readValue("ns=2;s=Demo.Tag"))
//                 .thenReturn((ResponseEntity) ResponseEntity.ok("123"));

//         mockMvc.perform(get("/loadingStation/readValue")
//                         .param("nodeId", "ns=2;s=Demo.Tag"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("123"));
//     }

// 	@SuppressWarnings("unchecked")
// 	@Test
// 	void testSubmitWorkDone() throws Exception {
// 		String requestJson = "{\"materialCode\":\"MAT001\"}";
// 		when(loadingStationService.submitWorkDone(any())).thenReturn((ResponseEntity) ResponseEntity.ok("Work Done"));

// 		mockMvc.perform(
// 				post("/loadingStation/submitWorkDone").contentType(MediaType.APPLICATION_JSON).content(requestJson))
// 				.andExpect(status().isOk()).andExpect(content().string("Work Done"));
// 	}
// }
