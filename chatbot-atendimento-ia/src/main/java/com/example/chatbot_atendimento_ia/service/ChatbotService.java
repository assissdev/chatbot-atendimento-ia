package com.example.chatbot_atendimento_ia.service;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    public String processarMensagem(String mensagemDoCliente) {
        // Aqui amanhã entrará a chamada HTTP para a API da IA.
        // Por enquanto, vamos simular que a IA analisou e retornou o texto abaixo:

        System.out.println("Mensagem recebida para processamento: " + mensagemDoCliente);

        return "Olá! Bem-vindo ao nosso delivery de pizza frita. Recebemos sua mensagem e nossa IA já vai te atender. O que vai pedir hoje?";
    }
}
