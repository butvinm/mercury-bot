.PHONY: all dev deploy

SOURCES := $(shell find src -name '*.java')
JAR := target/bot-0.0.1-SNAPSHOT.jar

SERVER_USER ?=
SERVER_IP ?=
SERVER_SSH = "$(SERVER_USER)@$(SERVER_IP)"

BIND_PORT = 6969

all: dev

$(JAR): $(SOURCES)
	mvn clean package

deploy: $(JAR)
	scp $(JAR) $(SERVER_SSH):~/target
	scp Dockerfile-bot $(SERVER_SSH):~
	scp .env $(SERVER_SSH):~
	ssh $(SERVER_SSH) "docker ps -a -q | xargs -r docker stop || true"
	ssh $(SERVER_SSH) "docker ps -a -q | xargs -r docker kill || true"
	ssh $(SERVER_SSH) "docker ps -a -q | xargs -r docker rm || true"
	ssh $(SERVER_SSH) "docker build -t bot Dockerfile-bot"
	ssh $(SERVER_SSH) "docker run --env-file .env -v ./cnt-tmp:/tmp/logs -d -p $(BIND_PORT):8080 bot"

dev: $(JAR)
	docker build -t mercury.bot .
	docker run --env-file .env -v ./cnt-tmp:/tmp/logs -p $(BIND_PORT):8080 mercury.bot:latest
