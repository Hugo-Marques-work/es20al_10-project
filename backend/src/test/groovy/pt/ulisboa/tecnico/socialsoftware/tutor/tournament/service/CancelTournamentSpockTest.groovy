package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import spock.lang.Unroll
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class CancelTournamentSpockTest extends Specification{
    static final enum userType {
        USER,
        CREATOR,
        INVALID_USER
    }

    static final Integer NUMBER_QUESTIONS = 1
    static final String USER_NAME = "Carlos"
    static final String USER_UNAME = "carlos"
    static final Integer USER_KEY = 1
    static final LocalDateTime CONCLUSION_DATE = DateHandler.now().plusDays(3)
    static final LocalDateTime BAD_START_DATE = DateHandler.now().minusDays(1)
    static final LocalDateTime GOOD_START_DATE = DateHandler.now().plusDays(1)
    static final LocalDateTime BAD_START_DATE2 = DateHandler.now().minusDays(3)
    static final LocalDateTime BAD_CONCLUSION_DATE = DateHandler.now().minusDays(2)

    User creator;

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    QuizService quizService

    @Autowired
    AnswerService answerService

    @Autowired
    AnswersXmlImport answersXmlImport

    @Autowired
    QuestionService questionService

    Tournament tournament

    def setup(){
        creator = new User(USER_NAME, USER_UNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(creator)

        tournament = new Tournament(creator, "TEST", GOOD_START_DATE, CONCLUSION_DATE, NUMBER_QUESTIONS)
        tournamentRepository.save(tournament)
    }

    def getTournamentId(tournament, validTournamentId) {
        if(validTournamentId) return tournament.getId();
        return -1;
    }

    def setStatusCanceled(tournament, statusCanceled) {
        if(statusCanceled) tournament.setStatus(Tournament.Status.CANCELED)
    }

    def "cancel a tournament"() {
        given:"a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()

        when:
        tournamentService.cancelTournament(tournamentId);

        then:"tournament has been canceled"
        tournamentRepository.findAll().get(0).getStatus() == Tournament.Status.CANCELED
    }

    @Unroll
    def  "invalid data in database where: startingDate=#startingDate | \
        conclusionDate=#conclusionDate |statusCanceled=#statusCanceled | \
        validTournamentId=#validTournamentId | errorMessage=#errorMessage "() {
        given:"a tournament id"
        tournament.setStartingDate(startingDate)
        tournament.setConclusionDate(conclusionDate)
        def tournamentId = getTournamentId(tournament, validTournamentId)

        when:
        setStatusCanceled(tournament, statusCanceled)
        tournamentService.cancelTournament(tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        startingDate    | conclusionDate      | statusCanceled | validTournamentId || errorMessage
        GOOD_START_DATE | CONCLUSION_DATE     | true           | true              || TOURNAMENT_ALREADY_CANCELED
        GOOD_START_DATE | CONCLUSION_DATE     | false          | false             || TOURNAMENT_NOT_FOUND
    }

    def "cancel running tournament"() {
        given: "a running tournament"
        tournament.setStartingDate(BAD_START_DATE)
        tournament.setConclusionDate(CONCLUSION_DATE)
        tournament.addSignUp(creator)

        when:
        tournamentService.cancelTournament(tournament.getId())

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_RUNNING
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
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