package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class CancelTournamentSpockPerformanceTest extends Specification{
    static final Integer NUMBER_QUESTIONS = 1
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    static final String TOPIC_NAME = "Risk Management"

    @Autowired
    UserRepository userRepository

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TopicService topicService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    UserService userService

    static final String TITLE = "Test Tournament"
    String START_DATE
    String CONCLUSION_DATE

    def setup() {
        START_DATE = DateHandler.format(LocalDateTime.now().plusDays(1))
        CONCLUSION_DATE = DateHandler.format(LocalDateTime.now().plusDays(2))
    }

    def "cancel tournament performance test"() {
        given: "a student"
        def student = new User("Bernardo", "berna", 1, User.Role.STUDENT)
        userRepository.save(student)
        and: "a course execution"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        and: "a topic"
        def topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        topicDto = topicService.createTopic(course.getId(), topicDto)
        and: "a tournament dto"
        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TITLE)
        tournamentDto.setTitle(TITLE)
        tournamentDto.setStartingDate(START_DATE)
        tournamentDto.setConclusionDate(CONCLUSION_DATE)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.addTopic(topicDto)
        and: "1000 tournaments"
        1.upto(100000,  {
            def tournament = new Tournament(student, tournamentDto)
            tournament.setId(it)
            tournamentRepository.save(tournament)
        })

        when:
        def studentId = student.id
        1.upto(100000,  {
            tournamentService.cancelTournament(studentId, it)
        })

        then:
        true
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
