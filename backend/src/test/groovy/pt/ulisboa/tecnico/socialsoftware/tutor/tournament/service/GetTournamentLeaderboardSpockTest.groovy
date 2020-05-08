package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.UserBoardPlace
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.UserBoardPlaceDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

@DataJpaTest
class GetTournamentLeaderboardSpockTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACRONYM2 = "AS12"
    public static final String ACADEMIC_TERM = "1 SEM"

    static final String TOPIC_NAME = "Risk Management"
    static final Integer NUMBER_QUESTIONS = 5

    static final String TITLE = "TESTNAME";
    static final String NAME1 = "TestName3";
    static final String NAME2 = "TestName3";
    static final String NAME3 = "TestName3";
    static final String NAME4 = "TestName4";
    static final String USERNAME1 = "TestUsername1";
    static final String USERNAME2 = "TestUsername2";
    static final String USERNAME3 = "TestUsername3";
    static final String USERNAME4 = "TestUsername4";

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

    @Autowired
    UserService userService

    @Autowired
    OptionRepository optionRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    Course course
    CourseExecution courseExecution
    CourseExecution otherCourseExecution
    Set<User> studentSet
    TopicDto topicDto

    Option optionOK1
    Option optionOK2

    Question question1
    Question question2


    @Shared
    String START_DATE
    @Shared
    String CONCLUSION_DATE

    @Shared
    LocalDateTime FINISHED_START_DATE
    @Shared
    LocalDateTime FINISHED_CONCLUSION_DATE

    def createValidTournament() {
        def tournament = new Tournament(studentSet[0], TITLE,  LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), NUMBER_QUESTIONS)
        tournament.setCourseExecution(courseExecution)
        tournamentRepository.save(tournament);

        tournament.setQuiz(new Quiz())
        def quizQuestion1 = new QuizQuestion(tournament.getQuiz(),question1,0)
        def quizQuestion2 = new QuizQuestion(tournament.getQuiz(),question2,0)
        quizQuestionRepository.save(quizQuestion1)
        quizQuestionRepository.save(quizQuestion2)
        return tournament;
    }

    def createValidQuizAnswer(Tournament tournament,Integer nCorrect, User user) {

        def questionAnswer1 = new QuestionAnswer();
        def questionAnswer2 = new QuestionAnswer();
        def quizAnswer = new QuizAnswer(user, tournament.getQuiz())
        if(nCorrect >= 1) {
            questionAnswer1.setQuizAnswer(quizAnswer)
            optionOK1.addQuestionAnswer(questionAnswer1)
        }
        else if(nCorrect >= 2) {
            questionAnswer2.setQuizAnswer(quizAnswer)
            optionOK2.addQuestionAnswer(questionAnswer2)
        }

        quizAnswerRepository.save(quizAnswer)
    }

    def finishTournaments(tournamentSet, studentSet) {
        for(Tournament tournament : tournamentSet) {
            tournament.setStartingDate(FINISHED_START_DATE)
            tournament.setConclusionDate(FINISHED_CONCLUSION_DATE)
            tournament.setSignedUpUsers(studentSet)
            tournament.updateStatus()
        }
    }

    def setupUsers() {
        def student1 = new User(NAME1, USERNAME1, 1, User.Role.STUDENT)
        userRepository.save(student1)
        userService.addCourseExecution(student1.getId(),courseExecution.getId());

        def student2 = new User(NAME2, USERNAME2, 2, User.Role.STUDENT)
        userRepository.save(student2)
        userService.addCourseExecution(student2.getId(),courseExecution.getId());

        def student3 = new User(NAME3, USERNAME3, 3, User.Role.STUDENT)
        userRepository.save(student3)
        userService.addCourseExecution(student3.getId(),courseExecution.getId());

        def student4 = new User(NAME4, USERNAME4, 4, User.Role.STUDENT)
        userRepository.save(student4)
        userService.addCourseExecution(student4.getId(),courseExecution.getId());

        studentSet = new ArrayList<User>()
        studentSet.add(student1)
        studentSet.add(student2)
        studentSet.add(student3)
        studentSet.add(student4)
    }

    def studentSetSignedTournaments(tournamentSet) {
        for(User student : studentSet) {
            student.setSignUpTournaments(tournamentSet)
        }
    }

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        otherCourseExecution = new CourseExecution(course, ACRONYM2, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        setupUsers()

        topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        topicDto = topicService.createTopic(course.getId(), topicDto)

        question1 = new Question()
        question1.setKey(1)
        question1.setTitle("Question Title1")
        question1.setCourse(course)
        course.addQuestion(question1)

        question2 = new Question()
        question2.setKey(2)
        question2.setTitle("Question Title2")
        question2.setCourse(course)
        course.addQuestion(question2)

        optionOK1 = new Option()
        optionOK1.setCorrect(true)
        optionOK1.setContent("Option Content")
        optionOK1.setSequence(1)
        optionOK1.setQuestion(question1)

        optionOK2 = new Option()
        optionOK2.setCorrect(true)
        optionOK2.setContent("Option Content")
        optionOK2.setSequence(1)
        optionOK2.setQuestion(question2)

        questionRepository.save(question1)
        questionRepository.save(question2)
        optionRepository.save(optionOK1)
        optionRepository.save(optionOK2)
    }

    def setupSpec() {
        START_DATE = DateHandler.toISOString(LocalDateTime.now().plusDays(1))
        CONCLUSION_DATE = DateHandler.toISOString(LocalDateTime.now().plusDays(2))
        FINISHED_START_DATE = LocalDateTime.now().minusDays(10)
        FINISHED_CONCLUSION_DATE = LocalDateTime.now().minusDays(9)
    }


    def "Leaderboard OK after quizz generation"() {
        def tournamentSet = new HashSet<Tournament>()
        given: "a valid tournament dto"
        def tournament = createValidTournament()
        tournamentSet.add(tournament)
        and: "students that have signed up for it"
        studentSetSignedTournaments(tournamentSet);

        when:
        finishTournaments(tournamentSet,studentSet);
        def result = tournamentService.getClosedTournaments(studentSet[0].getId(), courseExecution.getId());

        then: "the returned data is correct"
        result.size() == 1
        def firstTournament = result[0]
        def leaderboard = firstTournament.getLeaderboard();
        leaderboard.size() == studentSet.size()
        def foundOne = false
        for(UserBoardPlaceDto ubp : leaderboard) {
            for(User student : studentSet) {
                if(ubp.getUser().getId() == student.getId()) {
                    foundOne = true
                    ubp.getPlace() == 0
                }
            }
        }
        foundOne == true
    }

    def "Leaderboard OK after answers"() {
        def tournamentSet = new HashSet<Tournament>()
        given: "a valid tournament dto"
        def tournament = createValidTournament()
        tournamentSet.add(tournament)
        and: "students that have signed up for it"
        studentSetSignedTournaments(tournamentSet);

        and: "student 1 has 1 right answer"
            createValidQuizAnswer(tournament, 1, studentSet[0]);

        and: "student 2 has 2 right answers"
            createValidQuizAnswer(tournament,2, studentSet[1]);

        and: "student 3 has not answered"
            //nothing
        and: "student 4 has 1 right answer"
            createValidQuizAnswer(tournament, 1, studentSet[3]);
        when:
        finishTournaments(tournamentSet,studentSet);
        def result = tournamentService.getClosedTournaments(studentSet[0].getId(), courseExecution.getId());

        then: "the returned data is correct"
        result.size() == 1
        def firstTournament = result[0]
        def leaderboard = firstTournament.getLeaderboard();
        leaderboard.size() == studentSet.size()
        for(UserBoardPlaceDto ubp : leaderboard) {
            for(int i =0; i < studentSet.size(); i++) {
                User student = studentSet[i];
                if(ubp.getUser().getId() == student.getId()) {
                    ubp.getPlace() == 0
                }
                if(i == 0)
                    ubp.getPlace() == 2;
                else if(i == 1)
                    ubp.getPlace() == 1;
                else if(i == 2)
                    ubp.getPlace() == 4;
                else
                    ubp.getPlace() == 2;
            }
        }
    }
    @TestConfiguration
    static class ServiceImplTestContextConfiguration {
        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

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
