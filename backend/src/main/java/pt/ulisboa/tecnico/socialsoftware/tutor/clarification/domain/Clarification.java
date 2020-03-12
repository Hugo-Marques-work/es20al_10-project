package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CLARIFICATION_IS_EMPTY;

@Entity
@Table(name = "clarifications")
public class Clarification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    public Clarification() {}

    public Clarification(ClarificationDto clarification, Question question, User user) {
        this.user = user;
        this.question = question;

        if (clarification.getContent() == null || clarification.getContent().isEmpty() || clarification.getContent().isBlank())
            throw new TutorException(CLARIFICATION_IS_EMPTY);
        else
            this.content = clarification.getContent();
    }

    public Clarification(String content, Question question, User user) {
        this.user = user;
        this.question = question;

        if (content == null || content.isEmpty() || content.isBlank())
            throw new TutorException(CLARIFICATION_IS_EMPTY);
        else
            this.content = content;
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public Question getQuestion() { return question; }

    public User getUser() { return user; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setQuestion(Question question) { this.question = question; }

    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Clarification{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", user=" + user +
                ", question=" + question +
                '}';
    }
}