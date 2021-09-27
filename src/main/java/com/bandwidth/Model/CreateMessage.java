package com.bandwidth.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMessage {

    private String to;
    private String from;
    private String text;
}
