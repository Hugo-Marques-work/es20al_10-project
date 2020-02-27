package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User

class CreateClarificationAnswerTest {


    def "user and clarification exists and creates clarification answers"(){
        expects: false;
    }

    def "clarification doesn't exist"(){
        expects: false;
    }

    def "user doesn't exist"() {
        expects: false;
    }

    def "user is not a teacher"() {
        expects: false;
    }

    def "content is blank"(){
        expects: false;
    }

    def "content is empty"(){
        expects: false;
    }
}
