package butvinm.mercury.bot;

import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;


public class Bot extends TelegramBot {
    private final String chatId;

    public Bot(String botToken, String chatId) {
        super(botToken);
        this.chatId = chatId;
    }

    public SendResponse sendBuildReport(String report, Integer projectId,
        Integer pipelineId) {
        var callbackData = "rebuild:%s:%s".formatted(projectId, pipelineId);
        var keyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton("Rebuild!").callbackData(callbackData)
        );
        SendMessage request = new SendMessage(chatId, report)
            .replyMarkup(keyboard);

        return this.execute(request);
    }

    public Optional<SendResponse> handleUpdate(Update update) {
        return Optional.empty();
    }
}
