package sample.lambda.basic;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Lst<T> extends Iterable<T> {
  T head();

  Lst<T> tail();

  boolean isEmpty();

  @SuppressWarnings("unchecked")
  static <A> Lst<A> empty() {
    return (Lst<A>) Nil.instance;
  }

  @SafeVarargs
  static <A> Lst<A> of(A... xs) {
    Objects.requireNonNull(xs);
    Lst<A> result = empty();
    for (int i = xs.length - 1; i >= 0; i--) {
      result = result.cons(xs[i]);
    }
    return result;
  }

  default Lst<T> cons(T x) {
    Objects.requireNonNull(x, "x should not be null");
    return new Cons<T>(x, this);
  }

  default Lst<T> prepend(Lst<T> xs) {
    Objects.requireNonNull(xs, "xs should not be null");
    return xs.foldRight(this, Cons::new);
  }

  default Lst<T> reverse() {
    return foldLeft(empty(), Lst::cons);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  default Iterator<T> iterator() {
    final Lst<T> that = this;
    return new Iterator<T>() {
      Lst<T> lst = that;

      @Override
      public boolean hasNext() {
        return !lst.isEmpty();
      }

      @Override
      public T next() {
        final T head = lst.head();
        lst = lst.tail();
        return head;
      }
    };
  }

  default <L> L foldLeft(L acc, BiFunction<L, T, L> f) {
    Objects.requireNonNull(f, "f should not be null");
    for (T x : this) {
      acc = f.apply(acc, x);
    }
    return acc;
  }

  default <R> R foldRight(R acc, BiFunction<T, R, R> f) {
    Objects.requireNonNull(f, "f should not be null");
    return reverse().foldLeft(acc, (r, t) -> f.apply(t, r));
  }


  default <R> Lst<R> flatMap(Function<T, Lst<R>> f) {
    Objects.requireNonNull(f, "f should not be null");
    return foldRight(Lst.empty(), (t, acc) -> acc.prepend(f.apply(t)));
  }

  default Lst<T> filter(Predicate<T> p) {
    Objects.requireNonNull(p, "p should not be null");
    return flatMap(t -> Optional.of(t).filter(p).map(Lst::of).orElseGet(Lst::empty));
  }

  final class Nil<T> implements Lst<T> {
    static Nil<?> instance = new Nil<>();

    @Override
    public T head() {
      throw new NoSuchElementException("head of empty list");
    }

    @Override
    public Lst<T> tail() {
      throw new NoSuchElementException("tail of empty list");
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public String toString() {
      return "Nil";
    }
  }

  final class Cons<T> implements Lst<T> {
    private final T head;
    private final Lst<T> tail;

    private Cons(T head, Lst<T> tail) {
      this.head = head;
      this.tail = tail;
    }

    @Override
    public T head() {
      return head;
    }

    @Override
    public Lst<T> tail() {
      return tail;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public String toString() {
      return String.format("%s :: %s", head, tail.toString());
    }
  }
}
