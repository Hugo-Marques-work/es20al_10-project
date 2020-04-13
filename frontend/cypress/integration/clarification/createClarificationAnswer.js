describe('Create Clarification Answer', () => {
  const clarificationTitle = 'Clarification test';
  const clarificationAnswer = 'Clarification Answer test';

  before(() => {
    cy.demoLogin('student');
    cy.makeAndSolveQuiz();
    cy.createClarification(clarificationTitle);
    cy.closeSuccessMessage('Clarification created');
    cy.logout();
  });

  beforeEach(() => {
    cy.demoLogin('teacher');
  });
  
  it('login, answers a clarification and sees it', () => {
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, clarificationAnswer);
    cy.closeSuccessMessage('Answer sent');
    cy.contains(clarificationAnswer);
  });

  it('login, answers a clarification with empty string and sees error', () => {
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, null);
    cy.closeErrorMessage();
  });

});
