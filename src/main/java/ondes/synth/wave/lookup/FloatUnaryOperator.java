package ondes.synth.wave.lookup;

import java.util.Objects;

@FunctionalInterface
public interface FloatUnaryOperator {

    float applyAsFloat(float operand);

    default FloatUnaryOperator compose(FloatUnaryOperator before) {
        Objects.requireNonNull(before);
        return (float v) -> applyAsFloat(before.applyAsFloat(v));
    }
    default FloatUnaryOperator andThen(FloatUnaryOperator after) {
        Objects.requireNonNull(after);
        return (float t) -> after.applyAsFloat(applyAsFloat(t));
    }
    static FloatUnaryOperator identity() {
        return t -> t;
    }
}
