package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationService;
import spock.lang.Specification;

class CreateClarificationServiceSpockTest extends Specification {
    def clarificationService

    def setup() {clarificationService = new ClarificationService()}

    def "question does not exist"() {
        // an exception is thrown
        expect: false
    }

    def "username is empty"() {
        // an exception is thrown
        expect: false
    }

    def "username is blank"() {
        // an exception is thrown
        expect: false
    }

    def "content is empty"() {
        // an exception is thrown
        expect: false
    }

    def "content is blank"() {
        // an exception is thrown
        expect: false
    }
}
