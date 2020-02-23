package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;

import java.util.HashSet;
import java.util.Set;

public class TournamentDto {
    private String availableDate;
    private String startingDate;
    private int numberOfQuestions;
    private Set<Topic> topics = new HashSet<>();

    public TournamentDto() {}
}
