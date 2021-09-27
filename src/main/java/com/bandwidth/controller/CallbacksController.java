package com.bandwidth.controller;

import com.bandwidth.Model.MessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    @RequestMapping("/outbound/messaging/status")
    public void statusCallback(@RequestBody MessageCallback[] callbacks) {

        for (MessageCallback callback : callbacks) {
            logger.info(callback.getType());
            logger.info(callback.getDescription());
            switch (callback.getType()) {
                case "message-sending":
                    logger.info("message-sending type is only for MMS");
                    break;
                case "message-delivered":
                    logger.info("your message has been handed off to the Bandwidth's MMSC network, but has not been confirmed at the downstream carrier");
                    break;
                case "message-failed":
                    logger.info("For MMS and Group Messages, you will only receive this callback if you have enabled delivery receipts on MMS. ");
                    break;
                default:
                    logger.info("Message type does not match endpoint. This endpoint is used for message status callbacks only.");
                    break;
            }
        }
    }

    @RequestMapping("/inbound/messaging")
    public void inboundCallback(@RequestBody MessageCallback[] callbacks) {

        for (MessageCallback callback : callbacks) {
            logger.info(callback.getType());
            logger.info(callback.getDescription());
            switch (callback.getType()) {
                case "message-received":
                    logger.info("from: " + callback.getMessage().getFrom() + "-->" + callback.getMessage().getTo().get(0));
                    logger.info(callback.getMessage().getText());
                    break;
                default:
                    logger.info(("Message type does not match endpoint. This endpoint is used for inbound messages only.\nOutbound message callbacks should be sent to /callbacks/outbound/messaging."));
                    break;
            }
        }
    }
}
