package liu.xiao.zor.jvmbench;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.*;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;

@State(Scope.Thread)
public class Blake2b512 {

    @Param({"102400"})
    public int blakeMsgLen;
    public byte[] input;
    public MessageDigest messageDigest;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Setup(Level.Iteration)
    public void setup() throws Exception {
        Random random = new SecureRandom();
        input = new byte[blakeMsgLen];
        random.nextBytes(input);
        messageDigest = MessageDigest.getInstance("BLAKE2B-512", BouncyCastleProvider.PROVIDER_NAME);
    }

    public static byte[] hash(Blake2b512 state) {
        return state.messageDigest.digest(state.input);
    }

    @Benchmark
    @Threads(1)
    public static byte[] _01_singleThread(Blake2b512 state) {
        return hash(state);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static byte[] _02_multiThread(Blake2b512 state) {
        return hash(state);
    }
}
