package sample.sm;


import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Transition<TState, TContext, TResult> {

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Transition<?, ?, ?> that = (Transition<?, ?, ?>) o;
        return Objects.equals(fromState, that.fromState) && Objects.equals(toState, that.toState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromState, toState);
    }

    public abstract boolean check(TContext context);

    public abstract TResult apply(final TContext context);

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


        public <TResult> Transition<TState, TContext, TResult> build(final Predicate<TContext> predicate, final Function<? super TContext, ? extends TResult> function) {
            Objects.requireNonNull(predicate, "condition is required");
            Objects.requireNonNull(function, "function is required");
            return new Transition<>(fromState, toState) {
                @Override
                public boolean check(final TContext context) {
                    return predicate.test(context);
                }

                @Override
                public TResult apply(final TContext context) {
                    return function.apply(context);
                }
            };
        }

        public Transition<TState, TContext, TContext> build(final Predicate<TContext> predicate) {
            Objects.requireNonNull(predicate, "condition is required");
            return build(predicate, Function.identity());
        }

        public <TResult> Transition<TState, TContext, TResult> build(final Function<TContext, TResult> function) {
            Objects.requireNonNull(function, "function is required");
            return build(t -> true, function);
        }
    }
}
