FROM gradle:jdk17 AS build

COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle --no-daemon distTar
WORKDIR /src/nmr-backend/build/distributions/
RUN tar xf ./nmr-backend.tar

FROM eclipse-temurin:17-jre

COPY config.json /
COPY --from=build /src/nmr-backend/build/distributions/nmr-backend /nmr-backend

EXPOSE 8080

ENTRYPOINT /nmr-backend/bin/nmr-backend /config.json