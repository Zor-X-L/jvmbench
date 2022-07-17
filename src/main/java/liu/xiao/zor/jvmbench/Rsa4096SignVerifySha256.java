package liu.xiao.zor.jvmbench;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.openjdk.jmh.annotations.*;

import java.security.*;
import java.util.Random;

@State(Scope.Thread)
public class Rsa4096SignVerifySha256 {

    public int messageSize = 1024;
    public int keySize = 4096;

    byte[] message = new byte[messageSize];
    byte[] encodedDigest;

    KeyPair keyPair;
    byte[] signature;

    Signature signer;
    Signature verifier;

    @Setup(Level.Iteration)
    public void setup() throws Exception {
        Random random = new SecureRandom();
        random.nextBytes(message);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(message);

        // https://stackoverflow.com/questions/33305800/difference-between-sha256withrsa-and-sha256-then-rsa
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE);
        DigestInfo digestInfo = new DigestInfo(algorithmIdentifier, digest);
        encodedDigest = digestInfo.getEncoded();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        keyPair = keyPairGenerator.generateKeyPair();

        Signature sha256withRSA = Signature.getInstance("SHA256withRSA");
        sha256withRSA.initSign(keyPair.getPrivate());
        sha256withRSA.update(message);
        signature = sha256withRSA.sign();

        signer = Signature.getInstance("NONEwithRSA");
        signer.initSign(keyPair.getPrivate());

        verifier = Signature.getInstance("NONEwithRSA");
        verifier.initVerify(keyPair.getPublic());
    }

    public static byte[] sign(Rsa4096SignVerifySha256 state) throws Exception {
        state.signer.update(state.encodedDigest);
        return state.signer.sign();
    }

    public static boolean verify(Rsa4096SignVerifySha256 state) throws Exception {
        state.verifier.update(state.encodedDigest);
        return state.verifier.verify(state.signature);
    }

    @Benchmark
    @Threads(1)
    public static byte[] _01_signSingleThread(Rsa4096SignVerifySha256 state) throws Exception {
        return sign(state);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static byte[] _02_signMultiThread(Rsa4096SignVerifySha256 state) throws Exception {
        return sign(state);
    }

    @Benchmark
    @Threads(1)
    public static boolean _03_verifySingleThread(Rsa4096SignVerifySha256 state) throws Exception {
        return verify(state);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public static boolean _04_verifyMultiThread(Rsa4096SignVerifySha256 state) throws Exception {
        return verify(state);
    }
}
