package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class GetClarificationsSpockTest extends Specification{
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer KEY = 1
    static final User.Role ROLE = User.Role.STUDENT
    static final String CONTENT = "I want a clarification in this question."
    static final String CONTENT_AVAILABLE = "I want a clarification available in this question."
    static final String COURSE_NAME = "Software Architecture"
    static final int NON_EXISTING_ID = 1000

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    ClarificationRepository clarificationRepository

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository

    @Shared
    User user
    Question question
    Course course
    Clarification clarification

    def setup() {
        user = new User(NAME, USERNAME, ROLE)
        userRepository.save(user)
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        question = new Question()
        question.setKey(KEY)
        question.setTitle("Title")
        question.setCourse(course)
        questionRepository.save(question)
        course.addQuestion(question)
        clarification = new Clarification(CONTENT, question, user)
        clarification.setAvailability(Clarification.Availability.BOTH)
        clarificationRepository.save(clarification)
        user.addClarification(clarification)
        question.addClarification(clarification)
    }

    def "User exists and returns clarifications with no answers" () {

        when:
        def result = clarificationService.getClarificationsByUser(user.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
        !result.get(0).answered
    }

    def "Question exists and returns clarifications with no answers" () {
        when:
        def result = clarificationService.getClarificationsByQuestion(question.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
        !result.get(0).answered
    }

    def "Course exists and returns clarifications with no answers" () {
        when:
        def result = clarificationService.getClarificationsByCourse(course.getId(), User.Role.TEACHER)

        then:
        result.get(0).content == CONTENT
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
        !result.get(0).answered
    }

    def "User exists and returns clarifications with answers" () {
        given: "a teacher"
        def teacher = new User(NAME, "test", User.Role.TEACHER)
        userRepository.save(teacher)
        and: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, teacher)
        clarificationAnswerRepository.save(clarificationAnswer)
        clarification.addClarificationAnswer(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationsByUser(user.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
        result.get(0).answered
    }

    def "Question exists and returns clarifications with answers" () {
        given: "a teacher"
        def teacher = new User(NAME, "test", User.Role.TEACHER)
        userRepository.save(teacher)
        and: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, teacher)
        clarificationAnswerRepository.save(clarificationAnswer)
        clarification.addClarificationAnswer(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationsByQuestion(question.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
        result.get(0).answered
    }

    def "Course exists and returns clarifications with answers" () {
        given: "a teacher"
        def teacher = new User(NAME, "test", User.Role.TEACHER)
        userRepository.save(teacher)
        and: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, teacher)
        clarificationAnswerRepository.save(clarificationAnswer)
        clarification.addClarificationAnswer(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationsByCourse(course.getId(), User.Role.TEACHER)

        then:
        result.get(0).content == CONTENT
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
        result.get(0).answered
    }

    def "Course exists and has two clarifications, only one is with full availability, student only sees one" () {
        given: "a clarification not available"
        clarification.setAvailability(Clarification.Availability.NONE)

        and: "a clarification available"
        def clarificationAvailable = new Clarification(CONTENT_AVAILABLE, question, user)
        clarificationAvailable.setAvailability(Clarification.Availability.BOTH)
        question.addClarification(clarificationAvailable)
        clarificationRepository.save(clarificationAvailable)

        when:
        def result = clarificationService.getClarificationsByCourse(course.getId(), ROLE.STUDENT)

        then:
        result.size() == 1
        result.get(0).content == clarificationAvailable.content
        result.get(0).content != clarification.content
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
    }

    def "Course exists and has two clarifications, only one is with student availability, student only sees one" () {
        given: "a clarification not available"
        clarification.setAvailability(Clarification.Availability.NONE)

        and: "a clarification available"
        def clarificationAvailable = new Clarification(CONTENT_AVAILABLE, question, user)
        clarificationAvailable.setAvailability(Clarification.Availability.STUDENT)
        clarificationRepository.save(clarificationAvailable)
        question.addClarification(clarificationAvailable)

        when:
        def result = clarificationService.getClarificationsByCourse(course.getId(), ROLE.STUDENT)

        then:
        result.size() == 1
        result.get(0).content == clarificationAvailable.content
        result.get(0).content != clarification.content
        result.get(0).question.getId() == question.getId()
        result.get(0).user.getId() == user.getId()
    }

    def "Course exists and has two clarifications, only one is with teacher availability, student only sees one" () {
        given: "a clarification not available"
        clarification.setAvailability(Clarification.Availability.NONE)

        and: "a clarification available"
        def clarificationAvailable = new Clarification(CONTENT_AVAILABLE, question, user)
        clarificationAvailable.setAvailability(Clarification.Availability.TEACHER)
        clarificationRepository.save(clarificationAvailable)
        question.addClarification(clarificationAvailable)

        when:
        def result = clarificationService.getClarificationsByCourse(course.getId(), ROLE.STUDENT)

        then:
        result.size() == 1
        result.get(0).content == clarificationAvailable.content
        result.get(0).content != clarification.content
        result.get(0).question.getId() == question.getId()
        result.get(0).user == null
    }

    def "Course exists and has two clarifications, none is available, student doesn't see anything" () {
        given: "a clarification not available"
        clarification.setAvailability(Clarification.Availability.NONE)

        and: "a clarification available"
        def clarificationNotAvailable = new Clarification(CONTENT, question, user)
        clarificationNotAvailable.setAvailability(Clarification.Availability.NONE)
        clarificationRepository.save(clarificationNotAvailable)
        question.addClarification(clarificationNotAvailable)

        when:
        def result = clarificationService.getClarificationsByCourse(course.getId(), ROLE.STUDENT)

        then:
        result.size() == 0
    }

    def "User does not exist and a an error is returned" () {
        when:
        clarificationService.getClarificationsByUser(NON_EXISTING_ID)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.USER_NOT_FOUND
    }

    def "Question does not exist and an error is returned" () {
        when:
        clarificationService.getClarificationsByQuestion(NON_EXISTING_ID)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.QUESTION_NOT_FOUND
    }

    def "Course does not exist and an error is returned" () {
        when:
        clarificationService.getClarificationsByCourse(NON_EXISTING_ID, User.Role.TEACHER)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.COURSE_NOT_FOUND
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }

        @Bean
        UserService userService() {
            return new UserService()
        }

        @Bean
        CourseService courseService() {
            return new CourseService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        TopicService topicService() {
            return new TopicService()
        }

        @Bean
        AssessmentService assessmentService() {
            return new AssessmentService()
        }
    }
}
