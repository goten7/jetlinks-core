package org.jetlinks.core;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.codec.Decoder;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Setter
@NoArgsConstructor
public class NativePayload<T> implements Payload {

    private T nativeObject;

    private Function<T, Payload> bodySupplier;

    private volatile ByteBuf ref;

    private AtomicInteger retainCount = new AtomicInteger();
    private AtomicInteger releaseCount = new AtomicInteger();

    public static <T> NativePayload<T> of(T nativeObject, Function<T, Payload> bodySupplier) {
        NativePayload<T> payload = new NativePayload<>();

        payload.nativeObject = nativeObject;
        payload.bodySupplier = bodySupplier;
        return payload;
    }

    public static <T> NativePayload<T> of(T nativeObject, Supplier<Payload> bodySupplier) {
        return of(nativeObject, v -> bodySupplier.get());
    }


    @Override
    @SuppressWarnings("all")
    public <T> T decode(Decoder<T> decoder, boolean release) {
        if (decoder.isDecodeFrom(nativeObject)) {
            return (T) nativeObject;
        }
        return Payload.super.decode(decoder, release);
    }

    @Override
    public Object decode() {
        return nativeObject;
    }

    @Nonnull
    @Override
    public ByteBuf getBody() {
        if (ref == null) {
            synchronized (this) {
                if (ref != null) {
                    return ref;
                }
                ByteBuf buf = bodySupplier.apply(nativeObject).getBody();
                if (retainCount.get() > 0) {
                    buf.retain(retainCount.get());
                }
                if (releaseCount.get() > 0) {
                    buf.release(releaseCount.get());
                }
                ref = buf;
            }
        }
        return ref;
    }

    @Override
    public void release() {
        if (ref != null) {
            ref.release();
        } else {
            releaseCount.incrementAndGet();
        }
    }

    @Override
    public void retain() {
        if (ref != null) {
            ref.retain();
        } else {
            retainCount.incrementAndGet();
        }
    }

    @Override
    public void release(int dec) {
        if (ref != null) {
            ref.release(dec);
        } else {
            releaseCount.addAndGet(dec);
        }
    }

    @Override
    public void retain(int inc) {
        if (ref != null) {
            ref.retain(inc);
        } else {
            retainCount.addAndGet(inc);
        }
    }

    @Override
    public <T> T bodyAs(Class<T> type) {
        if (type.isInstance(nativeObject)) {
            return type.cast(nativeObject);
        }
        return Payload.super.bodyAs(type);
    }

    @Override
    public String toString() {
        return nativeObject == null ? "null" : nativeObject.toString();
    }
}
