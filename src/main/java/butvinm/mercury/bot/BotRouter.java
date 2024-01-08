package butvinm.mercury.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.pengrad.telegrambot.TelegramBot;
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

    private final Logger logger;

    private final Redis<Long, List<String>> pipelinesMessages = new Redis<>();

    public SendResponse handleUpdate(Update update) {
        if (update.callbackQuery() != null) {
            var rebuildCallback = RebuildCallback
                .unpack(update.callbackQuery().data());
            if (rebuildCallback.isPresent()) {
                return retryBuildJobs(rebuildCallback.get());
            }
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
        pipelinesMessages.putIfAbsent(pipelineId, new ArrayList<>());
        pipelinesMessages.get(pipelineId).add(message);
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

        logger.info(pipelinesMessages.toString());
        var messages = pipelinesMessages.remove(event.getAttributes().getId());
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
