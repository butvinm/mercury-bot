.PHONY: all dev deploy

SOURCES := $(shell find src -name '*.java')
JAR := target/bot-0.0.1-SNAPSHOT.jar

SERVER_USER ?=
SERVER_IP ?=
SERVER_SSH = "$(SERVER_USER)@$(SERVER_IP)"

DOCKER_FLAGS = --env-file .env \
	-v ./cnt-tmp:/tmp/logs \
	-v ${PWD}/users.db:/users.db

BIND_PORT = 80
DEV_PORT = 6969

all: dev

$(JAR): $(SOURCES)
	mvn clean package

deploy: $(JAR)
	scp $(JAR) $(SERVER_SSH):~/target
	scp Dockerfile $(SERVER_SSH):~/Dockerfile-bot
	scp .env $(SERVER_SSH):~
	ssh $(SERVER_SSH) touch users.db
	ssh $(SERVER_SSH) docker ps -a -q | xargs -r docker stop || true
	ssh $(SERVER_SSH) docker ps -a -q | xargs -r docker kill || true
	ssh $(SERVER_SSH) docker ps -a -q | xargs -r docker rm || true
	ssh $(SERVER_SSH) docker build -t bot -f Dockerfile-bot .
	ssh $(SERVER_SSH) docker run $(DOCKER_FLAGS) -d -p $(BIND_PORT):8080 bot

dev: $(JAR)
	touch users.db
	docker build -t mercury.bot .
	docker run $(DOCKER_FLAGS) -p $(DEV_PORT):8080 mercury.bot:latest
