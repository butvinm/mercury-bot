package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.stores.ChatsStore;
import butvinm.mercury.bot.stores.MessagesStore;
import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.callbacks.RebuildCallback;
import butvinm.mercury.bot.utils.FancyStringBuilder;
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

    private final MessagesStore messagesStore;

    private final UsersStore usersStore;

    private final ChatsStore chatsStore;

    @Override
    public Optional<Object> handleUpdate(
        Update update
    ) throws IOException {
        var callbackQuery = update.callbackQuery();
        if (callbackQuery == null) {
            return Optional.empty();
        }

        var rebuildCallback = RebuildCallback.unpack(callbackQuery.data());
        if (rebuildCallback.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(handleRebuildCallback(
            callbackQuery,
            rebuildCallback.get()
        ));
    }

    private SendResponse handleRebuildCallback(
        CallbackQuery query,
        RebuildCallback callback
    ) throws IOException {
        var userId = query.from().id();
        var user = usersStore.get(userId.toString());
        if (user.isPresent() && user.get().getAdmin()) {
            return retryBuildJobs(query, callback);
        }
        return sendPermissionsAlert(query);
    }

    private SendResponse retryBuildJobs(
        CallbackQuery query,
        RebuildCallback callback
    ) throws IOException {
        var fsb = new FancyStringBuilder()
            .l("Retry jobs:");
        for (var jobId : callback.getJobIds()) {
            try {
                var response = glClient.retryJob(
                    callback.getProjectId(),
                    Long.valueOf(jobId)
                );
                log.info(jobId);
                log.info(response.getBody().toPrettyString());
                fsb.l(
                    "<b>%s:</b> %s",
                    jobId,
                    response.isSuccess() ? "OK" : "FAIL"
                );
            } catch (UnirestException err) {
                fsb.l(
                    "<b>%s:</b> %s",
                    jobId,
                    err.toString()
                );
                break;
            }
        }
        var request = new SendMessage(
            query.message().chat().id(),
            fsb.toString()
        ).parseMode(ParseMode.HTML);

        return bot.execute(request);
    }

    private SendResponse sendPermissionsAlert(
        CallbackQuery query
    ) throws IOException {
        var request = new SendMessage(
            query.message().chat().id(),
            "You are not permitted to run this action."
        );
        return bot.execute(request);
    }
}
