package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import net.bytebuddy.implementation.bytecode.Throw;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_CONSISTENT;

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

    @ManyToOne
    @JoinColumn(name = "course_execution_id")
    private CourseExecution courseExecution;

    public Tournament() {}

    public Tournament(TournamentDto tournamentDto) {
        setStartingDate(tournamentDto.getStartingDateDate());
        setConclusionDate(tournamentDto.getConclusionDateDate());

        this.numberOfQuestions = tournamentDto.getNumberOfQuestions();
        if (this.numberOfQuestions < 1) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Number of questions" + this.numberOfQuestions);
        }
    }

    public Integer getId() { return id; }

    public Set<Topic> getTopics() { return topics; }

    public void addTopic(Topic topic) {
        Set<Topic> validTopics = courseExecution.getCourse().getTopics();

        if (!validTopics.stream().anyMatch(other -> topic == other)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Invalid topic" + topic);
        }
        topics.add(topic);
    }

    public Integer getNumberOfQuestions() { return numberOfQuestions; }

    public LocalDateTime getStartingDate() { return startingDate; }

    public void setStartingDate(LocalDateTime startingDate) {
        checkStartingDate(startingDate);
        this.startingDate = startingDate;
    }

    public LocalDateTime getConclusionDate() { return conclusionDate; }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        checkConclusionDate(conclusionDate);
        this.conclusionDate = conclusionDate;
    }

    public Quiz getQuiz() { return quiz; }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    void checkStartingDate(LocalDateTime startingDate) {
        if (startingDate == null) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Starting date" + startingDate);
        }
        if (this.conclusionDate != null && conclusionDate.isBefore(startingDate)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Starting date" + startingDate + conclusionDate);
        }
    }

    void checkConclusionDate(LocalDateTime conclusionDate) {
        if (conclusionDate == null) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Conclusion date " + conclusionDate);
        }
        if (startingDate != null && conclusionDate.isBefore(startingDate)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Conclusion date " + conclusionDate + startingDate);
        }
    }
}