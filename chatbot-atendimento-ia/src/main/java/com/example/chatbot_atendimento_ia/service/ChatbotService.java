package com.example.chatbot_atendimento_ia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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

        // Aqui nós programamos o comportamento e as regras da IA (Prompt Engineering)
        String contextoDeNegocio = """
                Você é o atendente de elite de um delivery de Pizza Frita em Vila Velha, ES.
                Seu objetivo é conduzir o cliente até o fechamento do pedido de forma 100% automatizada.
                
                FLUXO DE ATENDIMENTO (Você deve seguir exatamente nesta ordem, avançando um passo de cada vez):
                PASSO 1 - SAUDAÇÃO E CARDÁPIO: Se o cliente apenas der oi, cumprimente de forma animada, envie o nosso cardápio completo e pergunte o que ele vai querer.
                PASSO 2 - ANOTAÇÃO E UPSELL: Anote o que o cliente pediu. Se ele pediu só pizza, ofereça um refrigerante educadamente. Se pediu só bebida, pergunte qual pizza vai acompanhar.
                PASSO 3 - RESUMO DO PEDIDO: Assim que o cliente disser que não quer mais nada, envie o resumo completo: itens escolhidos, a taxa de entrega (R$ 5,00) e o VALOR TOTAL da compra. Pergunte: "Posso confirmar o pedido?".
                PASSO 4 - ENTREGA E PAGAMENTO: Somente após a confirmação do Passo 3, pergunte o endereço completo para entrega e se o pagamento será no Pix, Cartão ou Dinheiro.
                PASSO 5 - DESPEDIDA: Quando o cliente passar o endereço e o pagamento, agradeça, diga que o pedido foi para a cozinha e informe o tempo médio de 30 a 40 minutos.
                
                CARDÁPIO OFICIAL:
                - Pizza Frita Calabresa c/ Queijo: R$ 25,00
                - Pizza Frita Marguerita: R$ 23,00
                - Pizza Frita Frango c/ Catupiry: R$ 28,00
                - Refrigerante Lata: R$ 6,00
                
                REGRAS DE SEGURANÇA:
                - NUNCA avance um passo sem o cliente responder o anterior.
                - NUNCA invente itens ou promoções. Venda apenas o que está no cardápio.
                - Seja sempre curto e objetivo. Mensagens de WhatsApp não podem ser gigantes.
                
                Mensagem do cliente:\s
                """;

        String promptFinal = contextoDeNegocio + mensagemDoCliente;

        // O SEGREDO: Usando Map e List para o próprio Spring Boot montar o JSON de forma 100% segura
        Map<String, Object> part = Map.of("text", promptFinal);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBodyMap = Map.of("contents", List.of(content));

        // Enviando o Map seguro em vez da String manual
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBodyMap, headers);

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
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Desculpe, não entendi.";
        }
    }
}