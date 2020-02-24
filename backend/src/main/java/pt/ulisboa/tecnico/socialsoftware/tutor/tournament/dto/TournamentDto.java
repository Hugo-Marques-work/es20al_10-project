package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TournamentDto {
    private Integer id;
    private String startingDate;
    private String conclusionDate;
    private int numberOfQuestions;
    private Set<TopicDto> topics = new HashSet<>();

    public TournamentDto() {}

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
}
