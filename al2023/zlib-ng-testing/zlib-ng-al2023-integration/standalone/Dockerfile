FROM public.ecr.aws/amazonlinux/amazonlinux:2023-minimal AS builder

RUN dnf install -y git gcc automake autoconf cmake gcc-c++
RUN mkdir $HOME/projects

RUN cd $HOME/projects && \
    git clone --depth 1 --branch 2.1.7 https://github.com/zlib-ng/zlib-ng.git && \
    cd zlib-ng && \
    git status && \
    export CFLAGS='-g -O3 -mcpu=neoverse-n1 -march=native &&' \
    ./configure --zlib-compat && make && make install 
