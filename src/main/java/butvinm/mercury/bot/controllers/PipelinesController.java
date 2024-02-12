package butvinm.mercury.bot.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.gitlab.models.Job;
import butvinm.mercury.bot.gitlab.models.PipelineEvent;
import butvinm.mercury.bot.gitlab.models.Status;
import butvinm.mercury.bot.storage.Redis;
import butvinm.mercury.bot.telegram.ChatStore;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
@RequiredArgsConstructor
public class PipelinesController {
    private final TelegramBot bot;

    private final Redis<Long, List<String>> pipelinesMessagesStore;

    private final ChatStore chatStore;

    @PostMapping("/pipelines")
    public SendResponse pipelineHandler(@RequestBody PipelineEvent event) {
        List<Job> buildJobs = event.getJobs().stream()
            .filter(j -> j.getStage().equals("build")).toList();

        var buildFinished = buildJobs.stream()
            .allMatch(j -> Status.isFinished(j.getStatus()));

        if (buildFinished) {
            try {
                return sendBuildDigest(event, buildJobs);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        return null;
    }

    @PostMapping("/pipelines/{pipelineId}/messages")
    public void pipelineMessageHandler(
        @PathVariable Long pipelineId,
        @RequestBody String message
    ) {
        handlePipelineMessage(pipelineId, message);
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

        var messages = pipelinesMessagesStore.remove(
            event.getAttributes().getId()
        );
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
    ) throws IOException {
        var digest = createBuildDigest(event, buildJobs);
        var callback = new RebuildCallback(
            event.getProject().getId(),
            buildJobs.stream().map(j -> j.getId()).toList()
        );
        var keyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton("Rebuild!").callbackData(callback.pack())
        );
        var request = new SendMessage(chatStore.getTargetChat().get(), digest)
            .parseMode(ParseMode.HTML)
            .replyMarkup(keyboard);

        return bot.execute(request);
    }

    public void handlePipelineMessage(Long pipelineId, String message) {
        pipelinesMessagesStore.putIfAbsent(pipelineId, new ArrayList<>());
        pipelinesMessagesStore.get(pipelineId).add(message);
    }
}
