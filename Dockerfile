FROM debian:12-slim
RUN apt-get update  \
    && apt-get -y --no-install-recommends install \
        # install any other dependencies you might need
        sudo curl ca-certificates fontconfig \
    && rm -rf /var/lib/apt/lists/*
RUN groupadd -g 1000 gradle \
 && useradd -u 1000 -g 1000 -m -d /home/gradle gradle
COPY mise-ci.toml /opt/mise/config/mise.toml
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
ENV MISE_DATA_DIR="/opt/mise/data"
ENV MISE_CONFIG_DIR="/opt/mise/config"
ENV MISE_CACHE_DIR="/opt/mise/cache"
ENV MISE_STATE_DIR="/opt/mise/state"
ENV MISE_INSTALL_PATH="/usr/local/bin/mise"
ENV PATH="/opt/mise/data/shims:$PATH"
RUN ln -s /opt/mise/data/shims/vale /usr/bin/vale
# ENV MISE_VERSION="..."
RUN curl https://mise.run | MISE_DATA_DIR="/opt/mise/data" MISE_CONFIG_DIR="/opt/mise/config" MISE_CACHE_DIR="/opt/mise/cache" MISE_STATE_DIR="/opt/mise/state" MISE_INSTALL_PATH="/usr/local/bin/mise" sh
RUN echo 'eval "$(/usr/local/bin/mise activate bash)"' >> /root/.bashrc
RUN mise install
RUN mkdir -p "/home/gradle/project" && cd /home/gradle/project && touch mise.toml && mise trust --ignore mise.toml
RUN chown -R gradle:gradle /home/gradle
USER gradle
WORKDIR /home/gradle
COPY --chown=gradle .vale.ini ./
RUN vale sync