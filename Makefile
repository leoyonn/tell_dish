all: libgist
	@echo compile $^ done!

libgist:
	@echo compiling $^...
	cd ./src/cpp/gist && make clean && make && cd ../../..
	cp ./src/cpp/gist/libgist.so ./so/libgist.so
	cp ./src/cpp/gist/fftw/lib/libfftw3.so.3 ./so/libfftw3.so.3
	cd so && ln -s libfftw3.so.3 libfftw3.so && cd ..

clean:
	rm -f ./so/*
