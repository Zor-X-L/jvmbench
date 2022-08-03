package liu.xiao.zor.jvmbench;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SmallPt {

    public static double eRand48(short[] xSubI) {
        final long m = 1L << 48;
        final long a = 0x5DEECE66DL;
        final long c = 0xB;

        long x = Short.toUnsignedLong(xSubI[0])
                | (Short.toUnsignedLong(xSubI[1]) << 16)
                | (Short.toUnsignedLong(xSubI[2]) << 32);

        x = (a * x + c) & (m - 1);

        xSubI[0] = (short) (x & 0xFFFF);
        xSubI[1] = (short) ((x >> 16) & 0xFFFF);
        xSubI[2] = (short) ((x >> 32) & 0xFFFF);

        return (double) x / m;
    }

    public static class Vec {
        public double x, y, z; // position, also color (r,g,b)

        public Vec() {
            this(0, 0, 0);
        }

        public Vec(double x_) {
            this(x_, 0, 0);
        }

        public Vec(double x_, double y_) {
            this(x_, y_, 0);
        }

        public Vec(double x_, double y_, double z_) {
            x = x_;
            y = y_;
            z = z_;
        }

        public Vec add(final Vec b) {
            return new Vec(x + b.x, y + b.y, z + b.z);
        }

        public Vec subtract(final Vec b) {
            return new Vec(x - b.x, y - b.y, z - b.z);
        }

        public Vec scale(double b) {
            return new Vec(x * b, y * b, z * b);
        }

        public Vec multiply(final Vec b) {
            return new Vec(x * b.x, y * b.y, z * b.z);
        }

        public Vec norm() {
            double scale = 1 / Math.sqrt(x * x + y * y + z * z);
            x *= scale;
            y *= scale;
            z *= scale;
            return this;
        }

        public double dot(final Vec b) {
            return x * b.x + y * b.y + z * b.z;
        }

        public Vec cross(final Vec b) {
            return new Vec(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
        }
    }

    public static class Ray {
        public Vec o, d;

        public Ray(Vec o_, Vec d_) {
            o = o_;
            d = d_;
        }
    }

    public enum ReflectionType {DIFFUSE, SPECULAR, REFRACTIVE} // material types, used in radiance()

    public static class Sphere {
        public double rad; // radius
        public Vec p, e, c; // position, emission, color
        public ReflectionType reflectionType; // reflection type (diffuse, specular, refractive)

        public Sphere(double rad_, Vec p_, Vec e_, Vec c_, ReflectionType reflectionType_) {
            rad = rad_;
            p = p_;
            e = e_;
            c = c_;
            reflectionType = reflectionType_;
        }

        double intersect(final Ray r) { // returns distance, 0 if no hit
            Vec op = p.subtract(r.o); // Solve t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
            double t, eps = 1e-4, b = op.dot(r.d), det = b * b - op.dot(op) + rad * rad;
            if (det < 0) return 0;
            else det = Math.sqrt(det);
            return (t = b - det) > eps ? t : ((t = b + det) > eps ? t : 0);
        }
    }

    public static Sphere[] spheres = new Sphere[]{ //Scene: radius, position, emission, color, material
            new Sphere(1e5, new Vec(1e5 + 1, 40.8, 81.6), new Vec(), new Vec(.75, .25, .25), ReflectionType.DIFFUSE),//Left
            new Sphere(1e5, new Vec(-1e5 + 99, 40.8, 81.6), new Vec(), new Vec(.25, .25, .75), ReflectionType.DIFFUSE),//Right
            new Sphere(1e5, new Vec(50, 40.8, 1e5), new Vec(), new Vec(.75, .75, .75), ReflectionType.DIFFUSE),//Back
            new Sphere(1e5, new Vec(50, 40.8, -1e5 + 170), new Vec(), new Vec(), ReflectionType.DIFFUSE),//Front
            new Sphere(1e5, new Vec(50, 1e5, 81.6), new Vec(), new Vec(.75, .75, .75), ReflectionType.DIFFUSE),//Bottom
            new Sphere(1e5, new Vec(50, -1e5 + 81.6, 81.6), new Vec(), new Vec(.75, .75, .75), ReflectionType.DIFFUSE),//Top
            new Sphere(16.5, new Vec(27, 16.5, 47), new Vec(), new Vec(1, 1, 1).scale(.999), ReflectionType.SPECULAR),//Mirror
            new Sphere(16.5, new Vec(73, 16.5, 78), new Vec(), new Vec(1, 1, 1).scale(.999), ReflectionType.REFRACTIVE),//Glass
            new Sphere(600, new Vec(50, 681.6 - .27, 81.6), new Vec(12, 12, 12), new Vec(), ReflectionType.DIFFUSE) //Lite
    };

    public static double clamp(double a) {
        return a < 0 ? 0 : a > 1 ? 1 : a;
    }

    public static int toInt(double a) {
        return (int) (Math.pow(clamp(a), 1 / 2.2) * 255 + .5);
    }

    public static class MutableDouble {
        private double value;

        public MutableDouble(double value) {
            this.value = value;
        }

        public double get() {
            return value;
        }

        public double set(double value) {
            this.value = value;
            return value;
        }
    }

    public static class MutableInteger {
        private int value;

        public MutableInteger(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

        public void set(int value) {
            this.value = value;
        }
    }

    public static boolean intersect(final Ray r, MutableDouble t, MutableInteger id) {
        double d, inf = t.set(1e20);
        for (int i = spheres.length - 1; i >= 0; --i)
            if ((d = spheres[i].intersect(r)) != 0 && d < t.get()) {
                t.set(d);
                id.set(i);
            }
        return t.get() < inf;
    }

    public static Vec radiance(final Ray r, int depth, short[] Xi) {
        MutableDouble t = new MutableDouble(0); // distance to intersection
        MutableInteger id = new MutableInteger(0); // id of intersected object
        if (!intersect(r, t, id)) return new Vec(); // if missed, return black
        final Sphere obj = spheres[id.get()]; // the hit object
        Vec x = r.o.add(r.d.scale(t.get())), n = x.subtract(obj.p).norm(), nl = n.dot(r.d) < 0 ? n : n.scale(-1), f = obj.c;
        double p = f.x > f.y && f.x > f.z ? f.x : Math.max(f.y, f.z); // max reflectionType
        if (++depth > 5) if (eRand48(Xi) < p) f = f.scale(1 / p);
        else return obj.e; // R.R.
        if (obj.reflectionType == ReflectionType.DIFFUSE) { // Ideal DIFFUSE reflectionType
            double r1 = 2 * Math.PI * eRand48(Xi), r2 = eRand48(Xi), r2s = Math.sqrt(r2);
            Vec u = ((Math.abs(nl.x) > .1 ? new Vec(0, 1) : new Vec(1)).cross(nl)).norm(), v = nl.cross(u);
            Vec d = u.scale(Math.cos(r1) * r2s).add(v.scale(Math.sin(r1) * r2s)).add(nl.scale(Math.sqrt(1 - r2))).norm();
            return obj.e.add(f.multiply(radiance(new Ray(x, d), depth, Xi)));
        } else if (obj.reflectionType == ReflectionType.SPECULAR) // Ideal SPECULAR reflectionType
            return obj.e.add(f.multiply(radiance(new Ray(x, r.d.subtract(n.scale(2 * n.dot(r.d)))), depth, Xi)));
        Ray reflectedRay = new Ray(x, r.d.subtract(n.scale(2 * n.dot(r.d)))); // Ideal dielectric REFRACTION
        boolean into = n.dot(nl) > 0; // Ray from outside going in?
        double nc = 1, nt = 1.5, nnt = into ? nc / nt : nt / nc, ddn = r.d.dot(nl), cos2t;
        if ((cos2t = 1 - nnt * nnt * (1 - ddn * ddn)) < 0) // Total internal reflectionType
            return obj.e.add(f.multiply(radiance(reflectedRay, depth, Xi)));
        Vec tDir = r.d.scale(nnt).subtract(n.scale((into ? 1 : -1) * (ddn * nnt + Math.sqrt(cos2t)))).norm();
        double a = nt - nc, b = nt + nc, R0 = a * a / (b * b), c = 1 - (into ? -ddn : tDir.dot(n));
        double Re = R0 + (1 - R0) * c * c * c * c * c, Tr = 1 - Re, P = .25 + .5 * Re, RP = Re / P, TP = Tr / (1 - P);
        return obj.e.add(f.multiply(depth > 2 ? (eRand48(Xi) < P ?   // Russian roulette
                radiance(reflectedRay, depth, Xi).scale(RP) : radiance(new Ray(x, tDir), depth, Xi).scale(TP)) :
                radiance(reflectedRay, depth, Xi).scale(Re).add(radiance(new Ray(x, tDir), depth, Xi).scale(Tr))));
    }

    public static void main(String[] args) throws Exception {
        int w = 1024, h = 768, samples = args.length > 0 ? Integer.parseInt(args[0]) / 4 : 1; // # samples
        Ray cam = new Ray(new Vec(50, 52, 295.6), new Vec(0, -0.042612, -1).norm()); // cam pos, dir
        Vec cx = new Vec(w * .5135 / h), cy = cx.cross(cam.d).norm().scale(.5135);
        Vec[] c = new Vec[w * h];
        for (int i = 0; i < c.length; ++i) c[i] = new Vec();
        List<Integer> yList = new ArrayList<>();
        for (int y = 0; y < h; y++) yList.add(y);
        yList.parallelStream().forEach(y -> {
            System.err.printf("\rRendering (%d spp) %5.2f%%", samples * 4, 100. * y / (h - 1));
            short[] Xi = new short[3];
            Xi[2] = (short) (y * y * y);
            for (short x = 0; x < w; x++) {  // Loop cols
                for (int sy = 0, i = (h - y - 1) * w + x; sy < 2; sy++) { // 2x2 subpixel rows
                    for (int sx = 0; sx < 2; sx++) { // 2x2 subpixel cols
                        Vec r = new Vec();
                        for (int s = 0; s < samples; s++) {
                            double r1 = 2 * eRand48(Xi), dx = r1 < 1 ? Math.sqrt(r1) - 1 : 1 - Math.sqrt(2 - r1);
                            double r2 = 2 * eRand48(Xi), dy = r2 < 1 ? Math.sqrt(r2) - 1 : 1 - Math.sqrt(2 - r2);
                            Vec d = cx.scale(((sx + .5 + dx) / 2 + x) / w - .5)
                                    .add(cy.scale(((sy + .5 + dy) / 2 + y) / h - .5)).add(cam.d);
                            // "Ray(cam.o+d*140,d.norm()" is an unspecified behavior.
                            // With gcc, d.norm() will be evaluated first (thus normalized d), and produce the correct result
                            // With clang and Java, cam.o+d*140 will be evaluated first,
                            // produce several white lines at the top of the result.
                            // But even after fixing the issue, gcc and clang still produce different result.
                            // Maybe because different implementation of math functions like sin, cos, etc.
                            d.norm();
                            r = r.add(radiance(new Ray(cam.o.add(d.scale(140)), d), 0, Xi).scale(1. / samples));
                        } // Camera rays are pushed ^^^^^ forward to start in interior
                        c[i] = c[i].add(new Vec(clamp(r.x), clamp(r.y), clamp(r.z)).scale(.25));
                    }
                }
            }
        });
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get("image.ppm"), StandardCharsets.ISO_8859_1)) {
            writer.write("P3\n" + w + " " + h + "\n255\n");
            for (int i = 0; i < w * h; i++) {
                writer.write("" + toInt(c[i].x) + " " + toInt(c[i].y) + " " + toInt(c[i].z) + " ");
            }
        }
    }
}
