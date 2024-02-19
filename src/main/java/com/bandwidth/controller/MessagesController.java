package com.bandwidth.controller;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.ApiResponse;
import com.bandwidth.sdk.ApiException;
import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.auth.HttpBasicAuth;
import com.bandwidth.sdk.Configuration;
import com.bandwidth.sdk.model.*;
import com.bandwidth.sdk.api.MessagesApi;
import com.bandwidth.model.CreateMessage;
import com.bandwidth.model.MessageReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping("callbacks/outbound/messaging")
public class MessagesController {

    Logger logger = LoggerFactory.getLogger(MessagesController.class);

    private String username = System.getenv("BW_USERNAME");
    private String password = System.getenv("BW_PASSWORD");
    private String accountId = System.getenv("BW_ACCOUNT_ID");
    private String bwNumber = System.getenv("BW_NUMBER");
    private String applicationId = System.getenv("BW_MESSAGING_APPLICATION_ID");

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    HttpBasicAuth Basic = (HttpBasicAuth) defaultClient.getAuthentication("Basic");
    private final MessagesApi api = new MessagesApi(defaultClient);
    public MessageRequest messageRequest = new MessageRequest();

    @PostMapping()
    public MessageReply createMessage(@RequestBody CreateMessage createMessage) throws IOException {


        // Build the body of the message request to the Bandwidth API
        Basic.setUsername(username);
        Basic.setPassword(password);
        messageRequest.applicationId(applicationId);
        messageRequest.addToItem(createMessage.getTo());
        messageRequest.from(createMessage.getFrom());
        messageRequest.text(createMessage.getText());
        messageRequest.priority(PriorityEnum.DEFAULT);

        MessageReply messageReply = new MessageReply();
        try {
            Message response = api.createMessage(accountId, messageRequest);
            messageReply.setSuccess(true);
        } catch (ApiException e) { // Bandwidth API response status not 2XX
            messageReply.setSuccess(false);
            messageReply.setError(e.getMessage());
        }

        return messageReply;
    }
}
