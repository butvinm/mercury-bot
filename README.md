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
    - Select `api` scope.

## Build and Deployment

### The Simplest Way

1. Download the latest `app.jar` from the [latest release](https://github.com/butvinm/mercury-bot/releases/latest).
2. Create a `share` directory - database data would be stored there.
3. Run the following command:
```bash
java -jar app.jar \
    --share="/share" \
    --bot.token=Token from BotFather \
    --gitlab.host=https://gitlab.com \
    --gitlab.access.token=GitLab PAT \
```

Optional settings:
```
--users.db=users.db \
--chats.db=chats.db \
--filters.db=filters.db
```

### ~~For zoomers~~ In Docker

1. Create a `.env` (or just rename `.example-env`) file and fill in the credentials:
```dotenv
BOT_TOKEN=Token from BotFather
GITLAB_HOST=https://gitlab.com
GITLAB_ACCESS_TOKEN=GitLab PAT
SHARE=/share
```

Optional settings:
```
USERS_DB=users.db
CHATS_DB=chats.db
FILTERS_DB=filters.db
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

### Authorization

Auth system is very strange, because it was designed to reduce amount of work for my lazy ass.

**Common user**: Enter `/login <part of bot token before ":">`

**Adming**: Enter `/login <whole bot token>`

### Filters

Mercury Bot has pretty powerful events filtering system.

You can manage filters with following commands:
- `/add_job_filter {path}={regex}`
- `/del_job_filter {path}`
- `/clear_job_filters`
- `/add_pipeline_filter {path}={regex}`
- `/del_pipeline_filter {path}`
- `/clear_pipeline_filters`
- `/show_filters`
- `/help_filters`

What is {path} and {regex}?

Regex is just regular expression that would be used to validate field value (its string representation, actually).

Path is a bit more complecated. It is string in JsonPointer format directly inherited from Jackson library. If you want to get name of job author, path would be: `/user/name`. You can refer to [RFC](https://datatracker.ietf.org/doc/html/rfc6901) for more details.

**Job fields available for filtering:**
```json
{
  "createdAt": "2024-02-12T02:11:07Z",
  "duration": 0.08851946,
  "failureReason": "unknown_failure",
  "finishedAt": null,
  "id": 6147367853,
  "name": "build-job2",
  "queuedDuration": 0.3411436,
  "stage": "build",
  "startedAt": "2024-02-12T02:11:09Z",
  "status": "RUNNING",
  "pipeline_id": 1172795709,
  "objectKind": "build",
  "ref": "main",
  "user": {
    "id": 16837135,
    "name": "Mihail Butvin",
    "username": "butvinm",
    "avatarUrl": "https://secure.gravatar.com/avatar/77398f38bf23d7ea429b49162d49252a?s=80&d=identicon",
    "email": "[REDACTED]"
  },
  "project": {
    "id": 53607146,
    "name": "Mercury Test",
    "description": null,
    "webUrl": "https://gitlab.com/butvinm/mercury-test",
    "avatarUrl": null,
    "gitSshUrl": "git@gitlab.com:butvinm/mercury-test.git",
    "gitHttpUrl": "https://gitlab.com/butvinm/mercury-test.git",
    "namespace": "Mihail Butvin",
    "pathWithNamespace": "butvinm/mercury-test",
    "defaultBranch": "main",
    "homepage": null,
    "url": null,
    "sshUrl": null,
    "httpUrl": null
  }
}

```

**Pipeline fields available for filtering:**
```json
{
    "object_kind": "pipeline",
    "object_attributes": {
        "id": 1097064499,
        "iid": 4,
        "name": null,
        "ref": "test2",
        "source": "merge_request_event",
        "status": "running",
        "stages": [
            "test"
        ],
        "created_at": "2023-12-06 09:10:09 UTC",
        "finished_at": null,
        "duration": null,
    },
    "merge_request": {
        "id": 268297921,
        "iid": 2,
        "title": "Update file README.md",
        "source_branch": "test2",
        "source_project_id": 50273425,
        "target_branch": "main",
        "target_project_id": 50273425,
        "state": "opened",
        "merge_status": "checking"
    },
    "user": {
        "id": 16837135,
        "name": "Mihail Butvin",
        "username": "butvinm",
        "avatar_url": "https://secure.gravatar.com/avatar/77398f38bf23d7ea429b49162d49252a?s=80&d=identicon",
        "email": "[REDACTED]"
    },
    "project": {
        "id": 50273425,
        "name": "test",
        "description": null,
        "web_url": "https://gitlab.com/test6425537/test",
        "avatar_url": null,
        "git_ssh_url": "git@gitlab.com:test6425537/test.git",
        "git_http_url": "https://gitlab.com/test6425537/test.git",
        "namespace": "test",
        "path_with_namespace": "test6425537/test",
        "default_branch": "main",
    },
    "builds": [
        {
            "id": 5690969305,
            "stage": "build",
            "name": "job1",
            "status": "running",
            "created_at": "2023-12-06 09:10:09 UTC",
            "started_at": "2023-12-06 09:10:10 UTC",
            "finished_at": null,
            "duration": 0.466666359,
            "queued_duration": 0.160732,
            "failure_reason": null,
            "when": "on_success",
            "manual": false,
            "allow_failure": false,
            "user": {
                "id": 16837135,
                "name": "Mihail Butvin",
                "username": "butvinm",
                "avatar_url": "https://secure.gravatar.com/avatar/77398f38bf23d7ea429b49162d49252a?s=80&d=identicon",
                "email": "[REDACTED]"
            }
        }
    ]
}
```

### Groups bindings (admins)

To bind bot to the group:
- Add bot to the group members
- Bot should have send you **Bind** button after `/login` command. Click it and select target group. In case of success bot would reply "Chat was bind".

Unbinding is not implemented yet, because our data storage does not support deleting.

### Rebuild button (admins)

Each pipeline/job event has **Rebuild** button. For individual jobs it would rebuild only that job and for pipelines - all related jobs.

### Custom messages

You can push additional information to the bot from GitLab CI. We have `scripts/pipeline_message.sh` and `scripts/job_message.sh` scripts those you can trigger to send message that would be added to bot digests.

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
- [x] Pipeline tag info in digest
- [x] Notifications in private chat, not only in groups
- [x] Allow bind multiple groups
- [x] Admin rights management from UI
- [x] Digest about each individual pipeline job
- [x] Pipelines filtering
