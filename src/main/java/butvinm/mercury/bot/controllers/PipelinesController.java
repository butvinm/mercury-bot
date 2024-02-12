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
import butvinm.mercury.bot.stores.ChatStore;
import butvinm.mercury.bot.stores.MessagesStore;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import butvinm.mercury.bot.utils.FancyStringBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
@RequiredArgsConstructor
public class PipelinesController {
    private final TelegramBot bot;

    private final MessagesStore messagesStore;

    private final ChatStore chatStore;

    @PostMapping("/pipelines")
    public SendResponse pipelineHandler(@RequestBody PipelineEvent pipeline) {
        List<Job> buildJobs = pipeline.getJobs().stream()
            .filter(j -> j.getStage().equals("build")).toList();

        var buildFinished = buildJobs.stream()
            .allMatch(j -> Status.isFinished(j.getStatus()));

        if (buildFinished) {
            try {
                return sendPipelineDigest(pipeline, buildJobs);
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
        messagesStore.putIfAbsent(pipelineId, new ArrayList<>());
        messagesStore.get(pipelineId).add(message);
    }

    // TODO: add link to the pipeline
    private String createPipelineDigest(
        PipelineEvent pipeline,
        List<Job> buildJobs
    ) {
        var attrs = pipeline.getAttributes();
        var fsb = new FancyStringBuilder()
            .l("Pipeline <code>%s</code> finished.", attrs.getId()).n()
            .l("<b>Finished at</b>: %s", attrs.getFinishedAt())
            .l("<b>Duration</b>: %s s", attrs.getDuration())
            .l("<b>Created by</b>: %s", pipeline.getUser().getName());

        fsb.n().l("<b>Jobs</b>:");
        for (var job : buildJobs) {
            fsb.l("<b>%s:</b> %s", job.getName(), job.getStatus().getLabel());
        }

        var messages = messagesStore.remove(
            pipeline.getAttributes().getId()
        );
        if (messages != null) {
            fsb.n().l("<b>Messages</b>:");
            messages.forEach(m -> fsb.l(m));
        }
        return fsb.toString();
    }

    private SendResponse sendPipelineDigest(
        PipelineEvent pipeline,
        List<Job> buildJobs
    ) throws IOException {
        var digest = createPipelineDigest(pipeline, buildJobs);
        var callback = new RebuildCallback(
            pipeline.getProject().getId(),
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
}
