package com.bandwidth.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MessageCallback {

    private String type;
    private String time;
    private String description;
    private String to;
    private Message message;

    @Getter
    @Setter
    public class Message {
        private String id;
        private String time;
        private List<String> to;
        private String from;
        private String text;
        private String applicationId;
        private List<String> media;
        private String owner;
        private String direction;
        private int segmentCount;
    }
}
