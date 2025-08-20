package me.lyrica.events.impl;

import lombok.*;
import me.lyrica.events.Event;

@Getter @Setter @AllArgsConstructor
public class ChatInputEvent extends Event {
    private String message;
}
