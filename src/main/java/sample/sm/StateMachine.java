package sample.sm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StateMachine<TState, TContext, TResult> {

    private final List<Transition<TState, TContext, TResult>> transitions;

    private StateMachine(final List<Transition<TState, TContext, TResult>> transitions) {
        this.transitions = transitions;
    }

    /**
     * check if the state transition is allowed based on input context
     *
     * @param context input context
     * @return true if state transition is allowed based on input context, false if state transition is allowed based on input context
     */
    public boolean test(final TState fromState, final TState toState, final TContext context) {
        final boolean isAllowed = Optional
            .ofNullable(transitions)
            .orElse(List.of())
            .stream()
            .filter(t -> Objects.equals(t.getFromState(), fromState) && Objects.equals(t.getToState(), toState))
            .findAny()
            .map(t -> t.check(context))
            .orElse(Boolean.FALSE);
        return isAllowed;
    }

    /**
     * make state transition if input context has satisfied the predicate check, and convert context to result type according to the matching transition. <br> If the state transition is not allowed, then return null as result.
     *
     * @param context input context
     * @return converted result by given function of matched transition if transition is allowed, or null if transition is not allowed
     */
    public TResult change(final TState fromState, final TState toState, final TContext context) {
        final TResult result = Optional.ofNullable(transitions).orElse(List.of())
            .stream()
            .filter(t -> Objects.equals(t.getFromState(), fromState) && Objects.equals(t.getToState(), toState))
            .findFirst()
            .filter(t -> t.check(context))
            .map(t -> t.apply(context))
            .orElse(null);
        return result;
    }

    public static class Builder<TState, TContext, TResult> {

        private final List<Transition<TState, TContext, TResult>> transitions;

        private Builder(final List<Transition<TState, TContext, TResult>> transitions) {
            this.transitions = Optional.ofNullable(transitions).orElse(List.of());
        }

        public static <TState, TContext, TResult> Builder<TState, TContext, TResult> typeOf(final Class<TState> stateClass, final Class<TContext> contextClass, final Class<TResult> resultClass) {
            Objects.requireNonNull(stateClass, "state class is required");
            Objects.requireNonNull(contextClass, "context class is required");
            Objects.requireNonNull(resultClass, "result class is required");
            return new Builder<>(List.of());
        }

        public Builder<TState, TContext, TResult> withTransition(final TState from, final TState to, final Predicate<TContext> predicate, final Function<TContext, TResult> function) {
            Objects.requireNonNull(predicate, "predicate is required");
            Objects.requireNonNull(function, "function is required");
            if (transitions.stream().anyMatch(t -> Objects.equals(t.getFromState(), from) && Objects.equals(t.getToState(), to))) {
                throw new IllegalArgumentException(String.format("transition from: %s to: %s already exists", from, to));
            } else {
                final List<Transition<TState, TContext, TResult>> lst = Stream.concat(
                        transitions.stream(),
                        Stream.of(Transition.Builder.<TState, TContext>from(from).to(to).build(predicate, function)))
                    .toList();
                return new Builder<>(lst);
            }
        }

        /**
         * better keep this private, to make predicate as required parameter, to mandatory check if context is in from state
         */
        private Builder<TState, TContext, TResult> withTransition(final TState from, final TState to, final Function<TContext, TResult> function) {
            Objects.requireNonNull(function, "function is required");
            return withTransition(from, to, c -> true, function);
        }

        private Builder<TState, TContext, TResult> withTransitions(final List<Transition<TState, TContext, TResult>> transitions) {
            if (Optional.ofNullable(transitions).filter(lst -> lst.stream().noneMatch(this.transitions::contains)).isPresent()) {
                final List<Transition<TState, TContext, TResult>> lst = Stream.concat(
                        this.transitions.stream(),
                        transitions.stream())
                    .toList();
                return new Builder<>(lst);
            } else {
                throw new IllegalArgumentException("invalid transitions list, empty list or contains existing transitions.");
            }
        }

        public Builder<TState, TContext, TResult> withTransitions(final Set<TState> fromStates, final TState to, final Predicate<TContext> predicate, final Function<TContext, TResult> function) {
            Objects.requireNonNull(predicate, "predicate is required");
            Objects.requireNonNull(function, "function is required");
            final List<Transition<TState, TContext, TResult>> transitions = Optional.ofNullable(fromStates).orElse(Set.of())
                .stream()
                .map(from -> Transition.Builder.<TState, TContext>from(from).to(to).build(predicate, function))
                .toList();
            final Builder<TState, TContext, TResult> builder = withTransitions(transitions);
            return builder;
        }

        /**
         * better keep this private, to make predicate as required parameter, to mandatory check if context is in from state
         */
        private Builder<TState, TContext, TResult> withTransitions(final Set<TState> fromStates, final TState to, final Function<TContext, TResult> function) {
            Objects.requireNonNull(function, "function is required");
            return withTransitions(fromStates, to, c -> true, function);
        }

        /**
         * better keep this private, because there is no scenario that transitions from same state to multiple states that will use same function
         */
        private Builder<TState, TContext, TResult> withTransitions(final TState from, final Set<TState> toStates, final Predicate<TContext> predicate, final Function<TContext, TResult> function) {
            Objects.requireNonNull(predicate, "predicate is required");
            Objects.requireNonNull(function, "function is required");
            final List<Transition<TState, TContext, TResult>> transitions = Optional.ofNullable(toStates).orElse(Set.of())
                .stream()
                .map(to -> Transition.Builder.<TState, TContext>from(from).to(to).build(predicate, function))
                .toList();
            final Builder<TState, TContext, TResult> builder = withTransitions(transitions);
            return builder;
        }

        /**
         * better keep this private, to make predicate as required parameter, to mandatory check if context is in from state
         */
        private Builder<TState, TContext, TResult> withTransitions(final TState from, final Set<TState> toStates, final Function<TContext, TResult> function) {
            Objects.requireNonNull(function, "function is required");
            return withTransitions(from, toStates, c -> true, function);
        }

        public StateMachine<TState, TContext, TResult> build() {
            return new StateMachine<>(transitions);
        }
    }
}
