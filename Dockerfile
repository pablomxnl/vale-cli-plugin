FROM gradle:8.2.1-jdk17 as gradle
COPY --from=jdkato/vale /bin/vale /usr/bin/
WORKDIR /home/gradle
COPY --chown=gradle .vale.ini ./
RUN vale sync

