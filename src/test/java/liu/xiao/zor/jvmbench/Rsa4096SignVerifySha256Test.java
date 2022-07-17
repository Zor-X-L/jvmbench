package liu.xiao.zor.jvmbench;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;

public class Rsa4096SignVerifySha256Test {

    @Test
    public void noneWithRsa() throws Exception {
        Rsa4096SignVerifySha256 state = new Rsa4096SignVerifySha256();
        state.setup();
        Assert.assertArrayEquals(state.signature, Rsa4096SignVerifySha256.sign(state));
        Assert.assertTrue(Rsa4096SignVerifySha256.verify(state));
    }

    @Test
    public void rsaEcbPkcs1Padding() throws Exception {
        Rsa4096SignVerifySha256 state = new Rsa4096SignVerifySha256();
        state.setup();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, state.keyPair.getPrivate());
        Assert.assertArrayEquals(state.signature, cipher.doFinal(state.encodedDigest));

        cipher.init(Cipher.DECRYPT_MODE, state.keyPair.getPublic());
        Assert.assertArrayEquals(state.encodedDigest, cipher.doFinal(state.signature));
    }
}
