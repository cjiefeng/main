package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INDEX;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.RandomMeetingLocationGeneratedEvent;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.EmbedGoogleMaps;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventDate;
import seedu.address.model.event.EventName;

/**
 *  Generates a location for a particular meeting.
 */
public class GenerateLocationCommand extends Command {

    public static final String COMMAND_WORD = "generateLocation";
    public static final String COMMAND_WORD_ALIAS = "gl";

    public static final String MESSAGE_USAGE = COMMAND_WORD + "(alias: " + COMMAND_WORD_ALIAS + ")"
            + ": Generates a location for a specific meeting. "
            + "Parameters: "
            + PREFIX_DATE + "EVENT_DATE "
            + PREFIX_INDEX + "EVENT_INDEX "
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_DATE + "2018-04-01 "
            + PREFIX_INDEX + "1";

    public static final String MESSAGE_EVENT_DOES_NOT_EXIST = "This event does not exist!";

    public static final String MESSAGE_SUCCESS = "Meeting location generated for ";

    private final EventDate meetingLocationEventDate;
    private final Index meetingLocationEventIndex;
    private EventName meetingLocationEventName = null;

    private final Logger logger = LogsCenter.getLogger(getClass());

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public GenerateLocationCommand(EventDate eventDate, Index eventIndex) {
        requireNonNull(eventDate);
        requireNonNull(eventIndex);
        meetingLocationEventDate = eventDate;
        meetingLocationEventIndex = eventIndex;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);

        List<List<Event>> listOfEventListByDate = model.getFilteredEventListByDate();
        boolean isEventFound = false;

        isEventFound = doesEventExist(listOfEventListByDate, isEventFound);

        if (!isEventFound) {
            logger.info("Event was not found. GenerateLocationCommand will not be created.");
            throw new CommandException(MESSAGE_EVENT_DOES_NOT_EXIST);
        }

        logger.info("Event has been found. GenerateLocationCommand to be created.");
        String meetingPlaceId = EmbedGoogleMaps.getMeetingPlaceId();
        EventsCenter.getInstance().post(new RandomMeetingLocationGeneratedEvent(meetingPlaceId));
        return new CommandResult(createFinalSuccessMessage(meetingLocationEventName.toString()));
    }

    /**
     * This method tests if the event for which the location is going to be generated is found in the address book.
     * @param listToCheck The list of list of events (by date) to be checked.
     * @param isEventFound Boolean value that represents if the event has been found.
     * @return Boolean value that indicates if the event has been found in the list of list of events.
     */
    private boolean doesEventExist(List<List<Event>> listToCheck, boolean isEventFound) {
        if (listToCheck.isEmpty() || listToCheck.stream()
                .noneMatch(list -> list.get(0).getEventDate().equals(meetingLocationEventDate))) {
            return isEventFound;
        }

        List<Event> specificDateEventList =
                listToCheck.stream()
                        .filter(list -> list.get(0).getEventDate().equals(meetingLocationEventDate))
                        .collect(Collectors.toList())
                        .get(0);

        if (meetingLocationEventIndex.getZeroBased() >= specificDateEventList.size()) {
            return isEventFound;
        }

        meetingLocationEventName = specificDateEventList.get(meetingLocationEventIndex.getZeroBased()).getEventName();

        isEventFound = true;
        return isEventFound;

    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof GenerateLocationCommand // instanceof handles nulls
                && meetingLocationEventDate.equals(((GenerateLocationCommand) other).meetingLocationEventDate)
                && meetingLocationEventIndex.equals(((GenerateLocationCommand) other).meetingLocationEventIndex));
    }

    /**
     * This method creates the final message on successful completion of the command.
     * @param eventName The name of the event whose location is to be displayed
     * @return
     */
    public String createFinalSuccessMessage(String eventName) {
        return MESSAGE_SUCCESS + eventName
                + "! Use editEventLocation command to change the location of your event if you are happy with this :)";
    }
}
