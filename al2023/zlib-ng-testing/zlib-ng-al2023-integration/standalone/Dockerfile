FROM public.ecr.aws/amazonlinux/amazonlinux:2023-minimal AS builder

RUN dnf install -y git gcc automake autoconf cmake gcc-c++
RUN mkdir $HOME/projects

RUN cd $HOME/projects && \
    git clone --depth 1 --branch 2.1.7 https://github.com/zlib-ng/zlib-ng.git && \
    cd zlib-ng && \
    git status && \
    export CFLAGS='-g -O3 -march=native' && \
    ./configure --zlib-compat && make && make install

FROM public.ecr.aws/amazonlinux/amazonlinux:2023-minimal AS al2023-zlib-ng


# install amazon corretto
RUN rpm --import https://yum.corretto.aws/corretto.key && \
curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo && \
dnf install -y java-21-amazon-corretto-devel

COPY --from=builder /root/projects/zlib-ng/libz.so.1.3.1.zlib-ng /usr/lib/jvm/java-21-amazon-corretto/bin/../lib/libz.so.1
