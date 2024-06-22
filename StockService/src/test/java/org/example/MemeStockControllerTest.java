package org.example;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Import(TestConfig.class)
@ExtendWith(SpringExtension.class)
@WebMvcTest(PublicStockController.class)
public class MemeStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageUploadService imageUploadService;

    @MockBean
    private ChatGPTDescriptionGenerator chatGPTDescriptionGenerator;

    @MockBean
    private MemeStockService stockService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder defaultMockMvcBuilder = MockMvcBuilders.webAppContextSetup(webApplicationContext);
        var urlConfigSource = new UrlBasedCorsConfigurationSource();
        urlConfigSource.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        defaultMockMvcBuilder.addFilters(new CorsFilter(urlConfigSource));
        mockMvc = defaultMockMvcBuilder
                .build();
    }
    @Test
    public void testPrice() throws Exception {
        when(stockService.getPrice(anyInt())).thenReturn(10);
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/stock/price")
                        .param("stockId", "1"))
       .andExpect(jsonPath("$", is(10)));
    }

    @Test
    public void testGetStockHistory() throws Exception {
        when(stockService.getHistory(anyInt(), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(List.of());
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/stock/history")
                        .param("stockId", "1")
                        .param("startDate", "2020-01-01T00:00:00Z")
                        .param("endDate", "2020-01-02T00:00:00Z"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    public void testStockMetadata() throws Exception {
//        when(stockService.getMetadata(anyInt())).thenReturn(new StockMetadataV1());
//        mockMvc.perform(MockMvcRequestBuilders.get("/v1/stock/metadata")
//                        .param("stockId", "1"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    public void testPlaceOrder() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"stockId\":1,\"order\":\"BUY\",\"numShares\":10,\"pricePerShare\":10.0}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    public void testGetFrontpageStocks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/frontpagestocks"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    public void testSearchStocks() throws Exception {
        when(stockService.searchStocks(any(String.class), anyInt())).thenReturn(List.of());
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/searchStocks")
                        .param("searchString", "test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    public void testHandleImageUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());
        when(imageUploadService.saveImage(any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/stock/new")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "*"));
    }
}