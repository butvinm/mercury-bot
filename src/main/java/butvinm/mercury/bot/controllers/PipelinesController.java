package butvinm.mercury.bot.controllers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
import butvinm.mercury.bot.stores.ChatsStore;
import butvinm.mercury.bot.stores.FiltersStore;
import butvinm.mercury.bot.stores.MessagesStore;
import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import butvinm.mercury.bot.telegram.utils.MessagesUtils;
import butvinm.mercury.bot.utils.FancyStringBuilder;
import butvinm.mercury.bot.utils.filter.ObjectFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
@RequiredArgsConstructor
public class PipelinesController {
    private final TelegramBot bot;

    private final MessagesStore messagesStore;

    private final ChatsStore chatsStore;

    private final UsersStore usersStore;

    private final FiltersStore filtersStore;

    @PostMapping("/pipelines")
    public List<SendResponse> pipelineHandler(
        @RequestBody PipelineEvent pipeline
    ) {
        log.info(pipeline.toString());
        var finishedJobs = pipeline.getJobs().stream()
            .allMatch(j -> Status.isFinished(j.getStatus()));

        if (finishedJobs) {
            try {
                return sendPipelineDigest(pipeline, pipeline.getJobs());
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
            .l("<b>Tag</b>: %s", attrs.getRef())
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

    private Boolean filterPipeline(
        String chatId,
        PipelineEvent job
    ) throws IOException {
        var patterns = filtersStore.get(chatId);
        if (patterns.isEmpty()) {
            return true;
        }

        var filter = new ObjectFilter(
            patterns.get().getPipelinesFilters()
        );
        return filter.test(job);
    }

    private List<SendResponse> sendPipelineDigest(
        PipelineEvent pipeline,
        List<Job> jobs
    ) throws IOException {
        var digest = createPipelineDigest(pipeline, jobs);
        var callback = new RebuildCallback(
            pipeline.getProject().getId(),
            jobs.stream().map(j -> j.getId()).toList()
        );
        var keyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton("Rebuild!").callbackData(callback.pack())
        );
        var request = new SendMessage(null, digest)
            .parseMode(ParseMode.HTML)
            .replyMarkup(keyboard);

        var chatsIds = chatsStore.list().values().stream()
            .filter(c -> c.getBind())
            .map(c -> c.getChatId());

        var usersIds = usersStore.list().values().stream()
            .filter(u -> {
                try {
                    return filterPipeline(u.getChatId().toString(), pipeline);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .map(u -> u.getChatId());

        return MessagesUtils.spread(
            bot,
            request,
            Stream.concat(chatsIds, usersIds).toList()
        );
    }
}
