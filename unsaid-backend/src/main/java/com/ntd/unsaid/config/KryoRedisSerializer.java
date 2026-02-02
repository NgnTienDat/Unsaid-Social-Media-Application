package com.ntd.unsaid.config;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.ntd.unsaid.application.dto.FeedPostDTO;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom Redis serializer using Kryo for high-performance serialization.
 * Kryo is significantly faster and produces smaller payloads than JSON serialization.
 *
 * @param <T> the type of object to serialize
 */
public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    private final Class<T> clazz;

    // Thread-safe pool of Kryo instances
    private final Pool<Kryo> kryoPool = new Pool<>(true, false, 128) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);

            kryo.register(FeedPostDTO.class);
            kryo.register(ArrayList.class);
            kryo.register(HashMap.class);

            return kryo;
        }
    };

    public KryoRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }

        Kryo kryo = kryoPool.obtain();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            kryo.writeClassAndObject(output, t);
            output.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Error serializing object using Kryo", e);
        } finally {
            kryoPool.free(kryo);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        Kryo kryo = kryoPool.obtain();
        try (Input input = new Input(bytes)) {
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing object using Kryo", e);
        } finally {
            kryoPool.free(kryo);
        }
    }
}
