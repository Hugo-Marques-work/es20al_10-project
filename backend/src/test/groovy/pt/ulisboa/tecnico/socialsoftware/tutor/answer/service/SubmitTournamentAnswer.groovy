package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class SubmitTournamentAnswer extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    @Autowired
    AnswerService answerService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    OptionRepository optionRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    def user
    def courseExecution
    def quizQuestion
    def optionOk
    def optionKO
    def quizAnswer
    def date
    def quiz
    def timeBefore

    def createCorrectOption(){
        optionOk = new Option()
        optionOk.setCorrect(true)
        optionOk.setSequence(1)
        optionOk.setContent("RIGHT")
    }

    def createIncorrectOption(){
        optionKO = new Option()
        optionKO.setCorrect(false)
        optionKO.setSequence(0)
        optionKO.setContent("WRONG")
    }

    def setup() {
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)

        courseExecutionRepository.save(courseExecution)

        user = new User('name', "username", 1, User.Role.STUDENT)
        user.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user)

        quiz = new Quiz()
        quiz.setType(Quiz.QuizType.TOURNAMENT.toString())
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)

        quizAnswer = new QuizAnswer(user, quiz)

        def question = new Question()
        question.setCourse(course)
        course.addQuestion(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        createIncorrectOption()
        question.addOption(optionKO)
        question.setTitle("Question Test")
        createCorrectOption()
        question.addOption(optionOk)

        date = LocalDateTime.now()

        userRepository.save(user)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        quizAnswerRepository.save(quizAnswer)
        optionRepository.save(optionOk)
        optionRepository.save(optionKO)

        timeBefore = DateHandler.now();
    }

    def 'submit a correct statementAnswer'() {
        given: 'a statementAnswer'
        def questionAnswer = new QuestionAnswer(quizAnswer,quizQuestion,0)
        questionAnswer.setTimeTaken()
        questionAnswer.setOption(optionOk)
        def statementAnswer = new StatementAnswerDto(questionAnswer)
        statementAnswer.setTimeTaken(10)

        when:'the answer is submitted'
        def correctAnswer = answerService.submitTournamentAnswer(user, quiz.getId(), statementAnswer)

        then: 'the answer is correct'
        correctAnswer
        questionAnswer.getOption() == optionOk
        questionAnswer.getTimeTaken() == 10
        quizAnswer.getAnswerDate().isAfter(timeBefore)
    }

    def 'a user tries to submit an answer two times'() {
        given: 'a statementAnswer is submited'
        def questionAnswer = new QuestionAnswer(quizAnswer,quizQuestion,0)
        questionAnswer.setTimeTaken()
        questionAnswer.setOption(optionOk)
        def statementAnswer = new StatementAnswerDto(questionAnswer)
        statementAnswer.setTimeTaken(10)
        answerService.submitTournamentAnswer(user, quiz.getId(), statementAnswer)

        when:'the answer is submitted again'
        answerService.submitTournamentAnswer(user, quiz.getId(), statementAnswer)

        then: "an exception is thrown"
        def error = thrown(TutorException)
        error.errorMessage == QUESTION_ALREADY_ANSWERED
    }

    @TestConfiguration
    static class AnswerServiceImplTestContextConfiguration {

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