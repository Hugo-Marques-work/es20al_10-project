package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.StudentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TournamentDto {
    private Integer id;
    private StudentDto creator;
    private String startingDate = null;
    private String conclusionDate = null;
    private int numberOfQuestions;
    private Set<TopicDto> topics = new HashSet<>();
    private Set<UserDto> signedUpUsers = new HashSet<>();
    private Tournament.Status status;

    public TournamentDto() {}

    public TournamentDto(Tournament tournament, boolean deepCopy) {
        this.id = tournament.getId();
        this.numberOfQuestions = tournament.getNumberOfQuestions();
        this.status = tournament.getStatus();
        if (tournament.getStartingDate() != null)
            this.startingDate = DateHandler.format(tournament.getStartingDate());
        if (tournament.getConclusionDate() != null)
            this.conclusionDate = DateHandler.format(tournament.getConclusionDate());

        if (deepCopy) {
            this.creator = new StudentDto(tournament.getCreator());
            this.topics = tournament.getTopics().stream()
                    .map(TopicDto::new)
                    .collect(Collectors.toSet());
            this.signedUpUsers = tournament.getSignedUpUsers().stream()
                    .map(UserDto::new)
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

    public Set<UserDto> getSignedUpUsers() { return signedUpUsers; }

    public void setSignedUpUsers(Set<UserDto> signedUpUsers) {
        this.signedUpUsers = signedUpUsers;
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

    public void clearTopicList() {
        this.topics.clear();
    }

    public Tournament.Status getStatus() {
        return status;
    }

    public void setStatus(Tournament.Status status) {
        this.status = status;
    }

    public LocalDateTime getStartingDateDate() {
        if (getStartingDate() == null || getStartingDate().isEmpty()) {
            return null;
        }
        return DateHandler.parse(getStartingDate());
    }

    public LocalDateTime getConclusionDateDate() {
        if (getConclusionDate() == null || getConclusionDate().isEmpty()) {
            return null;
        }
        return DateHandler.parse(getConclusionDate());
    }

    public boolean isOpen() {
        return this.status == Tournament.Status.OPEN;
    }

    public StudentDto getCreator() {
        return creator;
    }

    public void setCreator(StudentDto creator) {
        this.creator = creator;
    }
}
