package com.bandwidth.controller;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.api.MessagesApi;
import com.bandwidth.sdk.model.Message;
import com.bandwidth.sdk.model.MessageRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("messages")
public class MessagesController {

    Logger logger = LoggerFactory.getLogger(MessagesController.class);

    private final String accountId = System.getenv("BW_ACCOUNT_ID");
    private final String clientId = System.getenv("BW_CLIENT_ID");
    private final String clientSecret = System.getenv("BW_CLIENT_SECRET");
    private final String applicationId = System.getenv("BW_MESSAGING_APPLICATION_ID");
    private final String number = System.getenv("BW_NUMBER");

    private final MessagesApi messagesApi = new MessagesApi(new ApiClient(clientId, clientSecret, null));

    private static boolean isString(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    @PostMapping()
    public ResponseEntity<Void> sendMessage(@RequestBody String body) {
        JsonObject request;
        try {
            request = JsonParser.parseString(body).getAsJsonObject();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }

        JsonElement media = request.has("media") ? request.get("media") : new JsonArray();
        if (!isString(request.get("to")) || !isString(request.get("text")) || !media.isJsonArray()) {
            return ResponseEntity.badRequest().build();
        }

        List<URI> mediaUrls = new ArrayList<>();
        for (JsonElement url : media.getAsJsonArray()) {
            if (!isString(url)) {
                return ResponseEntity.badRequest().build();
            }
            mediaUrls.add(URI.create(url.getAsString()));
        }

        MessageRequest messageRequest = new MessageRequest()
                .applicationId(applicationId)
                .addToItem(request.get("to").getAsString())
                .from(number)
                .text(request.get("text").getAsString())
                .media(mediaUrls.isEmpty() ? null : mediaUrls);

        try {
            Message message = messagesApi.createMessage(accountId, messageRequest);
            logger.info("Sent message " + message.getId());
        } catch (Exception e) {
            logger.error("Failed to send message: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.noContent().build();
    }
}
