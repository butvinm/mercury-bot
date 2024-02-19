package butvinm.mercury.bot.controllers;

import java.io.IOException;
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

import butvinm.mercury.bot.gitlab.models.JobEvent;
import butvinm.mercury.bot.gitlab.models.Status;
import butvinm.mercury.bot.stores.ChatsStore;
import butvinm.mercury.bot.stores.MessagesStore;
import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import butvinm.mercury.bot.telegram.utils.MessagesUtils;
import butvinm.mercury.bot.utils.FancyStringBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
@RequiredArgsConstructor
public class JobsController {
    private final TelegramBot bot;

    private final MessagesStore messagesStore;

    private final ChatsStore chatsStore;

    private final UsersStore usersStore;

    @PostMapping("/jobs")
    public List<SendResponse> jobHandler(@RequestBody JobEvent job) {
        try {
            return sendJobDigest(job);
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @PostMapping("/jobs/{jobId}/messages")
    public void jobMessageHandler(
        @PathVariable Long jobId,
        @RequestBody String message
    ) {
        messagesStore.putIfAbsent(jobId, new ArrayList<>());
        messagesStore.get(jobId).add(message);
    }

    // TODO: add link to the pipeline
    private String createJobDigest(JobEvent job) {
        var fsb = new FancyStringBuilder()
            .l("Job <code>%s</code>", job.getId()).n()
            .l("<b>Tag</b>: %s", job.getRef())
            .l("<b>State</b>: %s", job.getStatus().getLabel())
            .l("<b>Created by</b>: %s", job.getUser().getName())
            .l("<b>Duration</b>: %s s", job.getDuration());

        if (job.getFinishedAt() != null) {
            fsb.l("<b>Finished at</b>: %s", job.getFinishedAt());
        }
        if (job.getStatus().equals(Status.FAILED)) {
            fsb.l("<b>Failure reason</b>: %s", job.getFailureReason());
        }

        log.info(messagesStore.toString());
        var messages = messagesStore.remove(job.getId());
        if (messages != null) {
            fsb.n().l("<b>Messages</b>:");
            messages.forEach(m -> fsb.l(m));
        }
        return fsb.toString();
    }

    private List<SendResponse> sendJobDigest(JobEvent job) throws IOException {
        var digest = createJobDigest(job);
        var callback = new RebuildCallback(
            job.getProject().getId(),
            List.of(job.getId())
        );
        var keyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton("Rebuild!").callbackData(callback.pack())
        );
        var request = new SendMessage(null, digest)
            .parseMode(ParseMode.HTML)
            .replyMarkup(keyboard);

        var chatsIds = Stream.concat(
            chatsStore.list().values().stream()
                .filter(c -> c.getBind()).map(c -> c.getChatId()),
            usersStore.list().keySet().stream()
        );
        return MessagesUtils.spread(bot, request, chatsIds.toList());
    }
}
