FROM public.ecr.aws/amazonlinux/amazonlinux:2023

RUN dnf install -y git gcc g++ make automake cmake gtest gtest-devel libabigail python-devel amazon-rpm-config rpm-build multilib-rpm-config createrepo && dnf clean all

RUN useradd -m rpm-builder && \
    mkdir -p /opt/al2023/next-gen/repos && \
    chown -R rpm-builder:root /opt/al2023/next-gen/repos/ && \
    chmod 775 /opt/al2023/next-gen/repos/
    
USER rpm-builder

RUN mkdir -p ~/rpmbuild/{BUILD,RPMS,SOURCES,SPECS,SRPMS} && \
    echo '%_topdir %(echo $HOME)/rpmbuild' > ~/.rpmmacros
