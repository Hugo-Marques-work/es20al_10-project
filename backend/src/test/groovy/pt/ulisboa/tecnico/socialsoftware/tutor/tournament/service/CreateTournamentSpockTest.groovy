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
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

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

    def course
    def courseExecution
    def formatter
    def tournamentDto
    def startingDate
    def conclusionDate
    def topicDtos

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

        tournamentDto = new TournamentDto()

        startingDate = LocalDateTime.now()
        conclusionDate = LocalDateTime.now().plusDays(1)
        tournamentDto.setStartingDate(startingDate.format(formatter))
        tournamentDto.setConclusionDate(conclusionDate.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.addTopics(topicDtos)
    }

    def "create a tournament"() {
        given: "a tournament dto with a topic set similar to the topic dto set"
        def topics = tournamentDto.getTopics().stream()
                .map({ topicDto -> new Topic(course, topicDto) })
                .collect(Collectors.toSet());

        when:
        def resultDto = tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then: "the correct quiz is in the repository and the resulting dto matches"
        resultDto.topics.size() == tournamentDto.topics.size()
        resultDto.numberOfQuestions == NUMBER_QUESTIONS
        resultDto.startingDate == startingDate.format(formatter)
        resultDto.conclusionDate == conclusionDate.format(formatter)

        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getId() == 1
        result.getStartingDate().format(formatter) == startingDate.format(formatter)
        result.getConclusionDate().format(formatter) == conclusionDate.format(formatter)
        result.getNumberOfQuestions() == NUMBER_QUESTIONS
        result.getTopics().equals(topics)
    }

    def "topic list is empty"() {
        given: "an empty topic list"
        tournamentDto.clearTopicList();

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        thrown(TutorException)
    }

    def "starting date is null"() {
        given: "a null starting date"
        tournamentDto.setStartingDate(null)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        thrown(TutorException)
    }

    def "conclusion date is null"() {
        given: "a null conclusion date"
        tournamentDto.setConclusionDate(null)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        thrown(TutorException)
    }

    def "dates overlap"() {
        given: "a conclusion date before a starting date"
        tournamentDto.setConclusionDate(startingDate.minusDays(1).format(formatter))

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        thrown(TutorException)
    }

    def "number of questions smaller than 1"() {
        given: "0 questions"
        tournamentDto.setNumberOfQuestions(0)

        when:
        tournamentService.createTournament(courseExecution.getId(), tournamentDto)

        then:
        thrown(TutorException)
    }

    def "invalid course execution id"() {
        when:
        tournamentService.createTournament(courseExecution.getId() - 10, tournamentDto)

        then:
        thrown(TutorException)
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
    }
}
