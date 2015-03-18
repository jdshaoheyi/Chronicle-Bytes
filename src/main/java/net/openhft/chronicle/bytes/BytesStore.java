package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.ReferenceCounted;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * A reference to some bytes with fixed extents.  Only offset access within the capacity is possible.
 */
public interface BytesStore<B extends BytesStore<B, Underlying>, Underlying> extends RandomDataInput<B>, RandomDataOutput<B>, ReferenceCounted {
    static BytesStore wrap(byte[] bytes) {
        return HeapBytesStore.wrap(ByteBuffer.wrap(bytes));
    }

    static BytesStore wrap(ByteBuffer bb) {
        return bb.isDirect()
                ? NativeStore.wrap(bb)
                : HeapBytesStore.wrap(bb);
    }

    default Bytes bytes() {
        return bytes(UnderflowMode.BOUNDED);
    }

    default Bytes bytes(UnderflowMode underflowMode) {
        switch (underflowMode) {
            case BOUNDED:
                return new BytesStoreBytes(this);
            case ZERO_EXTEND:
            case PADDED:
                return new ZeroedBytes(this, underflowMode);
            default:
                throw new UnsupportedOperationException("Unknown known mode " + underflowMode);
        }
    }

    /**
     * @return The smallest position allowed in this buffer.
     */
    default long start() {
        return 0L;
    }

    /**
     * @return the actual capacity available before resizing.
     */
    default long realCapacity() {
        return capacity();
    }

    /**
     * @return The maximum limit you can set.
     */
    long capacity();

    /**
     * Perform a set of actions with a temporary bounds mode.
     */
    default BytesStore with(long position, long length, Consumer<Bytes> bytesConsumer) {
        if (position + length > capacity())
            throw new BufferUnderflowException();
        BytesStoreBytes bsb = new BytesStoreBytes(this);
        bsb.position(position);
        bsb.limit(position + length);
        bytesConsumer.accept(bsb);
        return this;
    }

    /**
     * Use this test to determine if an offset is considered safe.
     */
    default boolean inStore(long offset) {
        return start() <= offset && offset < safeLimit();
    }

    default long safeLimit() {
        return capacity();
    }

    void storeFence();

    void loadFence();


    default void copyTo(BytesStore store) {
        Bytes b1 = bytes();
        b1.limit(b1.realCapacity());
        Bytes b2 = store.bytes();
        b2.write(b1);
        b2.release();
        b1.release();
    }

    Underlying underlyingObject();

    @Override
    default boolean isNative() {
        return underlyingObject() == null;
    }
}
