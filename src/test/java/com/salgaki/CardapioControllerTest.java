package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.controller.CardapioController;
import com.salgaki.dto.CardapioWhatsAppDTO;
import com.salgaki.service.WhatsAppService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardapioController.class)
class CardapioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WhatsAppService whatsAppService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveEnviarCardapioComSucesso() throws Exception {
        // Simula comportamento do service
        Mockito.doNothing().when(whatsAppService).enviarCardapio("55999999999");

        CardapioWhatsAppDTO dto = new CardapioWhatsAppDTO();
        dto.setNumeroWhatsApp("55999999999");

        mockMvc.perform(post("/api/cardapio/enviar-whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Cardápio enviado com sucesso para 55999999999"));
    }

    @Test
    void deveRetornarErroAoEnviarCardapio() throws Exception {
        // Simula exceção no service
        Mockito.doThrow(new RuntimeException("Falha no envio"))
                .when(whatsAppService).enviarCardapio("55988887777");

        CardapioWhatsAppDTO dto = new CardapioWhatsAppDTO();
        dto.setNumeroWhatsApp("55988887777");

        mockMvc.perform(post("/api/cardapio/enviar-whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro ao enviar cardápio: Falha no envio"));
    }
}