package butvinm.mercury.bot.telegram.utils;

import java.util.ArrayList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessagesUtils {
    public static List<SendResponse> spread(
        TelegramBot bot,
        SendMessage message,
        Iterable<?> chatIds
    ) {
        var responses = new ArrayList<SendResponse>();
        for (var chatId : chatIds) {
            try {
                var response = bot.execute(withChatId(message, chatId));
                responses.add(response);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        return responses;
    }

    public static SendMessage withChatId(SendMessage base, Object chatId) {
        var params = base.getParameters();
        var modified = new SendMessage(chatId, (String) params.get("text"));
        if (params.get("parse_mode") != null) {
            modified.parseMode(
                ParseMode.valueOf((String) params.get("parse_mode"))
            );
        }
        if (params.get("entities") != null) {
            modified.entities((MessageEntity) params.get("entities"));
        }
        if (params.get("disable_web_page_preview") != null) {
            modified.disableWebPagePreview(
                (boolean) params.get("disable_web_page_preview")
            );
        }
        if (params.get("message_thread_id") != null) {
            modified.messageThreadId((Integer) params.get("message_thread_id"));
        }
        if (params.get("disable_notification") != null) {
            modified.disableNotification(
                (boolean) params.get("disable_notification")
            );
        }
        if (params.get("reply_to_message_id") != null) {
            modified.replyToMessageId((int) params.get("reply_to_message_id"));
        }
        if (params.get("allow_sending_without_reply") != null) {
            modified.allowSendingWithoutReply(
                (boolean) params.get("allow_sending_without_reply")
            );
        }
        if (params.get("reply_markup") != null) {
            modified.replyMarkup((Keyboard) params.get("reply_markup"));
        }
        if (params.get("protect_content") != null) {
            modified.protectContent((boolean) params.get("protect_content"));
        }
        return modified;
    }
}
