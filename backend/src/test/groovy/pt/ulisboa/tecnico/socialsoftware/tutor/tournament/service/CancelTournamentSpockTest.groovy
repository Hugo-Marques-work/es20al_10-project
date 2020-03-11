package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class CancelTournamentSpockTest extends Specification{

    def setup() {}

    def "cancel a tournament"() {
        expect: false
    }

    def "a user that is not the creator cancels the tournament"(){
        expect: false
    }

    def "the tournament has already started"(){
        expect: false
    }

    def "the tournament has already been canceled"(){
        expect: false
    }

    def "user does not exist"(){
        expect: false
    }

    def "tournament does not exist"(){
        expect: false
    }
}