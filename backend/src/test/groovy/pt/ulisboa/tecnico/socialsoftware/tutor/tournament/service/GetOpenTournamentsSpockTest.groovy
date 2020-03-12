package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class GetOpenTournamentsSpockTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    static final String TOPIC_NAME = "Risk Management"
    static final Integer NUMBER_QUESTIONS = 1

    @Autowired
    UserRepository userRepository

    @Autowired
    TournamentService tournamentService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicService topicService

    @Shared
    DateTimeFormatter formatter
    @Autowired
    UserService userService

    Course course
    CourseExecution courseExecution
    User student
    TopicDto topicDto

    @Shared
    String START_DATE
    @Shared
    String CONCLUSION_DATE

    def createValidTournamentDto() {
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartingDate(START_DATE)
        tournamentDto.setConclusionDate(CONCLUSION_DATE)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.addTopic(topicDto)
        return tournamentService.createTournament(student.getId(), courseExecution.getId(), tournamentDto)
    }

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User("Joao", "joao", 1, User.Role.STUDENT)
        userRepository.save(student)

        topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        topicDto = topicService.createTopic(course.getId(), topicDto)
    }

    def setupSpec() {
        formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        START_DATE = LocalDateTime.now().plusDays(1).format(formatter)
        CONCLUSION_DATE = LocalDateTime.now().plusDays(2).format(formatter)
    }

    def "an open tournament and a cancelled one"() {
        given: "a valid tournament dto"
        def validTournament = createValidTournamentDto()
        and: "a tournament dto that has been canceled"
        def canceledTournament = createValidTournamentDto()
        tournamentService.cancelTournament(student.getId(), canceledTournament.getId())

        when:
        def result = tournamentService.getOpenTournaments(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 1
        result.get(0).getId() == validTournament.getId()
    }

    def "invalid course execution id"() {
        when: "an invalid course execution id is passed"
        tournamentService.getOpenTournaments(-1)

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
