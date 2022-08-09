package liu.xiao.zor.jvmbench;

import org.openjdk.jmh.annotations.*;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@State(Scope.Thread)
public class XzCompression1MiB {

    public int size = 1048576;

    public byte[] rawBytes = new byte[size];
    public byte[] compressedBytes;
    public XZOutputStream xzOutputStream;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        Random random = new SecureRandom();
        List<byte[]> dictionary = new ArrayList<>();
        int remainingDictionarySize = size / 2;
        while (remainingDictionarySize > 0) {
            int elementSize = 1 + random.nextInt(remainingDictionarySize);
            byte[] element = new byte[elementSize];
            random.nextBytes(element);
            dictionary.add(element);
            remainingDictionarySize -= elementSize;
        }

        int position = 0;
        while (position < rawBytes.length) {
            int index = random.nextInt(dictionary.size());
            int length = Math.min(dictionary.get(index).length, rawBytes.length - position);
            System.arraycopy(dictionary.get(index), 0, rawBytes, position, length);
            position += length;
        }

        ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream(size);
        xzOutputStream = new XZOutputStream(compressedOutputStream, new LZMA2Options());
        xzOutputStream.write(rawBytes);
        xzOutputStream.flush();
        compressedBytes = compressedOutputStream.toByteArray();

        xzOutputStream = new XZOutputStream(new OutputStream() {
            @Override
            public void write(int b) {
            }

            @Override
            public void write(byte[] b) {
            }

            @Override
            public void write(byte[] b, int off, int len) {
            }
        }, new LZMA2Options());
    }

    public static XZOutputStream compress(XzCompression1MiB xz) throws IOException {
        xz.xzOutputStream.write(xz.rawBytes);
        xz.xzOutputStream.flush();
        return xz.xzOutputStream;
    }

    @Benchmark
    @Threads(1)
    public static XZOutputStream _01_singleThread(XzCompression1MiB xz) throws IOException {
        return compress(xz);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static XZOutputStream _02_multiThread(XzCompression1MiB xz) throws IOException {
        return compress(xz);
    }
}
