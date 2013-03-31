# Lear's GIST implementation, version 1.1, (c) INRIA 2009, Licence: PSFL


all: libgist.so
bin: compute_gist

JAVAINC=-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
WFFTINC=-I./fftw/include
WFFTLIB=-L./fftw/lib -L./fftw/lib64

gist.o: gist.c gist.h standalone_image.h
	gcc -fPIC -c -Wall $< $(WFFTINC) -DUSE_GIST -DSTANDALONE_GIST -lm

standalone_image.o: standalone_image.c standalone_image.h
	gcc -fPIC -c -Wall $< 

compute_gist: compute_gist.c gist.o standalone_image.o
	gcc -fPIC -Wall -o $@ $^ $(WFFTLIB) -lfftw3 -lm

libgist.so: sanji_image_feature_Gist.cpp sanji_image_feature_Gist.h gist.h standalone_image.h gist.o standalone_image.o
	@echo 'compiling...'
	gcc -fPIC -Wall $(JAVAINC) $(WFFTINC) -shared -o $@ $^ $(WFFTLIB) -lstdc++ -lfftw3 -lm

clean: 
	rm -f *.o compute_gist libgist.so

