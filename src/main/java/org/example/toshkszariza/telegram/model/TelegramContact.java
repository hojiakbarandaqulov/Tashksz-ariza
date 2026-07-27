package org.example.toshkszariza.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramContact(
        @JsonProperty("phone_number") String phoneNumber,
        @JsonProperty("user_id") Long userId
) {
}
