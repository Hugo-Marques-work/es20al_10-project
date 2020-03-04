package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import org.springframework.data.annotation.Transient;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TournamentDto {
    private Integer id;
    private String startingDate = null;
    private String conclusionDate = null;
    private int numberOfQuestions;
    private Set<TopicDto> topics = new HashSet<>();

    @Transient
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TournamentDto() {}

    public TournamentDto(Tournament tournament, boolean deepCopy) {
        this.id = tournament.getId();
        this.numberOfQuestions = tournament.getNumberOfQuestions();

        if (tournament.getStartingDate() != null)
            this.startingDate = tournament.getStartingDate().format(formatter);
        if (tournament.getConclusionDate() != null)
            this.conclusionDate = tournament.getConclusionDate().format(formatter);

        if (deepCopy) {
            this.topics = tournament.getTopics().stream()
                    .map(TopicDto::new)
                    .collect(Collectors.toSet());
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }

    public String getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(String conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public Set<TopicDto> getTopics() {
        return topics;
    }

    public void addTopics(Collection<TopicDto> topics) {
        this.topics.addAll(topics);
    }

    public void addTopic(TopicDto topic) {
        this.topics.add(topic);
    }

    public void removeTopic(TopicDto topic) {
        this.topics.remove(topic);
    }

    public void clearTopicList() {
        this.topics.clear();
    }

    public LocalDateTime getStartingDateDate() {
        if (getStartingDate() == null || getStartingDate().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(getStartingDate(), formatter);
    }

    public LocalDateTime getConclusionDateDate() {
        if (getConclusionDate() == null || getConclusionDate().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(getConclusionDate(), formatter);
    }
}
