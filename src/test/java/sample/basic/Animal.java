package sample.basic;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class Animal {
    abstract String name();
    abstract int age();

    static Builder builder() {
        // The naming here will be different if you are using a nested class
        // e.g. `return new AutoValue_OuterClass_InnerClass.Builder();`
        return new AutoValue_Animal.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder name(String value);
        abstract Builder age(int value);
        abstract Animal build();
    }
}