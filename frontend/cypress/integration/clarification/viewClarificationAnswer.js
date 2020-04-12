describe('View Clarification Answer', () => {
  const clarificationTitle = 'Clarification test for viewing';
  const clarificationAnswer = 'Clarification Answer test for viewing';

  before(() => {
    cy.demoLogin('student');
    cy.firstSolvedQuiz();
    cy.createClarification(clarificationTitle);
    cy.closeSuccessMessage('Clarification created');
    cy.logout();
  });

  it('login, check that clarification is unanswered', () => {
    cy.demoLogin('student');
    cy.clarificationList();
    cy.checkClarificationAnswered(clarificationTitle, false);
  });

  it('login, teacher sends an answer then student sees it answered', () => {
    cy.log('Create the answer for clarification: ' + clarificationTitle);
    cy.demoLogin('teacher');
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, clarificationAnswer);
    cy.closeSuccessMessage('Answer sent');
    cy.logout();

    cy.log('Check if student sees it');
    cy.demoLogin('student');
    cy.clarificationList();
    cy.checkClarificationAnswered(clarificationTitle, true);
    cy.checkForClarificationAnswer(clarificationTitle, clarificationAnswer);
  });

});
