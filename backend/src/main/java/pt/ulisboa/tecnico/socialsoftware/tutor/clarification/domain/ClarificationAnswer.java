package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;

import javax.persistence.*;

@Entity
@Table(name = "clarification_answers")
public class ClarificationAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
}
