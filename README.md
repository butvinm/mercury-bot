> *You can make classes, but you can only ever make one instance of them. This shouldn't affect how
most object-oriented programmers work. - [TodePond](https://github.com/TodePond/DreamBerd---e-acc?tab=readme-ov-file#classes)*

# Mercury Bot

Telagram bot that notifies about GitLab pipelines statuses and allow rebuild pipeline in on click.

## Prerequisites

Before using Mercury Bot, make sure to:

1. Create Telegram Bot:
    - Open Telegram, search for "@BotFather," and start a chat by clicking "Start."
    - Type /newbot, follow @BotFather's instructions, and choose a name with a username ending in "bot."
    - Copy the API Token to set in `.env` file later.

2. Set up a webhook in GitLab to trigger Mercury Bot:
    - Navigate to Repository -> Settings -> Webhooks.
    - Add a new pipelines webhook:
        - **URL**: `<application-host>/pipelines`.
        - **Trigger**: Pipeline events.
    - Add a new jobs webhook:
        - **URL**: `<application-host>/jobs`.
        - **Trigger**: Job events.

3. Get Personal Access Token (PAT) for GitLab:
    - Navigate to Edit profile -> Access Token -> Add new token.
    - Enter a name and expiry date for the token.
    - Select the desired scopes. Mercury Bot requires at least write and read access for pipelines and jobs.

## Build and Deployment

### The Simplest Way

1. Download the latest `app.jar` from the [latest release](https://github.com/butvinm/mercury-bot/releases/latest).
2. Create a `share` directory - database data would be stored there.
3. Run the following command:
```bash
java -jar app.jar \
    --share="./share" \
    --bot.token=Token from BotFather \
    --gitlab.host=https://gitlab.com \
    --gitlab.access.token=GitLab PAT \
    --users.db=users.db \
    --chats.db=chats.db
```

### ~~For zoomers~~ In Docker

1. Create a `.env` (or just rename `.example-env`) file and fill in the credentials:
```dotenv
BOT_TOKEN=Token from BotFather
GITLAB_HOST=https://gitlab.com
GITLAB_ACCESS_TOKEN=GitLab PAT
SHARE=share
USERS_DB=users.db
CHATS_DB=chats.db
```

2. Build the Docker image:
```bash
scripts/build.sh
```

3. Run the Docker container:
```bash
scripts/run.sh
```

## Management and configuration

### Bind group or group

To bind bot to the group:
- Add bot to the group members
- Go to private chat with bot and type `/start`
- Bot would send your **Bind** button. Click it and selet target channke. In case of success bot would reply "Chat was bind".

### Rebuild button and admin rights

Only bot admins can use "Rebuild" button. To grant admin right to user go to file, specified as `users.db` - it is actually json, that you can easily modify:
```json
{
    "5609708885": {
        "id": 5609708885,
        "username": "butvinm",
        "is_admin": true  // variable, you should modify
    }
}
```

> Users are added to the users.db only after some message to the target chat.

### Custom messages

You can push additional information to the bot during pipeline. We have `scripts/pipeline_message.sh` and `scripts/job_message.sh` scripts those you can trigger to send message that would be added to bot digests.

```yaml
variables:
    BOT_HOST: 10.10.10.10 # host of Pipeline Bot server

buildServer:
    stage: build
    image: maven:3.8.5-openjdk-11
    script:
        - mvn clean install -e -DskipTests=true
        - ./pipeline_message.sh "Hello, meine Freunde!"
        - ./job_message.sh "Hello, meine Freunde!"
```

## Roadmap

- [x] Pipeline job's digest
- [x] Rebuild button
- [ ] Pipeline tag info in digest
- [x] Notifications in private chat, not only in groups
- [x] Allow bind multiple groups
- [ ] Admin rights management from UI
- [x] Digest about each individual pipeline job
- [ ] Pipelines filtering
