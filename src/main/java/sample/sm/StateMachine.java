package sample.sm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StateMachine<TState, TContext> {

    private final List<Transition<TState, TContext>> transitions;
    private final Boolean isDefaultAllowed;

    private StateMachine(final List<Transition<TState, TContext>> transitions, final Boolean allowDefaultTransition) {
        this.transitions = transitions;
        this.isDefaultAllowed = Optional.ofNullable(allowDefaultTransition).orElse(Boolean.FALSE);
    }

    public boolean testTransition(final TState fromState, final TState toState, final TContext context) {
        final boolean isAllowed = Optional
            .ofNullable(transitions)
            .orElse(List.of())
            .stream()
            .filter(t -> Objects.equals(t.getFromState(), fromState) && Objects.equals(t.getToState(), toState))
            .findAny()
            .map(t -> t.check(context))
            .orElse(isDefaultAllowed);
        return isAllowed;
    }

    public static class Builder<TState, TContext> {

        private final List<Transition<TState, TContext>> transitions;
        private final Boolean isDefaultAllowed;

        private Builder(final List<Transition<TState, TContext>> transitions, final Boolean allowDefaultTransition) {
            this.transitions = Optional.ofNullable(transitions).orElse(List.of());
            this.isDefaultAllowed = Optional.ofNullable(allowDefaultTransition).orElse(Boolean.FALSE);
        }

        public static <TState, TContext> Builder<TState, TContext> typeOf(final Class<TState> stateClass, final Class<TContext> contextClass) {
            return new Builder<>(List.of(), false);
        }

        public Builder<TState, TContext> withTransition(final TState from, final TState to, final Predicate<TContext> check) {
            final List<Transition<TState, TContext>> existingTransitions = Optional.ofNullable(transitions).orElse(List.of());
            if (existingTransitions.stream().anyMatch(t -> Objects.equals(t.getFromState(), from) && Objects.equals(t.getToState(), to))) {
                throw new IllegalArgumentException(String.format("transition from: %s to: %s already exists", from, to));
            } else {
                final List<Transition<TState, TContext>> lst = Stream.concat(
                        existingTransitions.stream(),
                        Stream.of(Transition.Builder.<TState, TContext>from(from).to(to).build(check)))
                    .toList();
                return new Builder<>(lst, isDefaultAllowed);
            }
        }

        public Builder<TState, TContext> defaultAllow(final Boolean allowDefaultTransition) {
            return new Builder<>(transitions, allowDefaultTransition);
        }

        public StateMachine<TState, TContext> build() {
            return new StateMachine<>(transitions, isDefaultAllowed);
        }
    }
}
