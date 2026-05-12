FROM jenkins/jenkins:latest-jdk25

USER root

RUN apt-get update && apt-get install -y wget && \
    wget https://github.com/gitleaks/gitleaks/releases/download/v8.21.2/gitleaks_8.21.2_linux_x64.tar.gz && \
    tar -zxvf gitleaks_8.21.2_linux_x64.tar.gz && \
    mv gitleaks /usr/local/bin/ && \
    chmod +x /usr/local/bin/gitleaks && \
    rm gitleaks_8.21.2_linux_x64.tar.gz