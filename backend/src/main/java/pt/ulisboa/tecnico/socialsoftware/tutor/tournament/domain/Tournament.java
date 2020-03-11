package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Topic> topics = new HashSet<>();

    private Integer numberOfQuestions;

    private LocalDateTime startingDate;

    private LocalDateTime conclusionDate;

    @ManyToMany
    @Column(name = "user_id")
    private Set<User> signedUpUsers = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "course_execution_id")
    private CourseExecution courseExecution;

    public Tournament() {}

    public Tournament(User creator, TournamentDto tournamentDto) {
        this(creator, tournamentDto.getStartingDateDate(), tournamentDto.getConclusionDateDate(),
                tournamentDto.getNumberOfQuestions());
    }

    public Tournament(User creator,
                      LocalDateTime startDate, LocalDateTime concludeDate,
                      int nQuestions) {
        setCreator(creator);
        setStartingDate( startDate );
        setConclusionDate( concludeDate );

        this.numberOfQuestions = nQuestions;
        if (this.numberOfQuestions < 1) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Number of questions" + this.numberOfQuestions);
        }
    }

    private void setCreator(User creator) {
        if (creator.getRole() != User.Role.STUDENT) {
            throw new TutorException(TOURNAMENT_INVALID_CREATOR,
                    User.Role.STUDENT.toString(), creator.getRole().toString());
        }
        this.creator = creator;
    }

    public Integer getId() { return id; }

    public Set<Topic> getTopics() { return topics; }

    public void addTopic(Topic topic) {
        Set<Topic> validTopics = courseExecution.getCourse().getTopics();

        if (validTopics.stream().noneMatch(other -> topic == other)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Topic" + topic);
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

    public void addSignUp(User user) {
        this.signedUpUsers.add(user);
    }

    public Set<User> getSignedUpUsers() {
        return signedUpUsers;
    }

    public void setSignedUpUsers(Set<User> signedUpUsers) {
        this.signedUpUsers = signedUpUsers;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void checkReadyForSignUp() {
        LocalDateTime currentTime = LocalDateTime.now();

        checkValidTimeForSignUp(currentTime);
    }

    private void checkValidTimeForSignUp(LocalDateTime currentTime) {
        if(currentTime.isBefore(startingDate)) {
            throw new TutorException(TOURNAMENT_SIGN_UP_NOT_READY, this.id);
        }
        else if(currentTime.isAfter(this.conclusionDate)) {
            throw new TutorException(TOURNAMENT_SIGN_UP_OVER, this.id);
        }
    }

    @Override
    public String toString() {
        return "Tournament{" +
                "id=" + id +
                ", creator" + creator +
                ", startingDate=" + startingDate +
                ", conclusionDate=" + conclusionDate +
                ", numberOfQuestions=" + numberOfQuestions +
                ", topics=" + topics +
                '}';
    }
}
