package butvinm.mercury.bot.telegram;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.gitlab.models.Job;
import butvinm.mercury.bot.gitlab.models.PipelineEvent;
import butvinm.mercury.bot.gitlab.models.Status;
import butvinm.mercury.bot.storage.Mongo;
import butvinm.mercury.bot.storage.Redis;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import butvinm.mercury.bot.telegram.handlers.Handler;
import butvinm.mercury.bot.telegram.models.BotUser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class BotRouter {
    private final TelegramBot bot;

    private final String targetChatId;

    private final GLClient glClient;

    private final Redis<Long, List<String>> pipelinesMessagesStore;

    private final Mongo<BotUser> usersStore;

    private final List<Handler> handlers = new LinkedList<>();

    public void register(Handler handler) {
        this.handlers.add(handler);
    }

    public void unregister(Handler handler) {
        this.handlers.remove(handler);
    }

    public List<Object> handleUpdate(Update update) {
        var results = new LinkedList<>();
        for (var handler : handlers) {
            var result = handler.handleUpdate(update);
            if (result.isPresent()) {
                results.add(result.get());
            }
        }
        return results;
    }

    public SendResponse handlePipelineEvent(PipelineEvent event) {
        List<Job> buildJobs = event.getJobs().stream()
            .filter(j -> j.getStage().equals("build")).toList();

        log.info(buildJobs.toString());
        var buildFinished = buildJobs.stream()
            .allMatch(j -> Status.isFinished(j.getStatus()));
        log.info(Boolean.toString(buildFinished));
        if (buildFinished) {
            return sendBuildDigest(event, buildJobs);
        }
        return null;
    }

    public void handlePipelineMessage(Long pipelineId, String message) {
        pipelinesMessagesStore.putIfAbsent(pipelineId, new ArrayList<>());
        pipelinesMessagesStore.get(pipelineId).add(message);
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

        log.info(request.toString());
        var response = bot.execute(request);
        log.error("" + response.errorCode());
        log.error(response.description());
        return response;
    }
}
