package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.stores.ChatStore;
import butvinm.mercury.bot.stores.MessagesStore;
import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import kong.unirest.UnirestException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Handle rebuild pipeline button.
 *
 * Restart all pipeline jobs and send status.
 */
@Data
@Slf4j
public class RebuildHandler implements Handler {
    private final TelegramBot bot;

    private final GLClient glClient;

    private final MessagesStore pipelinesMessagesStore;

    private final UsersStore usersStore;

    private final ChatStore chatStore;

    @Override
    public Optional<Object> handleUpdate(Update update) {
        var callbackQuery = update.callbackQuery();
        if (callbackQuery != null) {
            var rebuildCallback = RebuildCallback.unpack(callbackQuery.data());
            if (rebuildCallback.isPresent()) {
                return Optional.of(handleRebuildCallback(
                    callbackQuery,
                    rebuildCallback.get()
                ));
            }
        }
        return Optional.empty();
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
            return sendError(err);
        }
    }

    private SendResponse retryBuildJobs(RebuildCallback callback)
        throws IOException {
        var report = "Retry jobs:\n";
        for (var jobId : callback.getJobIds()) {
            try {
                var response = glClient.retryJob(
                    callback.getProjectId(), jobId
                );
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
        var request = new SendMessage(
            chatStore.getTargetChat().get(),
            report
        ).parseMode(ParseMode.HTML);
        return bot.execute(request);
    }

    private SendResponse sendPermissionsAlert() throws IOException {
        var request = new SendMessage(
            chatStore.getTargetChat().get(),
            "You are not permitted to run this action."
        );
        return bot.execute(request);
    }

    private SendResponse sendError(Exception error) {
        try {
            var targetChat = chatStore.getTargetChat().get();
            var request = new SendMessage(
                targetChat,
                "Something went wrong: %s".formatted(error.getMessage())
            );
            return bot.execute(request);
        } catch (IOException | NoSuchElementException e) {
            log.error(e.toString());
            return null;
        }
    }
}
