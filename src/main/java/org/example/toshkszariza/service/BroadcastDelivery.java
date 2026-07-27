package org.example.toshkszariza.service;

import java.util.List;

public record BroadcastDelivery(String message, List<Long> recipientChatIds) {
}
