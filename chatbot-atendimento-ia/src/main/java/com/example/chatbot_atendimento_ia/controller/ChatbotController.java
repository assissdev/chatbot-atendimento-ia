package com.example.chatbot_atendimento_ia.controller;

import com.example.chatbot_atendimento_ia.dto.MetaWebhookRequest;
import com.example.chatbot_atendimento_ia.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/bot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    // Injeção de dependência via construtor (Melhor prática do ecossistema Spring)
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    // --- ROTA DE VALIDAÇÃO DA META (GET) ---
    // Atende o aperto de mão de segurança exigido pela API da Meta
    @GetMapping("/receber")
    public ResponseEntity<String> validarWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        String meuTokenSecreto = "pizza123";

        if ("subscribe".equals(mode) && meuTokenSecreto.equals(token)) {
            // Se o token bater, o Spring devolve o challenge com status 200 OK
            return ResponseEntity.ok(challenge);
        } else {
            // Bloqueio de segurança caso tentem acessar a URL diretamente
            return ResponseEntity.status(403).body("Falha na verificação de segurança");
        }
    }

    // --- ROTA QUE RECEBE AS MENSAGENS REAL-TIME (POST) ---
    // Agora processando o objeto complexo e aninhado vindo oficialmente do WhatsApp
    @PostMapping("/receber")
    public ResponseEntity<String> receberMensagem(@RequestBody MetaWebhookRequest request) {
        try {
            // Navegação segura pelas camadas do JSON da Meta (Entries -> Changes -> Value -> Messages)
            if (request.entry() != null && !request.entry().isEmpty()) {
                var entry = request.entry().get(0);

                if (entry.changes() != null && !entry.changes().isEmpty()) {
                    var change = entry.changes().get(0);
                    var value = change.value();

                    if (value.messages() != null && !value.messages().isEmpty()) {
                        var messageObj = value.messages().get(0);

                        // Extração limpa dos dados de negócio
                        String remetente = messageObj.from(); // Número de telefone do cliente
                        String textoMensagem = (messageObj.text() != null) ? messageObj.text().body() : "";

                        // Delegação do processamento para a camada de serviço
                        String respostaIA = chatbotService.processarMensagem(remetente, textoMensagem);

                        // Retorna 200 OK imediatamente para a Meta não reenviar a mesma mensagem
                        return ResponseEntity.ok(respostaIA);
                    }
                }
            }

            return ResponseEntity.ok("Evento recebido com sucesso, mas não continha mensagens de texto tratáveis.");

        } catch (Exception e) {
            // Tratamento preventivo e log estruturado
            return ResponseEntity.status(500).body("Erro interno ao processar o payload da Meta.");
        }
    }
}