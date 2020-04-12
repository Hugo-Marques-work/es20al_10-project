describe('Create Clarifications', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.firstSolvedQuiz();
  });

  afterEach(() => {
    cy.contains('Logout').click()
  });

  it('login and created a clarification', () => {
    cy.get('[data-cy="createClarificationButton"]').click();
    cy.createClarification('This is a new Clarification.');
    cy.closeSuccessMessage('Clarification created');
  });
  
  // it('login and created an empty clarification', () => {
  //   cy.get('[data-cy="createClarificationButton"]').click();
  //   cy.createClarification('\n');
  //   cy.closeErrorMessage();
  // });

});