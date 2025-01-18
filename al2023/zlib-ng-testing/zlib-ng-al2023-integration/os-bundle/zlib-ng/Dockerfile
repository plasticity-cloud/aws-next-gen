FROM registry.fedoraproject.org/fedora:40 AS source-dist

# python-zlib-ng will be built separately
RUN dnf install -y dnf-plugins-core

RUN cd /root && dnf download --enablerepo fedora-source,updates-source --source zlib-ng-compat python-zlib-ng && \
    ls -al /root/*.rpm
    
FROM ghcr.io/plasticity-cloud/amazonlinux/rpm-builder:2023 AS rpm-builder

COPY --from=source-dist /root/zlib-ng-2.1.7-2.fc40.src.rpm /home/rpm-builder/zlib-ng-2.1.7-2.amzn2023.src.rpm

RUN cd $HOME && \
     rpm -i $HOME/zlib-ng-2.1.7-2.amzn2023.src.rpm && \
     cd ~/rpmbuild/SPECS && rpmbuild -ba zlib-ng.spec && \
     ls -al $HOME/rpmbuild/RPMS/aarch64

RUN mkdir -p /opt/al2023/next-gen/repos/zlib-ng && \
    cp -R $HOME/rpmbuild/RPMS/aarch64/* /opt/al2023/next-gen/repos/zlib-ng/ && \
    cd /opt/al2023/next-gen/repos/zlib-ng && createrepo --repo=zlib-ng-os . && \
    tar -cf $HOME/al2023-zlib-ng.tar.gz /opt/al2023/next-gen/repos/zlib-ng/*

FROM public.ecr.aws/amazonlinux/amazonlinux:2023 AS al2023-zlib-ng-dist

RUN mkdir -p /opt/al2023/next-gen/repos/zlib-ng

COPY --from=rpm-builder /opt/al2023/next-gen/repos/zlib-ng/ /opt/al2023/next-gen/repos/zlib-ng/
COPY image_content/etc/yum.repos.d/amazonlinux-zlib-ng.repo /etc/yum.repos.d/

RUN dnf install -y zlib-ng-compat && dnf clean all
