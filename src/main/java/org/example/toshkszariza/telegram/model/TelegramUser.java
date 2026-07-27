package org.example.toshkszariza.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUser(
        long id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username
) {
}
