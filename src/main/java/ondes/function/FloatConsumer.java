package ondes.function;

import java.util.Objects;

//  Why does Java fail to define this one?

@FunctionalInterface
public interface FloatConsumer {

    void accept(float value);

    default FloatConsumer andThen(
        java.util.function.DoubleConsumer after
    ) {
            Objects.requireNonNull(after);
            return (float t) -> { accept(t);
            after.accept(t);
        };
    }
}

