// vim: et sw=2

#ifndef ERAND48_HPP
#define ERAND48_HPP

inline double erand48(unsigned short xsubi[3]) {
  const unsigned long long m = 1ULL << 48;
  const unsigned long long a = 0x5DEECE66DULL;
  const unsigned long long c = 0xB;

  unsigned long long x = xsubi[0] + ((unsigned long long)xsubi[1] << 16) + ((unsigned long long)xsubi[2] << 32);
  x = (a * x + c) & (m - 1);

  xsubi[0] = (x      ) & 0xFFFF;
  xsubi[1] = (x >> 16) & 0xFFFF;
  xsubi[2] = (x >> 32) & 0xFFFF;

  return (double)x / m;
}

#endif
