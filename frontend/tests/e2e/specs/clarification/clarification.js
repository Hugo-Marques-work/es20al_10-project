describe('Clarifications creating, answering and viewing', () => {
  const clarificationTitle = 'Clarification test for viewing';
  const clarificationAnswer = 'Clarification Answer test for viewing';

  afterEach(() => {
    cy.logout();
  });

  it('login and created a clarification', () => {
    cy.demoLogin('student');
    cy.makeAndSolveQuiz();
    cy.createClarification(clarificationTitle);
    cy.closeSuccessMessage('Clarification created');
  });

  it('login and created an empty clarification', () => {
    cy.demoLogin('student');
    cy.makeAndSolveQuiz();
    cy.createClarification(null);
    cy.closeErrorMessage();
  });

  it('login, check that clarification is unanswered', () => {
    cy.demoLogin('student');
    cy.clarificationList();
    cy.checkClarificationAnswered(clarificationTitle, false);
  });

  it('login, answers a clarification and sees it', () => {
    cy.demoLogin('teacher');
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, clarificationAnswer);
    cy.closeSuccessMessage('Answer sent');
    cy.contains(clarificationAnswer);
  });

  it('login, answers a clarification with empty string and sees error', () => {
    cy.demoLogin('teacher');
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, null);
    cy.closeErrorMessage();
  });

  it('login, student sees clarification answered', () => {
    cy.demoLogin('student');
    cy.clarificationList();
    cy.checkClarificationAnswered(clarificationTitle, true);
    cy.checkForClarificationAnswer(clarificationTitle, clarificationAnswer);
  });
});
