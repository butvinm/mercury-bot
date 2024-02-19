package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Chat.Type;
import com.pengrad.telegrambot.model.ChatMember.Status;
import com.pengrad.telegrambot.model.Update;

import butvinm.mercury.bot.stores.ChatsStore;
import butvinm.mercury.bot.telegram.models.BotChat;
import lombok.Data;

@Data
public class JoinChatHandler implements Handler {
    private final TelegramBot bot;

    private final ChatsStore chatsStore;

    @Override
    public Optional<Object> handleUpdate(
        Update update
    ) throws Exception {
        var myChatMember = update.myChatMember();
        if (myChatMember == null) {
            return Optional.empty();
        }

        var status = myChatMember.newChatMember().status();
        if (status != Status.administrator && status != Status.member) {
            return Optional.empty();
        }

        var chat = myChatMember.chat();
        if (chat.type() == Type.channel) {
            return Optional.empty();
        }
        return Optional.of(handleNewChat(chat));
    }

    private Object handleNewChat(
        Chat chat
    ) throws IOException {
        var oldChat = chatsStore.get(chat.id().toString());
        if (oldChat.isPresent()) {
            return oldChat.get();
        }
        var newChat = BotChat.builder()
            .chatId(chat.id())
            .title(chat.title())
            .bind(false)
            .build();
        return chatsStore.put(chat.id().toString(), newChat);
    }
}
