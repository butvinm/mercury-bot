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
    "before_sha": "3a0e6e6d0ca8f34e4a2ef8efdb761a2cadea6ea1",
    "build_allow_failure": false,
    "build_created_at": "2024-02-12 02:11:07 UTC",
    "build_duration": 0.088519463,
    "build_failure_reason": "unknown_failure",
    "build_finished_at": null,
    "build_id": 6147367853,
    "build_name": "build-job2",
    "build_queued_duration": 0.341143608,
    "build_stage": "build",
    "build_started_at": "2024-02-12 02:11:09 UTC",
    "build_status": "running",
    "commit": {
        "author_email": "butvin.mihail@yandex.ru",
        "author_name": "Mihail Butvin",
        "author_url": "https://gitlab.com/butvinm",
        "duration": null,
        "finished_at": null,
        "id": 1172795709,
        "message": "Update .gitlab-ci.yml file",
        "name": null,
        "sha": "8847c996b0166b7e0a8b9ed794de2185ec0e7348",
        "started_at": "2024-02-12 02:11:09 UTC",
        "status": "running"
    },
    "environment": null,
    "object_kind": "build",
    "pipeline_id": 1172795709,
    "project": {
        "avatar_url": null,
        "ci_config_path": "",
        "default_branch": "main",
        "description": null,
        "git_http_url": "https://gitlab.com/butvinm/mercury-test.git",
        "git_ssh_url": "git@gitlab.com:butvinm/mercury-test.git",
        "id": 53607146,
        "name": "Mercury Test",
        "namespace": "Mihail Butvin",
        "path_with_namespace": "butvinm/mercury-test",
        "visibility_level": 0,
        "web_url": "https://gitlab.com/butvinm/mercury-test"
    },
    "project_id": 53607146,
    "project_name": "Mihail Butvin / Mercury Test",
    "ref": "main",
    "repository": {
        "description": null,
        "git_http_url": "https://gitlab.com/butvinm/mercury-test.git",
        "git_ssh_url": "git@gitlab.com:butvinm/mercury-test.git",
        "homepage": "https://gitlab.com/butvinm/mercury-test",
        "name": "Mercury Test",
        "url": "git@gitlab.com:butvinm/mercury-test.git",
        "visibility_level": 0
    },
    "retries_count": 0,
    "runner": {
        "active": true,
        "description": "2-blue.saas-linux-small-amd64.runners-manager.gitlab.com/default",
        "id": 12270831,
        "is_shared": true,
        "runner_type": "instance_type",
        "tags": [
            "gce",
            "east-c",
            "linux",
            "ruby",
            "mysql",
            "postgres",
            "mongo",
            "git-annex",
            "shared",
            "docker",
            "saas-linux-small-amd64"
        ]
    },
    "sha": "8847c996b0166b7e0a8b9ed794de2185ec0e7348",
    "tag": false,
    "user": {
        "avatar_url": "https://secure.gravatar.com/avatar/77398f38bf23d7ea429b49162d49252a?s=80&d=identicon",
        "email": "[REDACTED]",
        "id": 16837135,
        "name": "Mihail Butvin",
        "username": "butvinm"
    }
}
```

**Pipeline fields available for filtering:**
```json
{
    "object_kind": "pipeline",
    "object_attributes": {
        "id": 31,
        "iid": 3,
        "name": "Pipeline for branch: master",
        "ref": "master",
        "tag": false,
        "sha": "bcbb5ec396a2c0f828686f14fac9b80b780504f2",
        "before_sha": "bcbb5ec396a2c0f828686f14fac9b80b780504f2",
        "source": "merge_request_event",
        "status": "success",
        "stages": [
            "build",
            "test",
            "deploy"
        ],
        "created_at": "2016-08-12 15:23:28 UTC",
        "finished_at": "2016-08-12 15:26:29 UTC",
        "duration": 63,
        "variables": [
            {
                "key": "NESTOR_PROD_ENVIRONMENT",
                "value": "us-west-1"
            }
        ],
        "url": "http://example.com/gitlab-org/gitlab-test/-/pipelines/31"
    },
    "merge_request": {
        "id": 1,
        "iid": 1,
        "title": "Test",
        "source_branch": "test",
        "source_project_id": 1,
        "target_branch": "master",
        "target_project_id": 1,
        "state": "opened",
        "merge_status": "can_be_merged",
        "detailed_merge_status": "mergeable",
        "url": "http://192.168.64.1:3005/gitlab-org/gitlab-test/merge_requests/1"
    },
    "user": {
        "id": 1,
        "name": "Administrator",
        "username": "root",
        "avatar_url": "http://www.gravatar.com/avatar/e32bd13e2add097461cb96824b7a829c?s=80\u0026d=identicon",
        "email": "user_email@gitlab.com"
    },
    "project": {
        "id": 1,
        "name": "Gitlab Test",
        "description": "Atque in sunt eos similique dolores voluptatem.",
        "web_url": "http://192.168.64.1:3005/gitlab-org/gitlab-test",
        "avatar_url": null,
        "git_ssh_url": "git@192.168.64.1:gitlab-org/gitlab-test.git",
        "git_http_url": "http://192.168.64.1:3005/gitlab-org/gitlab-test.git",
        "namespace": "Gitlab Org",
        "visibility_level": 20,
        "path_with_namespace": "gitlab-org/gitlab-test",
        "default_branch": "master"
    },
    "commit": {
        "id": "bcbb5ec396a2c0f828686f14fac9b80b780504f2",
        "message": "test\n",
        "timestamp": "2016-08-12T17:23:21+02:00",
        "url": "http://example.com/gitlab-org/gitlab-test/commit/bcbb5ec396a2c0f828686f14fac9b80b780504f2",
        "author": {
            "name": "User",
            "email": "user@gitlab.com"
        }
    },
    "source_pipeline": {
        "project": {
            "id": 41,
            "web_url": "https://gitlab.example.com/gitlab-org/upstream-project",
            "path_with_namespace": "gitlab-org/upstream-project"
        },
        "pipeline_id": 30,
        "job_id": 3401
    },
    "builds": [
        {
            "id": 376,
            "stage": "build",
            "name": "build-image",
            "status": "success",
            "created_at": "2016-08-12 15:23:28 UTC",
            "started_at": "2016-08-12 15:24:56 UTC",
            "finished_at": "2016-08-12 15:25:26 UTC",
            "duration": 17.0,
            "queued_duration": 196.0,
            "failure_reason": null,
            "when": "on_success",
            "manual": false,
            "allow_failure": false,
            "user": {
                "id": 1,
                "name": "Administrator",
                "username": "root",
                "avatar_url": "http://www.gravatar.com/avatar/e32bd13e2add097461cb96824b7a829c?s=80\u0026d=identicon",
                "email": "admin@example.com"
            },
            "runner": {
                "id": 380987,
                "description": "shared-runners-manager-6.gitlab.com",
                "active": true,
                "runner_type": "instance_type",
                "is_shared": true,
                "tags": [
                    "linux",
                    "docker"
                ]
            },
            "artifacts_file": {
                "filename": null,
                "size": null
            },
            "environment": null
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
