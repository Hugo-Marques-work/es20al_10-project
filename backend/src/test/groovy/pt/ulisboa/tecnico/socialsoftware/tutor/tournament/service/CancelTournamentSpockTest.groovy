package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
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
    static final String USER2_UNAME = "carlos2"
    static final Integer USER_KEY = 1
    static final Integer USER2_KEY = 2
    static final LocalDateTime CONCLUSION_DATE = LocalDateTime.now().plusDays(3)
    static final LocalDateTime BAD_START_DATE = LocalDateTime.now().minusDays(1)
    static final LocalDateTime GOOD_START_DATE = LocalDateTime.now().plusDays(1)
    static final LocalDateTime BAD_START_DATE2 = LocalDateTime.now().minusDays(3)
    static final LocalDateTime BAD_CONCLUSION_DATE = LocalDateTime.now().minusDays(2)

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserRepository userRepository

    Tournament tournament

    def setup(){
        def creator = new User(USER_NAME, USER_UNAME, USER_KEY, User.Role.STUDENT)
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
        setStatusCanceled(tournament,statusCanceled)
        tournamentService.cancelTournament(tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        startingDate    | conclusionDate      | statusCanceled | validTournamentId || errorMessage
        BAD_START_DATE  | CONCLUSION_DATE     | false          | true              || TOURNAMENT_RUNNING
        GOOD_START_DATE | CONCLUSION_DATE     | true           | true              || TOURNAMENT_ALREADY_CANCELED
        BAD_START_DATE2 | BAD_CONCLUSION_DATE | false          | true              || TOURNAMENT_FINISHED
        GOOD_START_DATE | CONCLUSION_DATE     | false          | false             || TOURNAMENT_NOT_FOUND
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}