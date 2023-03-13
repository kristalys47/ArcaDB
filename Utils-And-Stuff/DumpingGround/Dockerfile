FROM ghcr.io/graalvm/graalvm-ce:latest
ENTRYPOINT ["-it", "--mount type=bind, source=\"$(pwd)\", target=/"]
VOLUME .:/JavaCode
EXPOSE 4599: