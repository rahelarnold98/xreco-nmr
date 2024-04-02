FROM gradle:jdk21 AS build

COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle --no-daemon distTar
WORKDIR /src/nmr-backend/build/distributions/
RUN tar xf ./nmr-backend.tar

FROM openjdk:21-jdk

COPY ingestPipelines /ingestPipelines


COPY --from=build /src/nmr-backend/build/distributions/nmr-backend /nmr-backend

EXPOSE 7070

RUN microdnf install findutils

ENTRYPOINT /nmr-backend/bin/nmr