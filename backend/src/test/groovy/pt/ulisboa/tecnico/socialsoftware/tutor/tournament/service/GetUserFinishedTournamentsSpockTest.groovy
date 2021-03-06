package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

@DataJpaTest
class GetUserFinishedTournamentsSpockTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACRONYM2 = "AS12"
    public static final String ACADEMIC_TERM = "1 SEM"

    static final String TOPIC_NAME = "Risk Management"
    static final Integer NUMBER_QUESTIONS = 1

    static final String tournamentName = "TESTNAME";
    static final String tournamentName2 = "WILLNOTSHOWUP";

    @Autowired
    UserRepository userRepository

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicService topicService

    @Autowired
    UserService userService

    Course course
    CourseExecution courseExecution
    CourseExecution otherCourseExecution
    User student
    User student2
    Set<User> studentSet
    TopicDto topicDto

    @Shared
    String START_DATE
    @Shared
    String CONCLUSION_DATE

    @Shared
    LocalDateTime FINISHED_START_DATE
    @Shared
    LocalDateTime FINISHED_CONCLUSION_DATE

    def createValidTournamentDto(String startDate) {
        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(tournamentName2)
        tournamentDto.setStartingDate(startDate)
        tournamentDto.setConclusionDate(CONCLUSION_DATE)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.addTopic(topicDto)
        return tournamentService.createTournament(student.getId(), courseExecution.getId(), tournamentDto)
    }

    def createValidClosedTournament(title) {
        def tournament = new Tournament(student, title,  LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), NUMBER_QUESTIONS)
        tournamentRepository.save(tournament);
        tournament.setStartingDate( FINISHED_START_DATE)
        tournament.setConclusionDate( FINISHED_CONCLUSION_DATE)
        return tournament;
    }

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        otherCourseExecution = new CourseExecution(course, ACRONYM2, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User("Joao", "joao", 1, User.Role.STUDENT)
        userRepository.save(student)
        userService.addCourseExecution(student.getId(),courseExecution.getId());

        student2 = new User("Joaquina", "joaquina", 2, User.Role.STUDENT)
        userRepository.save(student2)
        userService.addCourseExecution(student2.getId(),courseExecution.getId());

        studentSet = new HashSet<User>()
        studentSet.add(student)
        studentSet.add(student2)

        topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        topicDto = topicService.createTopic(course.getId(), topicDto)
    }

    def setupSpec() {
        START_DATE = DateHandler.toISOString(LocalDateTime.now().plusDays(1))
        CONCLUSION_DATE = DateHandler.toISOString(LocalDateTime.now().plusDays(2))
        FINISHED_START_DATE = LocalDateTime.now().minusDays(10)
        FINISHED_CONCLUSION_DATE = LocalDateTime.now().minusDays(9)
    }

    def "2 open tournaments, 3 finished ones"() {
        given: "a valid tournament dto"
        createValidTournamentDto(START_DATE)

        and: " a valid tournament that the user is signed up for"
        def validTournament2 = createValidTournamentDto(START_DATE)
        tournamentService.signUp(student.getId(), validTournament2.getId());

        and: "a tournament dto that has finished that the user participated in"
        def validFinishedTournament = createValidClosedTournament(tournamentName);
        def tournamentSet = new HashSet<Tournament>()
        tournamentSet.add(validFinishedTournament)

        validFinishedTournament.setCourseExecution(courseExecution);
        student.setSignUpTournaments(tournamentSet)
        student2.setSignUpTournaments(tournamentSet)
        validFinishedTournament.setSignedUpUsers(studentSet);
        validFinishedTournament.updateStatus()

        and: "a tournament dto that has finished that the user participated in with another courseExecution"
        def validFinishedTournament2 = createValidClosedTournament(tournamentName2);
        tournamentSet.add(validFinishedTournament2)

        validFinishedTournament2.setCourseExecution(otherCourseExecution);

        student.setSignUpTournaments(tournamentSet)
        student2.setSignUpTournaments(tournamentSet)
        validFinishedTournament2.setSignedUpUsers(studentSet);
        validFinishedTournament2.updateStatus()

        and: "a tournament dto that has finished that the user did not participate in"
        createValidClosedTournament(tournamentName2);

        when:
        def result = tournamentService.getClosedTournaments(student.getId(), courseExecution.getId());

        then: "the returned data is correct"
        result.size() == 1
        def firstTournament = result[0]
        firstTournament.getId() == validFinishedTournament.getId()
        firstTournament.title == validFinishedTournament.getTitle()
    }

    def "invalid user id"() {
        when: "an invalid user id is passed"
        tournamentService.getClosedTournaments(-1,courseExecution.getId());

        then: "an exception is thrown"
        def error = thrown(TutorException)
        error.errorMessage == USER_NOT_FOUND
    }

    def "invalid course execution id"() {
        when: "an invalid course execution is passed"
        tournamentService.getClosedTournaments(student.getId(),-1);

        then: "an exception is thrown"
        def error = thrown(TutorException)
        error.errorMessage == COURSE_EXECUTION_NOT_FOUND
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        TopicService topicService() {
            return new TopicService()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        UserService userService() {
            return new UserService()
        }
    }
}
