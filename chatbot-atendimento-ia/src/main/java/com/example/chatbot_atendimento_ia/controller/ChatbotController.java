package com.example.chatbot_atendimento_ia.controller;

import com.example.chatbot_atendimento_ia.dto.MensagemRequest;
import com.example.chatbot_atendimento_ia.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/bot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    // Injeção de dependência via construtor (melhor prática do Spring)
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    // --- NOVA ROTA DE VALIDAÇÃO DA META (GET) ---
    // A Meta faz uma requisição GET para testar se a URL é nossa mesmo
    @GetMapping("/receber")
    public ResponseEntity<String> validarWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        // A mesma senha que vamos colocar lá no painel da Meta
        String MEU_TOKEN_SECRETO = "pizza123";

        if ("subscribe".equals(mode) && MEU_TOKEN_SECRETO.equals(token)) {
            // Se a senha bater, devolvemos o código de verificação para a Meta
            return ResponseEntity.ok(challenge);
        } else {
            // Se tentarem hackear a rota, bloqueamos com erro 403
            return ResponseEntity.status(403).body("Falha na verificação de segurança");
        }
    }

    // --- ROTA QUE RECEBE AS MENSAGENS (POST) ---
    // Essa continua intacta, é onde os JSONs do WhatsApp vão chegar de verdade
    @PostMapping("/receber")
    public ResponseEntity<String> receberMensagem(@RequestBody MensagemRequest request) {
        // 1. Recebe o JSON do WhatsApp
        // 2. Manda o texto para o Service processar
        String respostaIA = chatbotService.processarMensagem(request.remetente(), request.texto());

        // 3. Retorna a resposta
        return ResponseEntity.ok(respostaIA);
    }
}