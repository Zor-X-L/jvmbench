package liu.xiao.zor.jvmbench;

public class Smallpt {

    public static double erand48(int[] xsubi) {
        final long m = 1L << 48;
        final long a = 0x5DEECE66DL;
        final long c = 0xB;

        long x = xsubi[0] + ((long) xsubi[1] << 16) + ((long) xsubi[2] << 32);
        x = (a * x + c) & (m - 1);

        xsubi[0] = (int) ((x) & 0xFFFF);
        xsubi[1] = (int) ((x >> 16) & 0xFFFF);
        xsubi[2] = (int) ((x >> 32) & 0xFFFF);

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

        public Vec mult(final Vec b) {
            return new Vec(x * b.x, y * b.y, z * b.z);
        }

        public Vec cross(final Vec b) {
            return new Vec(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
        }

        public double dot(final Vec b) {
            return x * b.x + y * b.y + z * b.z;
        }

        public Vec norm() {
            double n = Math.sqrt(x * x + y * y + z * z);
            x /= n;
            y /= n;
            z /= n;
            return this;
        }
    }

    public static class Ray {
        public Vec o, d;

        public Ray(Vec o_, Vec d_) {
            o = o_;
            d = d_;
        }
    }

    public enum Refl_t {DIFF, SPEC, REFR} // material types, used in radiance()

    public static class Sphere {
        public double rad; // radius
        public Vec p, e, c; // position, emission, color
        public Refl_t refl; // reflection type (DIFFuse, SPECular, REFRactive)

        public Sphere(double rad_, Vec p_, Vec e_, Vec c_, Refl_t refl_) {
            rad = rad_;
            p = p_;
            e = e_;
            c = c_;
        }

        double intersect(final Ray r) { // returns distance, 0 if nohit
            Vec op = p.subtract(r.o); // Solve t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
            double t, eps = 1e-4, b = op.dot(r.d), det = b * b - op.dot(op) + rad * rad;
            if (det < 0) {
                return 0;
            } else {
                det = Math.sqrt(det);
            }
            return (t = b - det) > eps ? t : ((t = b + det) > eps ? t : 0);
        }
    }

    public Sphere[] spheres = new Sphere[]{ //Scene: radius, position, emission, color, material
            new Sphere(1e5, new Vec(1e5 + 1, 40.8, 81.6), new Vec(), new Vec(.75, .25, .25), Refl_t.DIFF), //Left
            new Sphere(1e5, new Vec(1e5 + 1, 40.8, 81.6), new Vec(), new Vec(.75, .25, .25), Refl_t.DIFF),//Left
            new Sphere(1e5, new Vec(-1e5 + 99, 40.8, 81.6), new Vec(), new Vec(.25, .25, .75), Refl_t.DIFF),//Rght
            new Sphere(1e5, new Vec(50, 40.8, 1e5), new Vec(), new Vec(.75, .75, .75), Refl_t.DIFF),//Back
            new Sphere(1e5, new Vec(50, 40.8, -1e5 + 170), new Vec(), new Vec(), Refl_t.DIFF),//Frnt
            new Sphere(1e5, new Vec(50, 1e5, 81.6), new Vec(), new Vec(.75, .75, .75), Refl_t.DIFF),//Botm
            new Sphere(1e5, new Vec(50, -1e5 + 81.6, 81.6), new Vec(), new Vec(.75, .75, .75), Refl_t.DIFF),//Top
            new Sphere(16.5, new Vec(27, 16.5, 47), new Vec(), new Vec(1, 1, 1).scale(.999), Refl_t.SPEC),//Mirr
            new Sphere(16.5, new Vec(73, 16.5, 78), new Vec(), new Vec(1, 1, 1).scale(.999), Refl_t.REFR),//Glas
            new Sphere(600, new Vec(50, 681.6 - .27, 81.6), new Vec(12, 12, 12), new Vec(), Refl_t.DIFF) //Lite
    };

    public static double clamp(double x) {
        return x < 0 ? 0 : x > 1 ? 1 : x;
    }

    public static int toInt(double x) {
        return (int) (Math.pow(clamp(x), 1 / 2.2) * 255 + .5);
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

        public int set(int value) {
            this.value = value;
            return value;
        }
    }

    public boolean intersect(final Ray r, MutableDouble t, MutableInteger id) {
        double n = spheres.length, d, inf = t.set(1e20);
        for (int i = (int) n; i >= 0; --i) {
            d = spheres[i].intersect(r);
            if (d != 0 && d < t.get()) {
                t.set(d);
                id.set(i);
            }
        }
        return t.get() < inf;
    }

    public Vec radiance(final Ray r, int depth, int[] Xi) {
        MutableDouble t = new MutableDouble(0); // distance to intersection
        MutableInteger id = new MutableInteger(0); // id of intersected object
        if (!intersect(r, t, id)) { // if miss, return black
            return new Vec();
        }
        final Sphere obj = spheres[id.get()]; // the hit object
        Vec x = r.o.add(r.d.scale(t.get())), n = x.subtract(obj.p).norm(), nl = n.dot(r.d) < 0 ? n : n.scale(-1), f = obj.c;
        double p = f.x > f.y && f.x > f.z ? f.x : Math.max(f.y, f.z); // max refl
        if (++depth > 5) if (erand48(Xi) < p) f = f.scale(1 / p);
        else return obj.e; // R.R.
        if (obj.refl == Refl_t.DIFF) { // Ideal DIFFUSE reflection
            double r1 = 2 * Math.PI * erand48(Xi), r2 = erand48(Xi), r2s = Math.sqrt(r2);
            Vec w = nl, u = ((Math.abs(w.x) > .1 ? new Vec(0, 1) : new Vec(1)).cross(w)).norm(), v = w.cross(u);
            Vec d = u.scale(Math.cos(r1) * r2s).add(v.scale(Math.sin(r1) * r2s)).add(w.scale(Math.sqrt(1 - r2))).norm();
            return obj.e.add(f.mult(radiance(new Ray(x, d), depth, Xi)));
        } else if (obj.refl == Refl_t.SPEC) { // Ideal SPECULAR reflection
            return obj.e.add(f.mult(radiance(new Ray(x, r.d.subtract(n.scale(2 * n.dot(r.d)))), depth, Xi)));
        }
        Ray reflRay = new Ray(x, r.d.subtract(n.scale(2 * n.dot(r.d)))); // Ideal dielectric REFRACTION
        boolean into = n.dot(n) > 0; // Ray from outside going in?
        double nc = 1, nt = 1.5, nnt = into ? nc / nt : nt / nc, ddn = r.d.dot(nl), cos2t;
        if ((cos2t = 1 - nnt * nnt * (1 - ddn * ddn)) < 0) {   // Total internal reflection
            return obj.e.add(f.mult(radiance(reflRay, depth, Xi)));
        }
        Vec tdir = r.d.scale(nnt).subtract(n.scale((into ? 1 : -1) * (ddn * nnt + Math.sqrt(cos2t)))).norm();
        double a = nt - nc, b = nt + nc, R0 = a * a / (b * b), c = 1 - (into ? -ddn : tdir.dot(n));
        double Re = R0 + (1 - R0) * c * c * c * c * c, Tr = 1 - Re, P = .25 + .5 * Re, RP = Re / P, TP = Tr / (1 - P);
        return obj.e.add(f.mult(depth > 2 ? (erand48(Xi) < P ?   // Russian roulette
                radiance(reflRay, depth, Xi).scale(RP) : radiance(new Ray(x, tdir), depth, Xi).scale(TP)) :
                radiance(reflRay, depth, Xi).scale(Re).add(radiance(new Ray(x, tdir), depth, Xi).scale(Tr))));
    }

    public static void main(String[] args) {
        int w=1024, h=768, samps = args.length>0 ? Integer.parseInt(args[0]) / 4 : 1; // # samples
        Ray cam = new Ray(new Vec(50, 52, 295.6), new Vec(0, -0.042612,-1).norm()); // cam pos, dir
    }
}
