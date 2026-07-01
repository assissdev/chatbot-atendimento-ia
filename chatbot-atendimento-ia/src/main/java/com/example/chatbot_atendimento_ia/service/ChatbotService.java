package com.example.chatbot_atendimento_ia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {

    @Value("${meta.api.token}")
    private String metaApiToken;

    @Value("${meta.phone.id}")
    private String metaPhoneId;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // RestTemplate é o cliente HTTP padrão do Spring para fazer requisições
    private final RestTemplate restTemplate = new RestTemplate();

    public String processarMensagem(String remetente, String textoUsuario) {
        try {
            // 1. Pede para a IA gerar a resposta com base no que o cliente digitou
            String respostaIA = consultarGemini(textoUsuario);

            // 2. Envia a resposta gerada de volta para o WhatsApp do cliente
            enviarParaWhatsApp(remetente, respostaIA);

            return "Mensagem processada com sucesso";
        } catch (Exception e) {
            System.err.println("Erro no fluxo do Chatbot: " + e.getMessage());
            return "Erro ao processar";
        }
    }

    private String consultarGemini(String textoUsuario) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        // Aqui nós "programamos" o comportamento da IA.
        // Damos o contexto do negócio para que ele não responda como um robô genérico.
        String jsonRequest = """
            {
              "systemInstruction": {
                "parts": [
                  {
                    "text": "Você é o atendente virtual de um delivery especializado em pizza frita localizado em Vila Velha, Espírito Santo. Seja sempre amigável, ágil e use emojis. Responda de forma curta e direta. Não invente sabores que não existem no cardápio."
                  }
                ]
              },
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ]
            }
            """.formatted(textoUsuario.replace("\"", "\\\""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return extrairTextoDaRespostaGemini(response.getBody());
        } catch (Exception e) {
            System.err.println("Erro ao chamar Gemini: " + e.getMessage());
            return "Poxa, tivemos um probleminha interno aqui! Já volto a te atender.";
        }
    }

    private void enviarParaWhatsApp(String numeroDestino, String textoMensagem) {
        String url = "https://graph.facebook.com/v25.0/" + metaPhoneId + "/messages";

        // JSON oficial da Meta para responder com texto simples
        String jsonRequest = """
            {
                "messaging_product": "whatsapp",
                "recipient_type": "individual",
                "to": "%s",
                "type": "text",
                "text": {
                    "preview_url": false,
                    "body": "%s"
                }
            }
            """.formatted(numeroDestino, textoMensagem.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(metaApiToken);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("✅ Mensagem enviada com sucesso para " + numeroDestino);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar WhatsApp: " + e.getMessage());
        }
    }

    // Método auxiliar simples para "pescar" o texto dentro do JSON complexo de resposta do Gemini
    private String extrairTextoDaRespostaGemini(String jsonResponse) {
        if (jsonResponse == null || !jsonResponse.contains("\"text\":")) {
            return "Não consegui formular uma resposta.";
        }
        int startIndex = jsonResponse.indexOf("\"text\":") + 8;
        int endIndex = jsonResponse.indexOf("\"", startIndex);

        // Tratamento rápido para evitar quebras de linha quebrando o JSON da Meta
        return jsonResponse.substring(startIndex, endIndex).replace("\\n", "\n");
    }
}