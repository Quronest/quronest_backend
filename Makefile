VERSION := $(shell grep '<version>' pom.xml | sed -n '2p' | sed -E 's/.*<version>([^<]+)<\/version>.*/\1/')

.PHONY: build clean test dev-containers dev liquibase

build: target/quronest_backend-$(VERSION).jar

target/quronest_backend-$(VERSION).jar:
	./mvnw clean package -Dmaven.test.skip=true

clean:
	./mvnw clean

test:
	./mvnw test

dev-containers:
	./run_dev.sh

dev: dev-containers clean build
	java -jar target/quronest_backend-$(VERSION).jar

liquibase: dev-containers
	./mvnw liquibase:update
