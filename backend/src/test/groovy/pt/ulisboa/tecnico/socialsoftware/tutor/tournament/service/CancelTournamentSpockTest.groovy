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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_ALREADY_CANCELED
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_THE_CREATOR
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_RUNNING
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

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

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserRepository userRepository

    User creator
    User user
    Tournament tournament

    def setup(){
        creator = new User(USER_NAME, USER_UNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(creator)

        tournament = new Tournament(creator,GOOD_START_DATE, CONCLUSION_DATE, NUMBER_QUESTIONS)
        tournamentRepository.save(tournament)

        user = new User(USER_NAME,USER2_UNAME,USER2_KEY, User.Role.STUDENT)
        userRepository.save(user)
    }

    def getTournamentId(tournament, validTournamentId) {
        if(validTournamentId) return tournament.getId();
        return -1;
    }

    def getUserId(userType) {
        switch (userType) {
            case userType.CREATOR:
                return creator.getId();
            case userType.USER:
                return user.getId();
            default:
                return -1
        }
    }

    def setStatusCanceled(tournament, statusCanceled) {
        if(statusCanceled) tournament.setStatus(Tournament.Status.CANCELED)
    }

    def "cancel a tournament"() {
        given: "a creator"
        def creatorId=creator.getId()

        and:"a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()

        when:
        tournamentService.cancelTournament(creatorId, tournamentId);

        then:"tournament has been canceled"
        tournamentRepository.findAll().get(0).getStatus() == Tournament.Status.CANCELED
    }

    @Unroll
    def  "invalid arguments: startingDate=#startingDate | \
        statusCanceled=#statusCanceled |userTypeVar=#userTypeVar | \
        validTournamentId=#validTournamentId | errorMessage=#errorMessage "() {
        given:"a tournament id"
        tournament.setStartingDate(startingDate)
        def tournamentId = getTournamentId(tournament, validTournamentId)

        and: "a user id"
        def userId = getUserId(userTypeVar)

        when:
        setStatusCanceled(tournament,statusCanceled)
        tournamentService.cancelTournament(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        startingDate    | statusCanceled | userTypeVar           | validTournamentId || errorMessage
        GOOD_START_DATE | false          | userType.USER         | true              || TOURNAMENT_NOT_THE_CREATOR
        BAD_START_DATE  | false          | userType.CREATOR      | true              || TOURNAMENT_RUNNING
        GOOD_START_DATE | true           | userType.CREATOR      | true              || TOURNAMENT_ALREADY_CANCELED
        GOOD_START_DATE | false          | userType.INVALID_USER | true              || USER_NOT_FOUND
        GOOD_START_DATE | false          | userType.CREATOR      | false             || TOURNAMENT_NOT_FOUND
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}