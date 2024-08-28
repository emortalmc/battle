FROM --platform=$TARGETPLATFORM azul/zulu-openjdk:21.0.4-jre

RUN mkdir /app
WORKDIR /app

# Download packages
RUN apt-get update && apt-get install -y wget

COPY build/libs/*-all.jar /app/battle.jar
COPY run/maps /app/maps

ENTRYPOINT ["java"]
CMD ["-jar", "/app/battle.jar"]
