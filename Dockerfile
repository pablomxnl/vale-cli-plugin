FROM gradle:8.9.0-jdk21 as gradle
ARG VALE_VERSION=3.7.0
RUN curl -OL https://github.com/errata-ai/vale/releases/download/v${VALE_VERSION}/vale_${VALE_VERSION}_Linux_64-bit.tar.gz && tar -xvzf vale_${VALE_VERSION}_Linux_64-bit.tar.gz -C /usr/bin
USER gradle
WORKDIR /home/gradle
COPY --chown=gradle .vale.ini ./
RUN vale sync

