package sample.sm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
            if (transitions.stream().anyMatch(t -> Objects.equals(t.getFromState(), from) && Objects.equals(t.getToState(), to))) {
                throw new IllegalArgumentException(String.format("transition from: %s to: %s already exists", from, to));
            } else {
                final List<Transition<TState, TContext>> lst = Stream.concat(
                        transitions.stream(),
                        Stream.of(Transition.Builder.<TState, TContext>from(from).to(to).build(check)))
                    .toList();
                return new Builder<>(lst, isDefaultAllowed);
            }
        }

        private Builder<TState, TContext> withTransitions(final List<Transition<TState, TContext>> transitions) {
            if (Optional.ofNullable(transitions).filter(lst -> lst.stream().noneMatch(this.transitions::contains)).isPresent()) {
                final List<Transition<TState, TContext>> lst = Stream.concat(
                        this.transitions.stream(),
                        transitions.stream())
                    .toList();
                return new Builder<>(lst, isDefaultAllowed);
            } else {
                throw new IllegalArgumentException("invalid transitions list, empty list or contains existing transitions.");
            }
        }

        public Builder<TState, TContext> withTransition(final Set<TState> fromStates, final TState to, final Predicate<TContext> check) {
            final List<Transition<TState, TContext>> transitions = Optional.ofNullable(fromStates).orElse(Set.of())
                .stream()
                .map(from -> Transition.Builder.<TState, TContext>from(from).to(to).build(check))
                .toList();
            final Builder<TState, TContext> builder = withTransitions(transitions);
            return builder;
        }


        public Builder<TState, TContext> withTransition(final TState from, final Set<TState> toStates, final Predicate<TContext> check) {
            final List<Transition<TState, TContext>> transitions = Optional.ofNullable(toStates).orElse(Set.of())
                .stream()
                .map(to -> Transition.Builder.<TState, TContext>from(from).to(to).build(check))
                .toList();
            final Builder<TState, TContext> builder = withTransitions(transitions);
            return builder;
        }

        public Builder<TState, TContext> defaultAllow(final Boolean allowDefaultTransition) {
            return new Builder<>(transitions, allowDefaultTransition);
        }

        public StateMachine<TState, TContext> build() {
            return new StateMachine<>(transitions, isDefaultAllowed);
        }
    }
}
