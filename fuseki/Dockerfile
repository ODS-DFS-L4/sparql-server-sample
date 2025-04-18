# First stage: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY fuseki-extension/pom.xml ./pom.xml
COPY fuseki-extension/src ./src

# Build the application
RUN mvn package

# Second stage: Create the final image
FROM eclipse-temurin:21-jre-alpine

RUN set -eux; \
    apk -U upgrade; \
    apk add bash curl ca-certificates findutils coreutils gettext pwgen procps tini; \
    rm -rf /var/cache/apk/*

# Download from Binary Distributions
# Reference: https://jena.apache.org/download/
ENV FUSEKI_SHA512 5204eefefb921ec029346139f5cb768fe298c816c8642ab590c9bdcee4f24cfacfb15c4266f7acf020d0d5232eea909e4af876f1d5162231ea4b8f8fe0feb3cf
ENV FUSEKI_VERSION 5.3.0
ENV ASF_MIRROR https://www.apache.org/dyn/mirrors/mirrors.cgi?action=download&filename=
ENV ASF_ARCHIVE https://archive.apache.org/dist/

# Directories
ENV FUSEKI_BASE /fuseki
ENV FUSEKI_DIR /jena-fuseki

# Download, Verify, Extract, and Prepare
WORKDIR /tmp
RUN echo "$FUSEKI_SHA512  fuseki.tar.gz" > fuseki.tar.gz.sha512
RUN (curl --location --silent --show-error --fail --retry-connrefused --retry 3 --output fuseki.tar.gz ${ASF_MIRROR}jena/binaries/apache-jena-fuseki-$FUSEKI_VERSION.tar.gz || \
    curl --fail --silent --show-error --retry-connrefused --retry 3 --output fuseki.tar.gz $ASF_ARCHIVE/jena/binaries/apache-jena-fuseki-$FUSEKI_VERSION.tar.gz) && \
    sha512sum -c fuseki.tar.gz.sha512 && \
    tar zxf fuseki.tar.gz && \
    mv apache-jena-fuseki* $FUSEKI_DIR && \
    rm fuseki.tar.gz* && \
    cd $FUSEKI_DIR && rm -rf fuseki.war && chmod 755 fuseki-server

COPY shiro.ini $FUSEKI_DIR/shiro.ini
COPY entrypoint.sh /
RUN chmod 755 /entrypoint.sh

# Create User and Group
RUN addgroup -g 1001 -S fuseki && \
    adduser -u 1001 -G fuseki -S -D -H fuseki

# Initialize directories
WORKDIR $FUSEKI_DIR
RUN chown -R fuseki:fuseki $FUSEKI_DIR
RUN mkdir -p $FUSEKI_BASE; \
    chown -R fuseki:fuseki $FUSEKI_BASE

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar /fuseki-extra/

EXPOSE 3030
USER fuseki

ENTRYPOINT ["/sbin/tini", "--", "sh", "/entrypoint.sh"]

CMD ["/jena-fuseki/fuseki-server"]

