# smallpt Makefile
#
# 


# Select appropriate compiler.
CPP=g++
#CPP=g++-4.2
CPPFLAGS=-O3 -fopenmp -Wl,--stack,16777216       # Use this for gcc >= 4.2
#CPPFLAGS=-O3               # Use this for gcc <  4.2

ifdef DEBUG
CPPFLAGS=-g
endif

default: all

all: smallpt.exe explicit.exe forward.exe

smallpt.exe: smallpt.cpp Makefile
	$(CPP) $(CPPFLAGS) -o smallpt.exe smallpt.cpp

explicit.exe: explicit.cpp Makefile
	$(CPP) $(CPPFLAGS) -o explicit.exe explicit.cpp 

forward.exe: forward.cpp Makefile
	$(CPP) $(CPPFLAGS) -o forward.exe forward.cpp 

clean:
	-rm -f smallpt.exe explicit.exe forward.exe
