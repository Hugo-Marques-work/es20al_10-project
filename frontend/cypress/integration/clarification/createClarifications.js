describe('Create Clarifications', () => {
  beforeEach(() => {
    cy.demoLogin('student');
    cy.firstSolvedQuiz();
  });

  afterEach(() => {
    cy.contains('Logout').click()
  });

  it('login and created a clarification', () => {
    cy.createClarification('This is a new Clarification.');
    cy.closeSuccessMessage('Clarification created');
  });

  it('login and created an empty clarification', () => {
    cy.createClarification(null);
    cy.closeErrorMessage();
  });

});