package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class CreateTournamentSpockTest extends Specification {

    def tournamentService

    def setup() {
        tournamentService = new TournamentService()
    }

    def "topic list is null"() {
        expect: false
    }

    def "topic list is empty"() {
        expect: false
    }

    def "topic list isn't empty but is invalid"() {
        expect: false
    }

    def "starting date is empty"() {
        expect: false
    }

    def "conclusion date is emtpy"() {
        expect: false
    }

    def "dates overlap"() {
        expect: false
    }

    def "number of questions lesser than 0"() {
        expect: false
    }
}
