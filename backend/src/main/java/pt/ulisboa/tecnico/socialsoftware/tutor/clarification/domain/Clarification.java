package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

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
        this.content = clarification.getContent();
        this.id = clarification.getId();
        this.user = user;
        this.question = question;
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
