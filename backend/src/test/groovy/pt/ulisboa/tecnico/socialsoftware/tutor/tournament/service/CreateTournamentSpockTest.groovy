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
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class CreateTournamentSpockTest extends Specification {
    public static final enum userType {
        STUDENT,
        TEACHER,
        INEXISTENT
    }

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
    User student

    @Shared
    String START_DATE
    @Shared
    String CONCLUSION_DATE

    def createValidTournamentDto() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User("Joao", "joao", 1, User.Role.STUDENT)
        userRepository.save(student)

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
        START_DATE = LocalDateTime.now().plusDays(1).format(formatter)
        CONCLUSION_DATE = LocalDateTime.now().plusDays(2).format(formatter)
    }

    def "create a tournament"() {
        given: "a tournament dto"
        def tournamentDto = createValidTournamentDto()
        and: "a topic list for comparing"
        def topics = tournamentDto.getTopics().stream()
                .map({ topicDto -> new Topic(course, topicDto) })
                .collect(Collectors.toSet());

        when:
        def resultDto = tournamentService.createTournament(student.getId(), courseExecution.getId(), tournamentDto)

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
        courseExecution.getTournaments().size() == 1
        student.getCreatedTournaments().size() == 1
        result != null
        result.getId() == resultDto.getId()
        result.getStatus() == resultDto.getStatus()
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
        tournamentService.createTournament(student.getId(), courseExecution.getId(), tournamentDto)

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
        tournamentService.createTournament(student.getId(), courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "sign up list is not empty"() {
        given: "a valid tournament Dto"
        def tournamentDto = createValidTournamentDto()
        and: "a non empty sign up list"
        def signUps = new HashSet<>()
        signUps.add(new User())
        tournamentDto.setSignedUpUsers(signUps)

        when:
        tournamentService.createTournament(student.getId(), courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "sign up list is null"() {
        given: "a valid tournament Dto"
        def tournamentDto = createValidTournamentDto()
        and: "a non empty sign up list"
        tournamentDto.setSignedUpUsers(null)

        when:
        tournamentService.createTournament(student.getId(), courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    @Unroll
    def "invalid arguments: startingDate=#startingDate | conclusionDate=#conclusionDate |\
        numberOfQuestions=#numberOfQuestions | validExecutionId=#validExecutionId ||\
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

        when:
        tournamentService.createTournament(getCreatorId(userType.STUDENT), getExecutionId(validExecutionId),
                tournamentDto)

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

    @Unroll
    def "invalid creator: creatorType=#creatorType"() {
        given: "a valid tournament dto"
        def tournamentDto = createValidTournamentDto()

        when:
        tournamentService.createTournament(getCreatorId(creatorType), getExecutionId(true), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        creatorType         || errorMessage
        userType.TEACHER    || TOURNAMENT_INVALID_CREATOR
        userType.INEXISTENT || USER_NOT_FOUND

    }

    def getExecutionId(valid) {
        return (valid) ? courseExecutionRepository.findAll().get(0).getId() : -1
    }

    def getCreatorId(type) {
        switch (type) {
            case userType.STUDENT:
                def student = new User("student", "student", 1, User.Role.STUDENT)
                userRepository.save(student)
                return student.getId()
            case userType.TEACHER:
                def teacher = new User("prof", "prof", 2, User.Role.TEACHER)
                userRepository.save(teacher)
                return teacher.getId()
            default:
                return -1
        }
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
