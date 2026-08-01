FROM debian:12-slim

ARG GLAB_VERSION=1.107.0

RUN apt-get update \
    && apt-get -y --no-install-recommends install \
        ca-certificates \
        curl \
        nodejs \
        git \
        npm \
        zip \
        unzip \
    && rm -rf /var/lib/apt/lists/*

RUN npm install -g downdoc

RUN curl -sSL "https://gitlab.com/gitlab-org/cli/-/releases/v${GLAB_VERSION}/downloads/glab_${GLAB_VERSION}_linux_amd64.tar.gz" \
    | tar -xz -C /usr/local/bin --strip-components=1 bin/glab \
    && chmod +x /usr/local/bin/glab