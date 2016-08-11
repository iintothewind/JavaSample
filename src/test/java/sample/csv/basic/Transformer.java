package sample.csv.basic;


import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public abstract class Transformer<A, B extends Comparable> {
    private Function<A, B> forward;

    private Transformer(Function<A, B> function) {
        this.forward = function;
    }

    public static <A, B extends Comparable> Transformer<A, B> with(Function<A, B> function) {
        return new Transformer<A, B>(function) {
        };
    }

    public B transform(A record) {
        return forward.apply(record);
    }

    public FluentIterable<B> transform(Iterable<A> records) {
        return FluentIterable.from(records).transform(forward);
    }
}