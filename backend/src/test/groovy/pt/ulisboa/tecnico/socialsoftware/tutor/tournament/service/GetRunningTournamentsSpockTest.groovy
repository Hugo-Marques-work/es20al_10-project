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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_NOT_FOUND

@DataJpaTest
class GetRunningTournamentsSpockTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    static final String TOPIC_NAME = "Risk Management"
    static final String QUESTION_TITLE = "QUESTION"
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
    TopicRepository topicRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserService userService

    @Autowired
    QuizService quizService

    @Autowired
    AnswerService answerService

    @Autowired
    AnswersXmlImport answersXmlImport

    Course course
    CourseExecution courseExecution
    User student
    Topic topic
    Question question

    def createValidTournament() {
        def tournament = new Tournament()
        tournament.setTitle("TEST")
        tournament.setCreator(student)
        tournament.setCourseExecution(courseExecution)
        tournament.setStartingDate(DateHandler.now())
        tournament.setConclusionDate(DateHandler.now().plusDays(2))
        tournament.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournament.addTopic(topicRepository.findAll().getAt(0))
        tournament.addSignUp(student)
        tournament.setStatus(Tournament.Status.OPEN)
        tournamentRepository.save(tournament)
        courseExecution.addTournament(tournament);
        student.addCreatedTournament(tournament);
        return tournament
    }

    def createCanceledTournament() {
        def tournament = new Tournament()
        tournament.setTitle("CANCELED")
        tournament.setStatus(Tournament.Status.CANCELED)
        tournamentRepository.save(tournament)
    }

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User("Joao", "joao", 1, User.Role.STUDENT)
        userRepository.save(student)

        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topicRepository.save(topic)
        course.addTopic(topic)

        question = new Question()
        question.setTitle(QUESTION_TITLE)
        question.setCourse(course)
        question.addTopic(topic)
        question.setStatus(Question.Status.AVAILABLE)
        topic.addQuestion(question)

        questionRepository.save(question)

    }

    def "one running tournament and a cancelled one"() {
        given: "one valid tournament dto"
        def tournament = createValidTournament()
        and: "a tournament dto that has been canceled"
        createCanceledTournament()

        when:
        def result = tournamentService.getRunningTournaments(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 1
        def firstTournament = result[0]
        firstTournament.getId() == tournament.getId()
        firstTournament.getStatus() == Tournament.Status.RUNNING
        and: "the tournament was updated to be running"
        tournament.quiz.quizQuestions.size() == NUMBER_QUESTIONS
        tournament.quiz.quizQuestions.findAll()[0].question.title == QUESTION_TITLE
        tournament.getStatus() == Tournament.Status.RUNNING
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

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }
    }
}
