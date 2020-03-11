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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Shared
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class CreateTournamentSpockTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    static final String TOPIC_NAME = "Risk Management"
    static final Integer NUMBER_QUESTIONS = 1

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

    @Shared
    DateTimeFormatter formatter
    @Autowired
    UserService userService

    Course course
    CourseExecution courseExecution

    @Shared
    String START_DATE
    @Shared
    String CONCLUSION_DATE

    def createValidTournamentDto() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        def topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        topicDto = topicService.createTopic(course.getId(), topicDto)
        def topicDtos = new HashSet<TopicDto>(Arrays.asList(topicDto))

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartingDate(START_DATE)
        tournamentDto.setConclusionDate(CONCLUSION_DATE)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.addTopics(topicDtos)
        return tournamentDto
    }

    def setupSpec() {
        formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        START_DATE = LocalDateTime.now().format(formatter)
        CONCLUSION_DATE = LocalDateTime.now().plusDays(1).format(formatter)
    }

    def "create a tournament"() {
        given: "a tournament dto"
        def tournamentDto = createValidTournamentDto()
        and: "a topic list for comparing"
        def topics = tournamentDto.getTopics().stream()
                .map({ topicDto -> new Topic(course, topicDto) })
                .collect(Collectors.toSet());

        when:
        def resultDto = tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then: "the returned data is correct"
        resultDto.topics.size() == 1
        def resultTopicDto = resultDto.topics[0]
        resultTopicDto.name == TOPIC_NAME
        resultTopicDto.id == tournamentDto.topics[0].id
        resultDto.numberOfQuestions == NUMBER_QUESTIONS
        resultDto.startingDate == START_DATE
        resultDto.conclusionDate == CONCLUSION_DATE
        and: "the tournament is created"
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result != null
        result.getId() == 1
        result.getStartingDate().format(formatter) == START_DATE
        result.getConclusionDate().format(formatter) == CONCLUSION_DATE
        result.getNumberOfQuestions() == NUMBER_QUESTIONS
        result.getTopics() == topics
        result.getSignedUpUsers().size() == 0
    }

    def "topic list is empty"() {
        given: "a correct setup but with an empty topic list"
        def tournamentDto = createValidTournamentDto()
        tournamentDto.clearTopicList();

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "invalid topic"() {
        given: "a correct setup and invalid course"
        def tournamentDto = createValidTournamentDto()
        def newCourse = new Course("INVALID", Course.Type.TECNICO)

        courseRepository.save(newCourse)
        and: "a topic that belongs to that course"
        def invalidTopicDto = new TopicDto()
        invalidTopicDto.setName("INVALID")
        invalidTopicDto = topicService.createTopic(newCourse.getId(), invalidTopicDto)
        tournamentDto.addTopic(invalidTopicDto)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    //FIXME: Should we accept tournaments with pre-signedup users?
    def "user list is not empty"() {
        given: "a null starting date"
        def signUps = new HashSet<>();
        signUps.add(new User())
        def tournamentDto = createValidTournamentDto()
        tournamentDto.setSignedUpUsers(signUps)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    @Unroll
    def "invalid arguments: startingDate=#startingDate | conclusionDate=#conclusionDate |\
        numberOfQuestions=#numberOfQuestions | validCourseExecutionId=#validCourseExecutionId ||\
        errorMessage=#errorMessage "() {
        given: "a course and a course execution"
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        and: "a tournament dto with variable arguments"
        def tournamentDto = new TournamentDto()
        tournamentDto.addTopic(new TopicDto().setName(TOPIC_NAME))
        tournamentDto.setStartingDate(startingDate)
        tournamentDto.setConclusionDate(conclusionDate)
        tournamentDto.setNumberOfQuestions(numberOfQuestions)
        print(startingDate)

        when:
        tournamentService.createTournament(getExecutionId(validExecutionId), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        startingDate    | conclusionDate  | numberOfQuestions | validExecutionId || errorMessage
        null            | CONCLUSION_DATE | NUMBER_QUESTIONS  | true             || TOURNAMENT_NOT_CONSISTENT
        START_DATE      | null            | NUMBER_QUESTIONS  | true             || TOURNAMENT_NOT_CONSISTENT
        CONCLUSION_DATE | START_DATE      | NUMBER_QUESTIONS  | true             || TOURNAMENT_NOT_CONSISTENT
        START_DATE      | CONCLUSION_DATE | 0                 | true             || TOURNAMENT_NOT_CONSISTENT
        START_DATE      | CONCLUSION_DATE | NUMBER_QUESTIONS  | false            || COURSE_EXECUTION_NOT_FOUND
    }

    def getExecutionId(valid) {
        return (valid) ? courseExecutionRepository.findAll().get(0).getId() : -1
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
