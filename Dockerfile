FROM gradle:8.1.1-jdk11 as gradle
COPY --from=jdkato/vale /bin/vale /usr/bin/
WORKDIR /home/gradle
COPY --chown=gradle .vale.ini ./
RUN vale sync

