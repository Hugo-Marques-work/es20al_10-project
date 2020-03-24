package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class GetClarificationsSpockPerformanceTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer KEY = 1
    static final User.Role ROLE = User.Role.STUDENT
    static final String CONTENT = "I want a clarification in this question."
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
        user = new User(NAME, USERNAME, KEY, ROLE)
        userRepository.save(user)
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        question = new Question()
        question.setKey(KEY)
        question.setCourse(course)
        questionRepository.save(question)
        course.addQuestion(question)
        clarification = new Clarification(CONTENT, question, user)
        clarificationRepository.save(clarification)
        user.addClarification(clarification)
        question.addClarification(clarification)
    }

    def "performance test to get 10000 clarifications by user" () {
        when:
        1.upto(10000, {clarificationService.getClarificationsByUser(user.getId())})

        then:
        true
    }

    def "performance test to get 10000 clarifications by question" () {
        when:
        1.upto(10000, {clarificationService.getClarificationsByQuestion(question.getId())})

        then:
        true
    }

    def "performance test to get 10000 clarifications by course" () {
        when:
        1.upto(10000, {clarificationService.getClarificationsByCourse(course.getId())})

        then:
        true
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
