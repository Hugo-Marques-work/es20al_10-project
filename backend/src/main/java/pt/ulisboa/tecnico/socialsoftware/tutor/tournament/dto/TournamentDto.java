package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TournamentDto {
    private String availableDate;
    private String startingDate;
    private int numberOfQuestions;
    private Set<Topic> topics = new HashSet<>();

    public TournamentDto() {}

    public String getAvailableDate() { return availableDate; }

    public void setAvailableDate(String availableDate) {
        this.availableDate = availableDate;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void addTopics(Collection<Topic> topics) {
        this.topics.addAll(topics);
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
    }
}
