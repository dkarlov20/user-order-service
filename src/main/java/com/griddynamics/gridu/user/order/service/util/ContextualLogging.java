package com.griddynamics.gridu.user.order.service.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.function.Consumer;

@UtilityClass
public class ContextualLogging {
    public static final String REQUEST_ID_KEY = "CONTEXT_KEY";

    private static final String MDC_KEY = "MDC_KEY";

    public static <T> Consumer<Signal<T>> logOnEach(Consumer<T> logStatement) {
        return signal -> {
            Optional<String> requestId = signal.getContextView().getOrEmpty(REQUEST_ID_KEY);
            requestId.ifPresentOrElse(id -> {
                        try (MDC.MDCCloseable cMdc = MDC.putCloseable(MDC_KEY, id)) {
                            logStatement.accept(signal.get());
                        }
                    },
                    () -> logStatement.accept(signal.get()));
        };
    }

    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            if (!signal.isOnNext()) {
                return;
            }
            Optional<String> requestId = signal.getContextView().getOrEmpty(REQUEST_ID_KEY);
            requestId.ifPresentOrElse(id -> {
                        try (MDC.MDCCloseable cMdc = MDC.putCloseable(MDC_KEY, id)) {
                            logStatement.accept(signal.get());
                        }
                    },
                    () -> logStatement.accept(signal.get()));
        };
    }

    public static Consumer<Signal<?>> logOnError(Consumer<Throwable> errorLogStatement) {
        return signal -> {
            if (!signal.isOnError()) {
                return;
            }
            Optional<String> requestId = signal.getContextView().getOrEmpty(REQUEST_ID_KEY);

            requestId.ifPresentOrElse(id -> {
                        try (MDC.MDCCloseable cMdc = MDC.putCloseable(MDC_KEY, id)) {
                            errorLogStatement.accept(signal.getThrowable());
                        }
                    },
                    () -> errorLogStatement.accept(signal.getThrowable()));
        };
    }
}
