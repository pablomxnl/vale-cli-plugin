FROM gradle:9.1-jdk21 AS gradle
ARG VALE_VERSION=3.13.0
RUN curl -OL https://github.com/errata-ai/vale/releases/download/v${VALE_VERSION}/vale_${VALE_VERSION}_Linux_64-bit.tar.gz && \
    tar -xvzf vale_${VALE_VERSION}_Linux_64-bit.tar.gz -C /usr/bin
WORKDIR /home/gradle
COPY --chown=gradle .vale.ini ./
RUN vale sync