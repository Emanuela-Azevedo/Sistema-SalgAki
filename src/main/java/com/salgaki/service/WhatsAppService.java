package com.salgaki.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final CardapioService cardapioService;
    private final ObjectMapper objectMapper;

    @Value("${whatsapp.api-base-url}")
    private String whatsappApiBaseUrl;

    @Value("${whatsapp.phone-number-id}")
    private String whatsappPhoneNumberId;

    @Value("${whatsapp.access-token}")
    private String whatsappAccessToken;

    @Value("${whatsapp.document-filename}")
    private String documentFilename;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public void enviarCardapio(String numeroCliente) {
        try {
            byte[] pdfBytes = cardapioService.gerarPdfCardapio();
            String mediaId = uploadMedia(pdfBytes);
            sendDocumentMessage(numeroCliente, mediaId);
        } catch (Exception e) {
            log.error("Erro ao enviar cardápio via WhatsApp Cloud API", e);
            throw new RuntimeException("Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    private String uploadMedia(byte[] pdfBytes) throws Exception {
        String endpoint = String.format("%s/%s/media", whatsappApiBaseUrl, whatsappPhoneNumberId);
        String boundary = "WhatsAppBoundary" + System.currentTimeMillis();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + whatsappAccessToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(ofMimeMultipartData(pdfBytes, boundary, documentFilename))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            throw new RuntimeException("Falha no upload de mídia: " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        if (!json.has("id")) {
            throw new RuntimeException("Resposta inválida do WhatsApp Cloud API: " + response.body());
        }

        return json.get("id").asText();
    }

    private void sendDocumentMessage(String numeroCliente, String mediaId) throws Exception {
        String endpoint = String.format("%s/%s/messages", whatsappApiBaseUrl, whatsappPhoneNumberId);
        String numeroFormatado = numeroCliente.replaceAll("\\D", "");
        if (!numeroFormatado.startsWith("55")) {
            numeroFormatado = "55" + numeroFormatado;
        }

        JsonNode payload = objectMapper.createObjectNode()
                .put("messaging_product", "whatsapp")
                .put("to", numeroFormatado)
                .put("type", "document")
                .set("document", objectMapper.createObjectNode()
                        .put("id", mediaId)
                        .put("filename", documentFilename)
                        .put("caption", "Olá! Aqui está o cardápio do SalgAki.")
                );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + whatsappAccessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            throw new RuntimeException("Falha ao enviar documento via WhatsApp Cloud API: " + response.body());
        }
    }

    private static HttpRequest.BodyPublisher ofMimeMultipartData(byte[] fileBytes, String boundary, String filename) {
        List<byte[]> byteArrays = new ArrayList<>();
        String lineBreak = "\r\n";

        byteArrays.add(("--" + boundary + lineBreak).getBytes(StandardCharsets.UTF_8));
        byteArrays.add("Content-Disposition: form-data; name=\"messaging_product\"".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(lineBreak.getBytes(StandardCharsets.UTF_8));
        byteArrays.add(lineBreak.getBytes(StandardCharsets.UTF_8));
        byteArrays.add("whatsapp".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(lineBreak.getBytes(StandardCharsets.UTF_8));

        byteArrays.add(("--" + boundary + lineBreak).getBytes(StandardCharsets.UTF_8));
        byteArrays.add("Content-Disposition: form-data; name=\"type\"".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(lineBreak.getBytes(StandardCharsets.UTF_8));
        byteArrays.add(lineBreak.getBytes(StandardCharsets.UTF_8));
        byteArrays.add("application/pdf".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(lineBreak.getBytes(StandardCharsets.UTF_8));

        byteArrays.add(("--" + boundary + lineBreak).getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + lineBreak).getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Type: application/pdf" + lineBreak + lineBreak).getBytes(StandardCharsets.UTF_8));
        byteArrays.add(fileBytes);
        byteArrays.add(lineBreak.getBytes(StandardCharsets.UTF_8));

        byteArrays.add(("--" + boundary + "--" + lineBreak).getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
