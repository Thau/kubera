FROM clojure:lein-2.7.1 AS builder

COPY . /src
WORKDIR /src

RUN lein uberjar

FROM openjdk:8-alpine

RUN apk --update add postgresql && rm -rf /var/cache/apk/*
COPY --from=builder /src/target/uberjar/kubera-clojure-0.1.0-SNAPSHOT-standalone.jar /kubera.jar
CMD ["java", "-jar", "/kubera.jar"]
