package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

@Service
public class ClarificationService {
    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClarificationRepository clarificationRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto createClarification(Question question, User user, String content) {
        Question qtn = questionRepository.findByKey(question.getKey()).orElse(null);
        if (qtn == null)
            throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND, question.getKey());

        User usr = userRepository.findByKey(user.getKey());
        if (usr == null)
            throw new TutorException(ErrorMessage.USER_NOT_FOUND, user.getKey());

        if (content == null || content.isBlank() || content.isEmpty())
            throw new TutorException(ErrorMessage.CLARIFICATION_IS_EMPTY);

        Clarification clarification = new Clarification(content, qtn, usr);
        clarificationRepository.save(clarification);
        qtn.addClarification(clarification);
        usr.addClarification(clarification);

        return new ClarificationDto(clarification);
    }
}
