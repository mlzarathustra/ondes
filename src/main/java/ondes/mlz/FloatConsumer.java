package ondes.mlz;

import java.util.Objects;

@FunctionalInterface
public interface FloatConsumer {

    void accept(float value);

    default ondes.mlz.FloatConsumer andThen(
        java.util.function.DoubleConsumer after
    ) {
            Objects.requireNonNull(after);
            return (float t) -> { accept(t);
            after.accept(t);
        };
    }
}

