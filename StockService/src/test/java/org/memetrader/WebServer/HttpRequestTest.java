package org.memetrader.WebServer;

import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpRequestTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void authenticatedReturnsFalse() throws Exception {
        var response = testRestTemplate.getForEntity("http://localhost:" + port + "/v1/user", UserMetadataV1.class);
        assert response.getStatusCode() == HttpStatusCode.valueOf(401);
    }

//    @Test
//    public void testGetStockHistory() throws Exception {
//        when(stockService.getHistory(anyInt(), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(List.of());
//        mockMvc.perform(MockMvcRequestBuilders.get("/v1/stock/history")
//                        .param("stockId", "1")
//                        .param("startDate", "2020-01-01T00:00:00Z")
//                        .param("endDate", "2020-01-02T00:00:00Z"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
//    }
//
//    @Test
//    public void testStockMetadata() throws Exception {
////        when(stockService.getMetadata(anyInt())).thenReturn(new StockMetadataV1());
////        mockMvc.perform(MockMvcRequestBuilders.get("/v1/stock/metadata")
////                        .param("stockId", "1"))
////                .andExpect(MockMvcResultMatchers.status().isOk())
////                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
//    }
//
//    @Test
//    public void testPlaceOrder() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.post("/v1/order")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"userId\":1,\"stockId\":1,\"order\":\"BUY\",\"numShares\":10,\"pricePerShare\":10.0}"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
//    }
//
//    @Test
//    public void testGetFrontpageStocks() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/v1/frontpagestocks"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
//    }
//
//    @Test
//    public void testSearchStocks() throws Exception {
//        when(stockService.searchStocks(any(String.class), anyInt())).thenReturn(List.of());
//        mockMvc.perform(MockMvcRequestBuilders.get("/v1/searchStocks")
//                        .param("searchString", "test"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
//    }
}