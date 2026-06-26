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

    @PostMapping("/receber")
    public ResponseEntity<String> receberMensagem(@RequestBody MensagemRequest request) {
        // 1. Recebe o JSON do WhatsApp/Telegram
        // 2. Manda o texto para o Service processar
        String respostaIA = chatbotService.processarMensagem(request.remetente(), request.texto());

        // 3. Retorna a resposta (depois faremos o envio ativo de volta pro usuário)
        return ResponseEntity.ok(respostaIA);
    }
}
