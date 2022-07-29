//        x  = { 32624,  65271,   1425}
//(short) x  = { 32624,   -265,   1425}
//erand48(x) = 0.703499264988504791
//        x  = { 58043,  34591,  46104}
//(short) x  = { -7493, -30945, -19432}
//
//        x  = {  1172,  10545,   3393}
//(short) x  = {  1172,  10545,   3393}
//erand48(x) = 0.757036472651687831
//        x  = { 60175,   9323,  49613}
//(short) x  = { -5361,   9323, -15923}
//
//        x  = { 37184,    350,  32426}
//(short) x  = {-28352,    350,  32426}
//erand48(x) = 0.777358219806313144
//        x  = { 22603,  62147,  50944}
//(short) x  = { 22603,  -3389, -14592}
//
//        x  = { 26707,  60776,  42733}
//(short) x  = { 26707,  -4760, -22803}
//erand48(x) = 0.277115446280170374
//        x  = { 64866,   2482,  18161}
//(short) x  = {  -670,   2482,  18161}
//
//        x  = { 14640,   2660,   2683}
//(short) x  = { 14640,   2660,   2683}
//erand48(x) = 0.615811365999871185
//        x  = { 31099,  53325,  40357}
//(short) x  = { 31099, -12211, -25179}

#include<stdlib.h>
#include<stdio.h>

void test(unsigned short x[3]) {
    printf("        x  = {%6hu, %6hu, %6hu}\n",          x[0],        x[1],        x[2]);
    printf("(short) x  = {%6hd, %6hd, %6hd}\n",   (short)x[0], (short)x[1], (short)x[2]);
    printf("erand48(x) = %.18lf\n",                erand48(x));
    printf("        x  = {%6hu, %6hu, %6hu}\n",          x[0],        x[1],        x[2]);
    printf("(short) x  = {%6hd, %6hd, %6hd}\n\n", (short)x[0], (short)x[1], (short)x[2]);
}

int main(void) {
    unsigned short x[][3] = {
        {32624, 65271,  1425},
        {1172,  10545,  3393},
        {37184,   350, 32426},
        {26707, 60776, 42733},
        {14640,  2660,  2683}
    };

    int i;
    for (i = 0; i < sizeof(x) / sizeof(unsigned short[3]); ++i) {
        test(x[i]);
    }

    return 0;
}