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
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_NOT_FOUND

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

    def createValidTournamentDto(String startDate) {
        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle("TEST")
        tournamentDto.setStartingDate(startDate)
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
        START_DATE = DateHandler.format(LocalDateTime.now().plusDays(1))
        CONCLUSION_DATE = DateHandler.format(LocalDateTime.now().plusDays(2))
    }

    def "an open tournament and a cancelled one"() {
        given: "two valid tournament dto"
        def validTournament1 = createValidTournamentDto(START_DATE)
        def otherDate =  DateHandler.format(LocalDateTime.now().plusDays(1).plusHours(12))
        def validTournament2 = createValidTournamentDto(otherDate)
        and: "a tournament dto that has been canceled"
        def canceledTournament = createValidTournamentDto(START_DATE)
        tournamentService.cancelTournament(canceledTournament.getId())

        when:
        def result = tournamentService.getOpenTournaments(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 2
        def firstTournament = result[0]
        firstTournament.getId() == validTournament1.getId()
        firstTournament.startingDate == START_DATE
        firstTournament.conclusionDate == CONCLUSION_DATE
        firstTournament.numberOfQuestions == NUMBER_QUESTIONS
        firstTournament.getTopics().getAt(0).name == TOPIC_NAME
        def secondTournament = result[1]
        secondTournament.getId() == validTournament2.getId()
        secondTournament.startingDate == otherDate
        secondTournament.conclusionDate == CONCLUSION_DATE
        secondTournament.numberOfQuestions == NUMBER_QUESTIONS
        secondTournament.getTopics().getAt(0).name == TOPIC_NAME

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
