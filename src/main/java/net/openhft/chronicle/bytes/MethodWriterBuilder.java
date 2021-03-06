package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.io.Closeable;

import java.util.function.Supplier;

@Deprecated(/*is it used?*/)
public interface MethodWriterBuilder<T> extends Supplier<T> {
    MethodWriterBuilder<T> methodWriterListener(MethodWriterListener methodWriterListener);

    MethodWriterBuilder<T> genericEvent(String genericEvent);

    MethodWriterBuilder<T> useMethodIds(boolean useMethodIds);

    MethodWriterBuilder<T> onClose(Closeable closeable);

    MethodWriterBuilder<T> recordHistory(boolean recordHistory);

    default T build() {
        return get();
    }
}
