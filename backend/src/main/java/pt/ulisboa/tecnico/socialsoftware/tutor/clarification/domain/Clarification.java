package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CLARIFICATION_IS_EMPTY;

@Entity
@Table(name = "clarifications")
public class Clarification {
    public enum Availability {NONE, TEACHER, STUDENT, BOTH}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isAnswered;

    @Column
    private Availability availability;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "clarification")
    private List<ClarificationAnswer> clarificationAnswers = new ArrayList<>();

    public Clarification() {}

    public Clarification(String content, Question question, User user) {
        this.user = user;
        this.question = question;
        this.isAnswered = false;
        this.availability = Availability.NONE;

        if (content == null || content.isEmpty() || content.isBlank())
            throw new TutorException(CLARIFICATION_IS_EMPTY);
        else
            this.content = content;
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public Question getQuestion() { return question; }

    public User getUser() { return user; }

    public boolean isAnswered() { return isAnswered; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setQuestion(Question question) { this.question = question; }

    public void setUser(User user) { this.user = user; }

    public List<ClarificationAnswer> getClarificationAnswers() { return clarificationAnswers; }

    public void setClarificationAnswers(List<ClarificationAnswer> clarificationAnswers) { this.clarificationAnswers = clarificationAnswers; }

    public void setIsAnswered(boolean status) { this.isAnswered = status; }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public void makeAvailableTeacher() {
        if (this.availability == null)
            this.availability = Availability.NONE;
        if (this.availability == Availability.NONE)
            this.availability = Availability.TEACHER;
        else if (this.availability == Availability.STUDENT)
            this.availability = Availability.BOTH;
    }

    public void setAvailabilityStudent(boolean available) {
        if (this.availability == null)
            this.availability = Availability.NONE;
        if (this.availability == Availability.NONE && available)
            this.availability = Availability.STUDENT;
        else if (this.availability == Availability.TEACHER && available)
            this.availability = Availability.BOTH;
        else if (this.availability == Availability.STUDENT && !available)
            this.availability = Availability.NONE;
        else if (this.availability == Availability.BOTH && !available)
            this.availability = Availability.TEACHER;
    }

    public void addClarificationAnswer(ClarificationAnswer clarificationAnswer) {
        if (!isAnswered && clarificationAnswer.getUser().getId() != this.user.getId()) isAnswered = true;
        else if (isAnswered && clarificationAnswer.getUser().getId() == this.user.getId()) isAnswered = false;
        this.clarificationAnswers.add(0, clarificationAnswer);
    }

    public void remove() {
        user.getClarifications().remove(this);
        question.getClarifications().remove(this);
    }

    @Override
    public String toString() {
        return "Clarification{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", isAnswered=" + isAnswered +
                ", user=" + user +
                ", question=" + question +
                ", clarificationAnswers=" + clarificationAnswers +
                '}';
    }
}
