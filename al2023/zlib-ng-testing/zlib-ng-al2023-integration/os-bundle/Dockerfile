FROM registry.fedoraproject.org/fedora:40 AS zlib-ng-source-dist

# python-zlib-ng will be built separately

RUN dnf install -y dnf-plugins-core

RUN cd /root && dnf download --enablerepo fedora-source,updates-source --source zlib-ng-compat && \
    ls -al /root/zlib-ng-2.1.7-2.fc40.src.rpm
    
FROM public.ecr.aws/amazonlinux/amazonlinux:2023 AS zlib-ng-builder


RUN dnf install -y git gcc g++ make automake cmake gtest gtest-devel libabigail amazon-rpm-config rpm-build multilib-rpm-config createrepo

COPY --from=zlib-ng-source-dist /root/zlib-ng-2.1.7-2.fc40.src.rpm /root/zlib-ng-2.1.7-2.amzn2023.src.rpm

RUN cd /root && \
     mkdir -p ~/rpmbuild/{BUILD,RPMS,SOURCES,SPECS,SRPMS} && \
     echo '%_topdir %(echo $HOME)/rpmbuild' > ~/.rpmmacros && \
     rpm -i /root/zlib-ng-2.1.7-2.amzn2023.src.rpm && \
     cd ~/rpmbuild/SPECS && rpmbuild -ba zlib-ng.spec && \
     ls -al /root/rpmbuild/RPMS/aarch64

RUN mkdir -p /opt/al2023/next-gen/repos/zlib-ng && \
cp -R /root/rpmbuild/RPMS/aarch64/* /opt/al2023/next-gen/repos/zlib-ng/ && \
cd /opt/al2023/next-gen/repos/zlib-ng && createrepo --repo=zlib-ng-os . && \
tar -cf $HOME/al2023-zlib-ng.tar.gz /opt/al2023/next-gen/repos/zlib-ng/*

FROM public.ecr.aws/amazonlinux/amazonlinux:2023 AS zlib-ng-dist

RUN mkdir -p /opt/al2023/next-gen/repos/zlib-ng

COPY --from=zlib-ng-builder /opt/al2023/next-gen/repos/zlib-ng/ /opt/al2023/next-gen/repos/zlib-ng/
COPY image_content/etc/yum.repos.d/amazonlinux-zlib-ng.repo /etc/yum.repos.d/

RUN dnf install -y zlib-ng-compat && dnf clean all
