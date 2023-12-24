package butvinm.mercury.bot;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import butvinm.mercury.bot.models.PipelineEvent;
import butvinm.mercury.bot.models.PipelineStatus;

@SpringBootApplication
@RestController
public class BotApplication {
    private final Logger logger = initLogger();

    private final TelegramBot bot = initBot(System.getenv("BOT_TOKEN"));

    private final String chatId = System.getenv("CHAT_ID");

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @PostMapping("/pipelines")
    public String mergeRequestHandler(
        @RequestBody PipelineEvent event
    ) {
        logger.info(event.toString());
        var eventStatus = event.getObjectAttributes().getStatus();
        if (eventStatus.equals(PipelineStatus.SUCCESS)) {
            return handleSuccessPipeline(event);
        }
        return "none";
    }

    private String handleSuccessPipeline(PipelineEvent event) {
        var projectId = event.getProject().getId();
        var pipelineId = event.getObjectAttributes().getId();
        var pipelineName = event.getObjectAttributes().getName();
        var pipelineTime = event.getObjectAttributes().getFinishedAt();
        var callbackData = "rebuild:%s:%s".formatted(projectId, pipelineId);
        var text = "Pipeline \"%s\" finished successfully at %s"
            .formatted(pipelineName, pipelineTime);

        var keyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton("Rebuild!").callbackData(callbackData)
        );
        SendMessage request = new SendMessage(chatId, text)
            .replyMarkup(keyboard);

        var response = bot.execute(request);
        return response.toString();
    }

    private TelegramBot initBot(String token) {
        return new TelegramBot(token);
    }

    private Logger initLogger() {
        var logger = Logger.getLogger("main");
        try {
            var fileHandler = new FileHandler("/tmp/logs/logs.log");
            logger.addHandler(fileHandler);
            var formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            return logger;
        } catch (SecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
