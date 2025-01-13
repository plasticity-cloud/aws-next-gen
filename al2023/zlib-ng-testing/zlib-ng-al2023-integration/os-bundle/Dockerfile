FROM registry.fedoraproject.org/fedora:40 AS source-dist

# python-zlib-ng will be built separately

RUN dnf install -y dnf-plugins-core

RUN cd /root && dnf download --enablerepo fedora-source,updates-source --source zlib-ng-compat && \
    ls -al /root/zlib-ng-2.1.7-2.fc40.src.rpm
    
FROM public.ecr.aws/amazonlinux/amazonlinux:2023 AS builder

COPY --from=source-dist /root/zlib-ng-2.1.7-2.fc40.src.rpm /root/zlib-ng-2.1.7-2.amzn2023.src.rpm
