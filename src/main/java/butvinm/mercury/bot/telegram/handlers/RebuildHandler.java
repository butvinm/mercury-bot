package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.storage.Mongo;
import butvinm.mercury.bot.storage.Redis;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import butvinm.mercury.bot.telegram.models.BotUser;
import kong.unirest.UnirestException;
import lombok.Data;

/**
 * Handle rebuild pipeline button.
 *
 * Restart all pipeline jobs and send status.
 */
@Data
public class RebuildHandler implements Handler {
    private final TelegramBot bot;

    private final String targetChatId;

    private final GLClient glClient;

    private final Redis<Long, List<String>> pipelinesMessagesStore;

    private final Mongo<BotUser> usersStore;

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

    private SendResponse retryBuildJobs(RebuildCallback callback) {
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
}
