package com.bandwidth.controller;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.JSON;
import com.bandwidth.sdk.api.MediaApi;
import com.bandwidth.sdk.model.InboundCallback;
import com.bandwidth.sdk.model.InboundCallbackMessage;
import com.bandwidth.sdk.model.InboundCallbackTypeEnum;
import com.bandwidth.sdk.model.StatusCallback;
import com.bandwidth.sdk.model.StatusCallbackTypeEnum;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    private final String accountId = System.getenv("BW_ACCOUNT_ID");
    private final String clientId = System.getenv("BW_CLIENT_ID");
    private final String clientSecret = System.getenv("BW_CLIENT_SECRET");

    private final MediaApi mediaApi = new MediaApi(new ApiClient(clientId, clientSecret, null));

    private static final Map<StatusCallbackTypeEnum, String> STATUS_MESSAGES = Map.of(
            StatusCallbackTypeEnum.MESSAGE_SENDING, "message-sending type is only for MMS.",
            StatusCallbackTypeEnum.MESSAGE_DELIVERED, "Your message has been handed off to the Bandwidth's MMSC network, but has not been confirmed at the downstream carrier.",
            StatusCallbackTypeEnum.MESSAGE_FAILED, "For MMS and Group Messages, you will only receive this callback if you have enabled delivery receipts on MMS."
    );

    // The SDK models are Gson-based, so callbacks are parsed with the SDK's Gson instead of Spring's Jackson
    private static <T> T[] parseCallbacks(String body, Class<T[]> type) {
        try {
            return JSON.getGson().fromJson(body, type);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void saveMedia(URI mediaUrl) {
        String url = mediaUrl.toString();
        String mediaId = url.substring(url.indexOf("media/") + "media/".length());
        String mediaName = Paths.get(mediaId).getFileName().toString();
        if (mediaName.endsWith(".xml")) {
            return;
        }

        try {
            File media = mediaApi.getMedia(accountId, mediaId);
            Path mediaDir = Files.createDirectories(Paths.get("media"));
            Files.move(media.toPath(), mediaDir.resolve(mediaName), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved media to media/" + mediaName);
        } catch (Exception e) {
            logger.error("Failed to download " + url + ": " + e.getMessage());
        }
    }

    @PostMapping("/outbound/messaging/status")
    public ResponseEntity<Void> statusCallback(@RequestBody String body) {
        StatusCallback[] callbacks = parseCallbacks(body, StatusCallback[].class);
        if (callbacks == null || !Arrays.stream(callbacks).allMatch(callback -> STATUS_MESSAGES.containsKey(callback.getType()))) {
            return ResponseEntity.badRequest().build();
        }

        for (StatusCallback callback : callbacks) {
            logger.info(STATUS_MESSAGES.get(callback.getType()));
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/inbound/messaging")
    public ResponseEntity<Void> inboundCallback(@RequestBody String body) {
        InboundCallback[] callbacks = parseCallbacks(body, InboundCallback[].class);
        if (callbacks == null || !Arrays.stream(callbacks).allMatch(callback -> callback.getType() == InboundCallbackTypeEnum.MESSAGE_RECEIVED)) {
            return ResponseEntity.badRequest().build();
        }

        for (InboundCallback callback : callbacks) {
            InboundCallbackMessage message = callback.getMessage();
            logger.info(callback.getDescription() + "\nTo: " + message.getTo().iterator().next()
                    + "\nFrom: " + message.getFrom() + "\nText: " + message.getText());
            if (message.getMedia() != null) {
                message.getMedia().forEach(this::saveMedia);
            }
        }
        return ResponseEntity.noContent().build();
    }
}
