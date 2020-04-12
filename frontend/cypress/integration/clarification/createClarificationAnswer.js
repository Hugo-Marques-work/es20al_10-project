describe('Create Clarification Answer', () => {
  const clarificationTitle = 'Clarification test';
  const clarificationAnswer = 'Clarification Answer test';

  before(() => {
    cy.demoLogin('student');
    cy.firstSolvedQuiz();
    cy.createClarification(clarificationTitle);
    cy.closeSuccessMessage('Clarification created');
    cy.contains('Logout').click();
  });

  beforeEach(() => {
    cy.demoLogin('teacher');
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('login, answers a clarification and sees it', () => {
    cy.createClarificationAnswer(clarificationTitle, clarificationAnswer);
    cy.closeSuccessMessage('Answer sent');
    cy.checkForClarificationAnswer(clarificationAnswer);
  });

  it('login, answers a clarification with empty string and sees error', () => {
    cy.createClarificationAnswer(clarificationTitle, null);
    cy.closeErrorMessage();
  });

});
