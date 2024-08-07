package sample.sm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StateMachine<TState, TContext> {

    private final List<Transition<TState, TContext>> transitions;

    private StateMachine(final List<Transition<TState, TContext>> transitions) {
        this.transitions = transitions;
    }

    public boolean testTransition(final TState fromState, final TState toState, final TContext context) {
        final boolean isAllowed = Optional
            .ofNullable(transitions)
            .orElse(List.of())
            .stream()
            .anyMatch(t -> Objects.equals(t.getFromState(), fromState) && Objects.equals(t.getToState(), toState) && t.check(context));
        return isAllowed;
    }

    public static class Builder<TState, TContext> {

        private final List<Transition<TState, TContext>> transitions;

        private Builder(final List<Transition<TState, TContext>> transitions) {
            this.transitions = transitions;
        }

        public static <TState, TContext> StateMachine<TState, TContext> withTransitions(final List<Transition<TState, TContext>> transitions) {
            return new StateMachine<>(transitions);
        }


        public static <TState, TContext> Builder<TState, TContext> typeOf(final Class<TState> stateClass, final Class<TContext> contextClass) {
            return new Builder<>(List.of());
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
                return new Builder<>(lst);
            }
        }

        public StateMachine<TState, TContext> build() {
            return new StateMachine<>(transitions);
        }
    }
}
