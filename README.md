# Mercury Pipeline

Spring webapp that listens for GitLab events and triggers YT issues transitions.

## Custom messages

```yaml
variables:
    BOT_HOST: 10.10.10.10

buildServer:
    stage: build
    image: maven:3.8.5-openjdk-11
    script:
        - mvn clean install -e -DskipTests=true
        - ./bot.sh "Hello, meine Freunde!"
```
