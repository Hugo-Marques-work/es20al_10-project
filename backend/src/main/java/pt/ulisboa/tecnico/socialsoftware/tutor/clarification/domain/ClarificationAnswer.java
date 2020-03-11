package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY;

@Entity
@Table(name = "clarification_answers")
public class ClarificationAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "clarification_id")
    private Clarification clarification;

    public ClarificationAnswer() {}

    public ClarificationAnswer(String content, Clarification clarification, User user) {
        this.user = user;
        this.clarification = clarification;

        if (content == null
                || content.isEmpty()
                || content.isBlank())
            throw  new TutorException(CLARIFICATION_ANSWER_IS_EMPTY);
        else
            this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Clarification getClarification() {
        return clarification;
    }

    public void setClarification(Clarification clarification) {
        this.clarification = clarification;
    }

    @Override
    public String toString() {
        return "ClarificationAnswer{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", user=" + user +
                ", clarification=" + clarification +
                '}';
    }
}
