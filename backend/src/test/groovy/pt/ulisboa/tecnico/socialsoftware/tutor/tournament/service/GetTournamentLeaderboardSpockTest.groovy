package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

@DataJpaTest
class GetTournamentLeaderboardSpockTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACRONYM2 = "AS12"
    public static final String ACADEMIC_TERM = "1 SEM"

    static final String TOPIC_NAME = "Risk Management"
    static final Integer NUMBER_QUESTIONS = 1

    static final String TITLE = "TESTNAME";
    static final String NAME1 = "TestName3";
    static final String NAME2 = "TestName3";
    static final String NAME3 = "TestName3";
    static final String USERNAME1 = "TestUsername1";
    static final String USERNAME2 = "TestUsername2";
    static final String USERNAME3 = "TestUsername3";

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

    def createValidTournament() {
        def tournament = new Tournament(student, TITLE,  LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), NUMBER_QUESTIONS)
        tournament.setCourseExecution(courseExecution)
        tournamentRepository.save(tournament);
        return tournament;
    }

    def finishTournaments(tournamentSet, studentSet) {
        for(Tournament tournament : tournamentSet) {
            tournament.setStartingDate(FINISHED_START_DATE)
            tournament.setConclusionDate(FINISHED_CONCLUSION_DATE)
            tournament.setSignedUpUsers(studentSet)
            tournament.getValidatedStatus()
        }
    }

    def setupUsers() {
        def student1 = new User(NAME1, USERNAME1, 1, User.Role.STUDENT)
        userRepository.save(student1)
        userService.addCourseExecution(student1.getUsername(),courseExecution.getId());

        def student2 = new User(NAME2, USERNAME2, 1, User.Role.STUDENT)
        userRepository.save(student2)
        userService.addCourseExecution(student2.getUsername(),courseExecution.getId());

        def student3 = new User(NAME3, USERNAME3, 1, User.Role.STUDENT)
        userRepository.save(student3)
        userService.addCourseExecution(student3.getUsername(),courseExecution.getId());

        studentSet = new HashSet<User>()
        studentSet.add(student1)
        studentSet.add(student2)
        studentSet.add(student3)
    }

    def studentSetSignedTournaments(tournamentSet) {
        for(User student : studentSet) {
            student.setSignUpTournaments(tournamentSet)
        }
    }

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        otherCourseExecution = new CourseExecution(course, ACRONYM2, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        setupUsers()

        topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        topicDto = topicService.createTopic(course.getId(), topicDto)
    }

    def setupSpec() {
        START_DATE = DateHandler.format(LocalDateTime.now().plusDays(1))
        CONCLUSION_DATE = DateHandler.format(LocalDateTime.now().plusDays(2))
        FINISHED_START_DATE = LocalDateTime.now().minusDays(10)
        FINISHED_CONCLUSION_DATE = LocalDateTime.now().minusDays(9)
    }


    def "Leaderboard OK after quizz generation"() {
        def tournamentSet = new HashSet<Tournament>()
        given: "a valid tournament dto"
        def tournament = createValidTournament()
        tournamentSet.add(tournament)
        and: "students that have signed up for it"
        studentSetSignedTournaments(tournamentSet);

        when:
        finishTournaments(tournamentSet,studentSet);
        def result = tournamentService.getClosedTournaments(studentSet[0].getId(), courseExecution.getId());

        then: "the returned data is correct"
        result.size() == 1
        def firstTournament = result[0]
        firstTournament.getLeaderboard().size() == studentSet.size()
        firstTournament.getLeaderboard().get(new UserDto(studentSet[0]))
        //wrong
    }

    def "Leaderboard OK after answers"() {
        expect:
        true
    }

    def "invalid tournament id"() {
        expect:
        true
    }

    def "2 open tournaments, 3 finished ones"() {
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
