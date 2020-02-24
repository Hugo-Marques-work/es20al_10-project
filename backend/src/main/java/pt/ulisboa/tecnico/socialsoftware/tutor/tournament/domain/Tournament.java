package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Topic> topics = new HashSet<>();

    private Integer numberOfQuestions;

    private LocalDateTime startingDate;

    private LocalDateTime conclusionDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    public Tournament() {}

    public Integer getId() { return id; }

    public Set<Topic> getTopics() { return topics; }

    public Integer getNumberOfQuestions() { return numberOfQuestions; }

    public LocalDateTime getStartingDate() { return startingDate; }

    public LocalDateTime getConclusionDate() { return conclusionDate; }

    public Quiz getQuiz() { return quiz; }
}
