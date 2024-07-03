package org.memetrader.WebServer;

import org.junit.experimental.results.ResultMatchers;
import org.junit.jupiter.api.Test;
import org.memetrader.common.StockNotFoundException;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class PublicStockControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Mock
    private MemeStockService stockService;


    @Test
    public void testPrice() throws Exception {
        when(stockService.getPrice(1)).thenReturn(10L);
        when(stockService.getPrice(anyInt())).thenThrow(new StockNotFoundException("Stock not found"));

        mockMvc.perform(get("/v1/public/stock/price")
                        .param("stockId", "1"));
                // .andDo(res -> System.out.println(res.getResponse().getStatus()));
                // .andExpect(jsonPath("$", is(10L)));
    }
}
