package sample.sm;


import java.util.Objects;
import java.util.function.Predicate;

public abstract class Transition<TState, TContext> {

    private final TState fromState;
    private final TState toState;

    private Transition(TState fromState, TState toState) {
        this.fromState = fromState;
        this.toState = toState;
    }

    public TState getFromState() {
        return fromState;
    }

    public TState getToState() {
        return toState;
    }

    public abstract boolean check(TContext context);

    public static class Builder<TState, TContext> {

        private final TState fromState;
        private final TState toState;

        private Builder(final TState fromState, final TState toState) {
            this.fromState = fromState;
            this.toState = toState;
        }

        public static <TState, TContext> Builder<TState, TContext> from(final TState fromState) {
            return new Builder<>(fromState, null);
        }

        public Builder<TState, TContext> to(final TState toState) {
            return new Builder<>(fromState, toState);
        }


        public Transition<TState, TContext> build(Predicate<TContext> condition) {
            Objects.requireNonNull(condition);
            return new Transition<>(fromState, toState) {
                @Override
                public boolean check(final TContext context) {
                    return condition.test(context);
                }
            };
        }
    }
}
