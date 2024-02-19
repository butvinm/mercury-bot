package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonPointer;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import butvinm.mercury.bot.stores.FiltersStore;
import butvinm.mercury.bot.telegram.models.BotFilter;
import butvinm.mercury.bot.utils.FancyStringBuilder;
import lombok.Data;

@Data
public class FiltersHandler implements Handler {
    private static final String GUIDE_URL = "https://github.com/butvinm/mercury-bot?tab=readme-ov-file#filters";

    private final TelegramBot bot;

    private final FiltersStore filtersStore;

    @Override
    public Optional<Object> handleUpdate(Update update) throws Exception {
        var message = update.message();
        if (message == null) {
            return Optional.empty();
        }

        var text = message.text();
        if (text == null) {
            return Optional.empty();
        }

        var userFilter = getOrCreateFilter(message.chat().id().toString());

        if (text.startsWith("/add_job_filter")) {
            return Optional.of(handleAddFilter(
                message,
                userFilter.getJobsFilters(),
                userFilter
            ));
        } else if (text.startsWith("/del_job_filter")) {
            return Optional.of(handleDelFilter(
                message,
                userFilter.getJobsFilters(),
                userFilter
            ));
        } else if (text.startsWith("/clear_job_filters")) {
            return Optional.of(handleClearFilter(
                message,
                userFilter.getJobsFilters(),
                userFilter
            ));
        } else if (text.startsWith("/add_pipeline_filter")) {
            return Optional.of(handleAddFilter(
                message,
                userFilter.getPipelinesFilters(),
                userFilter
            ));
        } else if (text.startsWith("/del_pipeline_filter")) {
            return Optional.of(handleDelFilter(
                message,
                userFilter.getPipelinesFilters(),
                userFilter
            ));
        } else if (text.startsWith("/clear_pipeline_filters")) {
            return Optional.of(handleClearFilter(
                message,
                userFilter.getPipelinesFilters(),
                userFilter
            ));
        } else if (text.startsWith("/show_filters")) {
            return Optional.of(handleShowFilters(message, userFilter));
        } else if (text.startsWith("/help_filters")) {
            return Optional.of(handleHelpFilters(message, userFilter));
        }
        return Optional.empty();
    }

    private BotFilter getOrCreateFilter(String chatId) throws IOException {
        var userFilter = filtersStore.get(chatId);
        if (userFilter.isPresent()) {
            return userFilter.get();
        }
        return BotFilter.builder().build();
    }

    private Object handleAddFilter(
        Message message,
        Map<JsonPointer, Pattern> filterSection,
        BotFilter userFilter
    ) throws IOException {
        var args = message.text().split(" ");
        if (args.length < 2) {
            var request = new SendMessage(
                message.chat().id(),
                "Usage: %s {path}={regex}".formatted(args[0])
            );
            return bot.execute(request);
        }

        var arg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        var parts = arg.split("=");
        if (parts.length != 2) {
            var request = new SendMessage(
                message.chat().id(),
                "Wrong format: {path}={regex}"
            );
            return bot.execute(request);
        }

        JsonPointer path;
        try {
            path = JsonPointer.compile(parts[0]);
        } catch (IllegalArgumentException e) {
            var request = new SendMessage(
                message.chat().id(),
                "Wrong path: %s".formatted(e.getMessage())
            );
            return bot.execute(request);
        }

        Pattern regex;
        try {
            regex = Pattern.compile(parts[1]);
        } catch (IllegalArgumentException e) {
            var request = new SendMessage(
                message.chat().id(),
                "Wrong regex: %s".formatted(e.getMessage())
            );
            return bot.execute(request);
        }

        filterSection.put(path, regex);
        filtersStore.put(
            message.from().id().toString(),
            userFilter
        );

        var request = new SendMessage(
            message.chat().id(),
            "Successfully added new filter."
        );
        return bot.execute(request);
    }

    private Object handleDelFilter(
        Message message,
        Map<JsonPointer, Pattern> filterSection,
        BotFilter userFilter
    ) throws IOException {
        var args = message.text().split(" ");
        if (args.length != 2) {
            var request = new SendMessage(
                message.chat().id(),
                "Usage: %s {path}".formatted(args[0])
            );
            return bot.execute(request);
        }

        JsonPointer path;
        try {
            path = JsonPointer.compile(args[1]);
        } catch (IllegalArgumentException e) {
            var request = new SendMessage(
                message.chat().id(),
                "Wrong path: %s".formatted(e.getMessage())
            );
            return bot.execute(request);
        }

        if (filterSection.remove(path) == null) {
            var request = new SendMessage(
                message.chat().id(),
                "Such path not found."
            );
            return bot.execute(request);
        }

        filtersStore.put(
            message.from().id().toString(),
            userFilter
        );

        var request = new SendMessage(
            message.chat().id(),
            "Successfully delete filter."
        );
        return bot.execute(request);
    }

    private Object handleClearFilter(
        Message message,
        Map<JsonPointer, Pattern> filterSection,
        BotFilter userFilter
    ) throws IOException {
        filterSection.clear();
        filtersStore.put(
            message.from().id().toString(),
            userFilter
        );

        var request = new SendMessage(
            message.chat().id(),
            "Successfully clear filter."
        );
        return bot.execute(request);
    }

    private Object handleShowFilters(
        Message message,
        BotFilter userFilter
    ) throws IOException {
        var fsb = new FancyStringBuilder();

        fsb.l("<b>Jobs Filters:</b>");
        userFilter.getJobsFilters().forEach(
            (p, r) -> fsb.l("%s=%s", p, r)
        );

        fsb.n();

        fsb.l("<b>Pipelines Filters:</b>");
        userFilter.getPipelinesFilters().forEach(
            (p, r) -> fsb.l("%s=%s", p, r)
        );

        var request = new SendMessage(
            message.chat().id(),
            fsb.toString()
        ).parseMode(ParseMode.HTML);
        return bot.execute(request);
    }


    private Object handleHelpFilters(
        Message message,
        BotFilter userFilter
    ) throws IOException {
        var request = new SendMessage(
            message.chat().id(),
            "You can find full guide to filters at the <a href=\"%s\">docs</a>".formatted(
                GUIDE_URL
            )
        ).parseMode(ParseMode.HTML);
        return bot.execute(request);
    }
}
