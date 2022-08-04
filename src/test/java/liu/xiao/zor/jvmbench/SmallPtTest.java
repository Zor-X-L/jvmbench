package liu.xiao.zor.jvmbench;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Scanner;

public class SmallPtTest {

    private static void assertERand48(short i1, short i2, short i3, double o, short o1, short o2, short o3) {
        short[] x = new short[3];
        x[0] = i1;
        x[1] = i2;
        x[2] = i3;
        Assert.assertEquals(o, SmallPt.eRand48(x), 0);
        Assert.assertEquals(o1, x[0]);
        Assert.assertEquals(o2, x[1]);
        Assert.assertEquals(o3, x[2]);
    }

    @Test
    public void eRand48() {
        assertERand48((short) 32624, (short) -265, (short) 1425, 0.703499264988504791, (short) -7493, (short) -30945, (short) -19432);
        assertERand48((short) 1172, (short) 10545, (short) 3393, 0.757036472651687831, (short) -5361, (short) 9323, (short) -15923);
        assertERand48((short) -28352, (short) 350, (short) 32426, 0.777358219806313144, (short) 22603, (short) -3389, (short) -14592);
        assertERand48((short) 26707, (short) -4760, (short) -22803, 0.277115446280170374, (short) -670, (short) 2482, (short) 18161);
        assertERand48((short) 14640, (short) 2660, (short) 2683, 0.615811365999871185, (short) 31099, (short) -12211, (short) -25179);
    }

    private static class Ppm {
        public int w, h, maxValue;
        public int[] rgb;

        Ppm(int w, int h, SmallPt.Vec[] c) {
            this.w = w;
            this.h = h;
            this.maxValue = 255;
            rgb = new int[c.length * 3];
            for (int i = 0; i < c.length; ++i) {
                rgb[3 * i] = SmallPt.toInt(c[i].x);
                rgb[3 * i + 1] = SmallPt.toInt(c[i].y);
                rgb[3 * i + 2] = SmallPt.toInt(c[i].z);
            }
        }

        Ppm(InputStream inputStream) {
            try (Scanner scanner = new Scanner(new BufferedInputStream(inputStream))) {
                Assert.assertEquals("P3", scanner.next());
                w = scanner.nextInt();
                h = scanner.nextInt();
                maxValue = scanner.nextInt();
                Assert.assertEquals(255, maxValue);
                rgb = new int[3 * w * h];
                for (int i = 0; i < 3 * w * h; ++i) {
                    rgb[i] = scanner.nextInt();
                }
            }
        }

        public void write(Path path) throws Exception {
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.ISO_8859_1)) {
                writer.write("P3\n");
                writer.write("" + w + " " + h + "\n");
                writer.write("" + maxValue + "\n");
                for (int i = 0; i < rgb.length; i += 3) {
                    writer.write("" + rgb[i] + " " + rgb[i+1] + " " + rgb[i+2] + "\n");
                }
            }
        }

        public double relativeDifference(Ppm b) {
            Assert.assertEquals(w, b.w);
            Assert.assertEquals(h, b.h);
            Assert.assertEquals(rgb.length, b.rgb.length);
            double diff = 0;
            double mean = 0;
            for (int i = 0; i < rgb.length; ++i) {
                double da = rgb[i];
                double db = b.rgb[i];
                double x = da - db;
                diff += x * x;
                mean += da * da + db * db;
            }
            // 2-norm of (a-b)
            diff = Math.sqrt(diff);
            // quadratic mean (generalized mean similar to 2-norm) of a and b
            mean = Math.sqrt(mean * 0.5);
            return diff / mean;
        }
    }

    @Test
    public void parallelRender() throws Exception {
        Ppm gcc = new Ppm(this.getClass().getResourceAsStream("/SmallPt-gcc25k.ppm"));
        Ppm clangFixed = new Ppm(this.getClass().getResourceAsStream("/SmallPt-clangFixed25k.ppm"));
        Assert.assertEquals(gcc.w, clangFixed.w);
        Assert.assertEquals(gcc.h, clangFixed.h);

        int w = gcc.w, h = gcc.h;
        short[][] Xi = new short[h][3];
        for (int y = 0; y < h; y++) Xi[y][2] = (short) (y * y * y);
        SmallPt.Vec[] c = SmallPt.parallelRender(w, h, 1000 / 4, Xi);
        Ppm java = new Ppm(w, h, c);
        java.write(Paths.get(
                Paths.get(this.getClass().getResource("/").toURI()).toString(),
                "SmallPt-parallelRender25k.ppm"));
        double diffGcc = java.relativeDifference(gcc);
        double diffClangFixed = java.relativeDifference(clangFixed);
        System.out.println(diffGcc);
        System.out.println(diffClangFixed);
        Assert.assertTrue(Math.min(diffGcc, diffClangFixed) < 0.02);
    }

    @Test
    public void parallelRenderRandom() throws Exception {
        Ppm gcc = new Ppm(this.getClass().getResourceAsStream("/SmallPt-gcc25k.ppm"));
        Ppm clangFixed = new Ppm(this.getClass().getResourceAsStream("/SmallPt-clangFixed25k.ppm"));
        Assert.assertEquals(gcc.w, clangFixed.w);
        Assert.assertEquals(gcc.h, clangFixed.h);

        int w = gcc.w, h = gcc.h;
        short[][] Xi = new short[h][3];
        Random random = new SecureRandom();
        for (int y = 0; y < h; y++) {
            long r = random.nextLong();
            Xi[y][0] = (short) ((r >> 48) & 0xFFFF);
            Xi[y][1] = (short) ((r >> 32) & 0xFFFF);
            Xi[y][2] = (short) ((r >> 16) & 0xFFFF);
        }
        SmallPt.Vec[] c = SmallPt.parallelRender(w, h, 25000 / 4, Xi);
        Ppm java = new Ppm(w, h, c);
        java.write(Paths.get(
                Paths.get(this.getClass().getResource("/").toURI()).toString(),
                "SmallPt-parallelRenderRandom25k.ppm"));
        double diffGcc = java.relativeDifference(gcc);
        double diffClangFixed = java.relativeDifference(clangFixed);
        System.out.println(diffGcc);
        System.out.println(diffClangFixed);
        Assert.assertTrue(Math.min(diffGcc, diffClangFixed) < 0.02);
    }
}
