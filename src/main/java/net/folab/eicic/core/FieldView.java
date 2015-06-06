package net.folab.eicic.core;

import java.util.function.Function;

public class FieldView<T, V> {

    public final String name;

    public final Function<T, V> getter;

    public final int width;

    public FieldView(String name, Function<T, V> getter) {
        this(name, getter, -1);
    }

    public FieldView(String name, Function<T, V> getter, int width) {
        super();
        this.name = name;
        this.getter = getter;
        this.width = width;
    }

}
