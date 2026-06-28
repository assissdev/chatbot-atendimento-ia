package com.example.chatbot_atendimento_ia.dto;

import java.util.List;

public record MetaWebhookRequest(
        String object,
        List<WhatsAppEntry> entry
) {
    // Ao colocar os sub-records AQUI DENTRO, o Java permite que sejam públicos no mesmo arquivo!
    public record WhatsAppEntry(String id, List<WhatsAppChange> changes) {}

    public record WhatsAppChange(WhatsAppValue value, String field) {}

    public record WhatsAppValue(
            String messaging_product,
            WhatsAppMetadata metadata,
            List<WhatsAppContact> contacts,
            List<WhatsAppMessage> messages
    ) {}

    public record WhatsAppMetadata(String display_phone_number, String phone_number_id) {}

    public record WhatsAppContact(WhatsAppProfile profile, String wa_id) {}

    public record WhatsAppProfile(String name) {}

    public record WhatsAppMessage(
            String from,
            String id,
            String timestamp,
            WhatsAppText text,
            String type
    ) {}

    public record WhatsAppText(String body) {}
}