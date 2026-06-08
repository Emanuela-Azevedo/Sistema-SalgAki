package com.salgaki.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final CardapioService cardapioService;
    private final WebClient webClient;

    @Value("${whatsapp.api-base-url}")
    private String apiBaseUrl;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.document-filename}")
    private String documentFilename;

    // ✅ Método usado no sandbox (texto simples)
    public String enviarMensagemTexto(String numeroCliente) {
        String url = apiBaseUrl + "/" + phoneNumberId + "/messages";

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", numeroCliente);
        payload.put("type", "text");

        ObjectNode text = payload.putObject("text");
        text.put("body", "Seu cardápio está disponível em PDF. Peça já!");

        return webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // ✅ Mantém métodos de mídia para quando sair do sandbox
    private String sendDocumentMessageComRetorno(String numeroCliente, String mediaId) {
        String url = apiBaseUrl + "/" + phoneNumberId + "/messages";

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", numeroCliente);
        payload.put("type", "document");

        ObjectNode document = payload.putObject("document");
        document.put("id", mediaId);
        document.put("filename", documentFilename);

        return webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String uploadMedia(byte[] pdfBytes) {
        String url = apiBaseUrl + "/" + phoneNumberId + "/media";

        var response = webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", new ByteArrayResource(pdfBytes))
                        .with("type", "application/pdf"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return response.get("id").asText();
    }
}