package com.example.chatbot_atendimento_ia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public String processarMensagem(String mensagemDoCliente) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Aqui nós programamos o comportamento da IA
        String contextoDeNegocio = "Você é um atendente virtual de um delivery especializado em pizza frita localizado em Vila Velha, Espírito Santo. " +
                "Seja simpático, rápido e use emojis. Responda à seguinte mensagem do cliente: ";

        String promptFinal = contextoDeNegocio + mensagemDoCliente;

        // Montando o JSON que o Google Gemini exige
        String requestBody = """
                {
                  "contents": [{
                    "parts":[{"text": "%s"}]
                  }]
                }
                """.formatted(promptFinal);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            // Disparando a requisição para a IA do Google
            Map<String, Object> response = restTemplate.postForObject(apiUrl + "?key=" + apiKey, request, Map.class);

            // Extraindo apenas o texto da resposta do meio daquele JSON gigante que a API devolve
            return extrairTextoDaResposta(response);

        } catch (Exception e) {
            System.err.println("Erro ao chamar o Gemini: " + e.getMessage());
            return "Poxa, nosso chef virtual está ocupado fritando umas pizzas agora. Pode tentar de novo em um minuto?";
        }
    }

    private String extrairTextoDaResposta(Map<String, Object> response) {
        try {
            var candidates = (java.util.List<Map<String, Object>>) response.get("candidates");
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (java.util.List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Desculpe, não entendi.";
        }
    }
}