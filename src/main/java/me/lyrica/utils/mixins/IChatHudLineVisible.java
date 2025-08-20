package me.lyrica.utils.mixins;

public interface IChatHudLineVisible {
    boolean lyrica$isClientMessage();

    void lyrica$setClientMessage(boolean clientMessage);

    String lyrica$getClientIdentifier();

    void lyrica$setClientIdentifier(String clientIdentifier);
}