package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import java.util.function.Supplier;

/**
 * Created by Amaar on 2017-08-18.
 */
public interface MessageAcceptor {
    MessageAcceptor EMPTY = message -> {};

    void accept(String message);
    default void acceptLast(String message) {
        accept(message);
    }

    static MessageAcceptor addAcceptCondition(MessageAcceptor messageAcceptor, Supplier<Boolean> condition) {
        return new MessageAcceptor() {
            @Override
            public void accept(String message) {
                if (condition.get()) messageAcceptor.accept(message);
            }

            @Override
            public void acceptLast(String message) {
                if (condition.get()) messageAcceptor.acceptLast(message);
            }
        };
    }

    static MessageAcceptor addOnFinishRunnable(MessageAcceptor messageAcceptor, Runnable runnable) {
        return new MessageAcceptor() {
            @Override
            public void accept(String message) {
                messageAcceptor.acceptLast(message);
            }

            @Override
            public void acceptLast(String message) {
                messageAcceptor.acceptLast(message);
                runnable.run();
            }
        };
    }
}
