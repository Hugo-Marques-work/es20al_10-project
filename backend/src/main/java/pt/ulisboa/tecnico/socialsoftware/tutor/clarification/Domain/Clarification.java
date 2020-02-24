package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.Domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.*;

@Entity
@Table(name = "clarifications")
public class Clarification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String username;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    public Clarification() {}

    public Clarification(ClarificationDto clarification) {
        this.content = clarification.getContent();
        this.id = clarification.getId();
        this.username = clarification.getUsername();
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public Question getQuestion() { return question; }

    public String getUsername() { return username; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setQuestion(Question question) { this.question = question; }

    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        return "Clarification{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", question=" + question +
                '}';
    }
}
