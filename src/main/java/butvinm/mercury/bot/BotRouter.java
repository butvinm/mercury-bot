package butvinm.mercury.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.callbacks.RebuildCallback;
import butvinm.mercury.bot.models.Job;
import butvinm.mercury.bot.models.PipelineEvent;
import butvinm.mercury.bot.models.Status;
import kong.unirest.UnirestException;
import lombok.Data;

@Data
public class BotRouter {
    private final TelegramBot bot;

    private final String targetChatId;

    private final GitLabClient gitlabClient;

    private final Redis<Long, List<String>> pipelinesMessagesStore;

    private final Mongo<BotUser> usersStore;

    private final Logger logger;

    public Object handleUpdate(Update update) {
        var callbackQuery = update.callbackQuery();
        if (callbackQuery != null) {
            var rebuildCallback = RebuildCallback.unpack(callbackQuery.data());
            if (rebuildCallback.isPresent()) {
                return handleRebuildCallback(
                    callbackQuery,
                    rebuildCallback.get()
                );
            }
        }
        var message = update.message();
        if (message != null) {
            return handleMessage(message);
        }
        return null;
    }

    public SendResponse handlePipelineEvent(PipelineEvent event) {
        List<Job> buildJobs = event.getJobs().stream()
            .filter(j -> j.getStage().equals("build")).toList();
        var buildFinished = buildJobs.stream()
            .allMatch(j -> Status.isFinished(j.getStatus()));
        if (buildFinished) {
            return sendBuildDigest(event, buildJobs);
        }
        return null;
    }

    public void handlePipelineMessage(Long pipelineId, String message) {
        pipelinesMessagesStore.putIfAbsent(pipelineId, new ArrayList<>());
        pipelinesMessagesStore.get(pipelineId).add(message);
    }

    private BotUser handleMessage(Message message) {
        try {
            var user = new BotUser(
                message.from().id(),
                message.from().username(),
                false
            );
            if (usersStore.get(user.getId().toString()).isPresent()) {
                return null;
            }
            return usersStore.put(user.getId().toString(), user);
        } catch (IOException err) {
            logger.warning(err.toString());
            logger.warning(err.getMessage());
            return null;
        }
    }

    private SendResponse handleRebuildCallback(
        CallbackQuery query,
        RebuildCallback callback
    ) {
        try {
            var userId = query.from().id();
            var user = usersStore.get(userId.toString());
            if (user.isPresent() && user.get().getAdmin()) {
                return retryBuildJobs(callback);
            }
            return sendPermissionsAlert();
        } catch (IOException err) {
            logger.warning(err.getMessage());
            return sendError(err);
        }
    }

    private SendResponse retryBuildJobs(RebuildCallback callback) {
        var report = "Retry jobs:\n";
        for (var jobId : callback.getJobIds()) {
            try {
                var response = gitlabClient.retryJob(
                    callback.getProjectId(), jobId
                );
                logger.info(response.getBody().toPrettyString());
                report += "<b>%s:</b> %s\n".formatted(
                    jobId,
                    response.isSuccess() ? "OK" : "FAIL"
                );
            } catch (UnirestException err) {
                report += "<b>%s:</b> %s\n".formatted(
                    jobId,
                    err.toString()
                );
                break;
            }
        }
        var request = new SendMessage(targetChatId, report)
            .parseMode(ParseMode.HTML);

        return bot.execute(request);
    }

    private SendResponse sendPermissionsAlert() {
        var request = new SendMessage(
            targetChatId,
            "You are not permitted to run this action."
        );
        return bot.execute(request);
    }

    private SendResponse sendError(Exception error) {
        var request = new SendMessage(
            targetChatId,
            "Something went wrong: %s".formatted(error.getMessage())
        );
        return bot.execute(request);
    }

    // TODO: add link to the pipeline
    private String createBuildDigest(
        PipelineEvent event,
        List<Job> buildJobs
    ) {
        var attrs = event.getAttributes();
        var digest = "" +
            "Pipeline <code>%d</code> finished.\n\n".formatted(attrs.getId()) +
            "<b>Finished at</b>: %s\n".formatted(attrs.getFinishedAt()) +
            "<b>Duration</b>: %s s\n".formatted(attrs.getDuration()) +
            "<b>Created by</b>: %s\n\n".formatted(event.getUser().getName()) +
            "<b>Jobs</b>:\n";

        for (var job : buildJobs) {
            digest += "<b>%s:</b> %s\n".formatted(
                job.getName(),
                job.getStatus().getLabel()
            );
        }

        logger.info(pipelinesMessagesStore.toString());
        var messages = pipelinesMessagesStore
            .remove(event.getAttributes().getId());
        if (messages != null) {
            digest += "\n<b>Messages</b>:\n";
            for (var msg : messages) {
                digest += msg + "\n";
            }
        }
        return digest;
    }

    private SendResponse sendBuildDigest(
        PipelineEvent event,
        List<Job> buildJobs
    ) {
        var digest = createBuildDigest(event, buildJobs);
        var callback = new RebuildCallback(
            event.getProject().getId(),
            buildJobs.stream().map(j -> j.getId()).toList()
        );
        var keyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton("Rebuild!").callbackData(callback.pack())
        );
        var request = new SendMessage(targetChatId, digest)
            .parseMode(ParseMode.HTML)
            .replyMarkup(keyboard);

        return bot.execute(request);
    }
}
