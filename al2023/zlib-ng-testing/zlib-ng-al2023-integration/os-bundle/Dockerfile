FROM registry.fedoraproject.org/fedora:40 AS source-dist

# python-zlib-ng will be built separately

RUN dnf install -y dnf-plugins-core

RUN cd /root && dnf download --enablerepo fedora-source,updates-source --source zlib-ng-compat && \
    ls -al /root/zlib-ng-2.1.7-2.fc40.src.rpm
    
FROM public.ecr.aws/amazonlinux/amazonlinux:2023 AS builder

COPY --from=source-dist /root/zlib-ng-2.1.7-2.fc40.src.rpm /root/zlib-ng-2.1.7-2.amzn2023.src.rpm

RUN cd /root && \
    dnf install -y git gcc g++ make automake cmake gtest gtest-devel libabigail amazon-rpm-config rpm-build multilib-rpm-config && \
     mkdir -p ~/rpmbuild/{BUILD,RPMS,SOURCES,SPECS,SRPMS} && \
     echo '%_topdir %(echo $HOME)/rpmbuild' > ~/.rpmmacros && \
     rpm -i /root/zlib-ng-2.1.7-2.fc40.src.rpm && \
     cd ~/rpmbuild/SPECS && rpmbuild -ba zlib-ng.spec && \
     ls -al /root/rpmbuild/RPMS/aarch64


FROM public.ecr.aws/amazonlinux/amazonlinux:2023 AS zlib-ng-dist

RUN mkdir /root/zlib-ng-rpms

COPY --from=builder /root/rpmbuild/RPMS/aarch64/*.rpm /root/zlib-ng-rpms/

RUN dnf install -y /root/zlib-ng-rpms/*.rpm
