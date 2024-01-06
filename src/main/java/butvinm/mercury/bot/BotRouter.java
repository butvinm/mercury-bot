package butvinm.mercury.bot;

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
import lombok.Data;

@Data
public class BotRouter {
    private final TelegramBot bot;

    private final String targetChatId;

    private final Logger logger;

    public SendResponse handleUpdate(Update update) {
        // if (update.callbackQuery() != null) {
        // var rebuildCallback = RebuildCallback
        // .unpack(update.callbackQuery().data();
        // if (rebuildCallback.isPresent()) {
        // return rebuildPipeline(rebuildCallback.get());
        // }
        // }
        return null;
    }

    public SendResponse handlePipelineEvent(PipelineEvent event) {
        var buildJob = event.getJobs().stream()
            .filter(j -> j.getStage().equals("build"))
            .filter(j -> j.getStatus() == Status.SUCCESS
                || j.getStatus() == Status.FAILED)
            .findFirst();

        if (buildJob.isPresent()) {
            return sendBuildDigest(event, buildJob.get());
        }
        return null;
    }

    // POST /projects/:id/jobs/:job_id/retry

    private SendResponse sendBuildDigest(PipelineEvent event, Job build) {
        var report = "" +
            "Build <code>%d</code> finished.\n\n".formatted(build.getId()) +
            "<b>Status</b>: %s\n".formatted(build.getStatus().getLabel()) +
            "<b>Finished at</b>: %s\n".formatted(build.getFinishedAt()) +
            "<b>Duration</b>: %s s\n".formatted(build.getDuration()) +
            "<b>Create by</b>: %s\n".formatted(build.getUser().getName());

        logger.info(report);

        var callback = new RebuildCallback(
            event.getProject().getId(),
            build.getId()
        );
        var keyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton("Rebuild!").callbackData(callback.pack())
        );
        var request = new SendMessage(targetChatId, report)
            .parseMode(ParseMode.HTML)
            .replyMarkup(keyboard);

        return bot.execute(request);
    }
}
