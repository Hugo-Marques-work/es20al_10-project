package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer;

@Repository
@Transactional
public interface ClarificationAnswerRepository extends JpaRepository<ClarificationAnswer, Integer> {

}
