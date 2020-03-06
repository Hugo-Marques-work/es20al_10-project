package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.apache.tomcat.jni.Local
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
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

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

    @Autowired
    UserService userService

    Course course
    CourseExecution courseExecution
    DateTimeFormatter formatter
    TournamentDto tournamentDto
    Set<TopicDto> topicDtos
    LocalDateTime startingDate
    LocalDateTime conclusionDate

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        TopicDto topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)

        topicDto = topicService.createTopic(course.getId(), topicDto)
        topicDtos = new HashSet<TopicDto>()
        topicDtos.add(topicDto)

        def userDtos = new HashSet<UserDto>()

        tournamentDto = new TournamentDto()

        startingDate = LocalDateTime.now()
        conclusionDate = LocalDateTime.now().plusDays(1)
        tournamentDto.setStartingDate(startingDate.format(formatter))
        tournamentDto.setConclusionDate(conclusionDate.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.addTopics(topicDtos)
        tournamentDto.addUsers(userDtos)
    }

    def topicDtosMatch(set1, set2) {
        def tmpSet = new HashSet<TopicDto>(set2)
        def failed
        for (TopicDto tDto : set1) {
            failed = true
            for (TopicDto otherDto : tmpSet) {
                if (tDto.id == otherDto.id &&
                        tDto.name == otherDto.name &&
                        tDto.parentTopic == otherDto.parentTopic) {
                    tmpSet.remove(otherDto)
                    failed = false
                    break
                }
            }
            if (failed) {
                return false
            }
        }

        return true
    }

    def "create a tournament"() {
        given: "a tournament dto"
        def topics = tournamentDto.getTopics().stream()
                .map({ topicDto -> new Topic(course, topicDto) })
                .collect(Collectors.toSet());

        when:
        def resultDto = tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then: "the returned data is correct"
        topicDtosMatch(resultDto.topics, tournamentDto.topics)
        resultDto.numberOfQuestions == NUMBER_QUESTIONS
        resultDto.startingDate == startingDate.format(formatter)
        resultDto.conclusionDate == conclusionDate.format(formatter)
        and: "the tournament is created"
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result != null
        result.getId() == 1
        result.getStartingDate().format(formatter) == startingDate.format(formatter)
        result.getConclusionDate().format(formatter) == conclusionDate.format(formatter)
        result.getNumberOfQuestions() == NUMBER_QUESTIONS
        result.getTopics() == topics

        //FIXME does it need a new test?
        result.getSignedUpUsers().size() == 0
    }

    def "topic list is empty"() {
        given: "an empty topic list"
        tournamentDto.clearTopicList();

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "invalid topic"() {
        given: "an invalid course"
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

    //FIXME check if it makes sense
    def "user list is not empty"() {
        given: "a null starting date"
        def signUps = new HashSet<>();
        signUps.add(new User())
        tournamentDto.setSignedUpUsers(signUps)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "starting date is null"() {
        given: "a null starting date"
        tournamentDto.setStartingDate(null)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "conclusion date is null"() {
        given: "a null conclusion date"
        tournamentDto.setConclusionDate(null)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "dates overlap"() {
        given: "a conclusion date before a starting date"
        tournamentDto.setConclusionDate(startingDate.minusDays(1).format(formatter))

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "number of questions smaller than 1"() {
        given: "0 questions"
        tournamentDto.setNumberOfQuestions(0)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_CONSISTENT
    }

    def "invalid course execution id"() {
        when:
        tournamentService.createTournament(-1 , tournamentDto)

        then:
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
