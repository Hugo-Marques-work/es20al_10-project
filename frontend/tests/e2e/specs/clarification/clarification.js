describe('Clarifications creating, answering and viewing', () => {
  const clarificationTitle = 'Clarification test for viewing';
  const clarificationAnswer = 'Clarification Answer test for viewing';
  const clarificationHash = '2aae55f0ade2ce79cd0fde26761451fb';

  afterEach(() => {
    cy.logout();
  });

  it('login and created a clarification', () => {
    cy.demoStudentLogin();
    cy.makeAndSolveQuiz();
    cy.createClarification(clarificationTitle);
    cy.closeSuccessMessage('Clarification created');
  });

  it('login and created an empty clarification', () => {
    cy.demoStudentLogin();
    cy.makeAndSolveQuiz();
    cy.createClarification(null);
    cy.closeErrorMessage();
  });

  it('login, check that clarification is unanswered', () => {
    cy.demoStudentLogin();
    cy.clarificationList();
    cy.checkClarificationAnswered(clarificationTitle, false);
  });

  it('login, answers a clarification and sees it', () => {
    cy.demoTeacherLogin();
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, clarificationAnswer);
    cy.closeSuccessMessage('Answer sent');
    cy.contains(clarificationAnswer);
  });

  it('login, answers a clarification with empty string and sees error', () => {
    cy.demoTeacherLogin();
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, null);
    cy.closeErrorMessage();
  });

  it('login, student sees clarification answered', () => {
    cy.demoStudentLogin();
    cy.clarificationList();
    cy.checkClarificationAnswered(clarificationTitle, true);
    cy.checkForClarificationAnswer(clarificationTitle, clarificationAnswer);
  });

  it('login and asks for another clarification', () => {
    cy.demoStudentLogin();
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, clarificationAnswer);
    cy.closeSuccessMessage('Answer sent');
    cy.contains(clarificationAnswer);
  });

  it('login and asks for another clarification without waiting for an answer', () => {
    cy.demoStudentLogin();
    cy.clarificationList();
    cy.createClarificationAnswer(clarificationTitle, clarificationAnswer);
    cy.closeErrorMessage();
  });

  it('login, goes to dashboard and checks credited clarifications', () => {
    cy.demoStudentLogin();
    cy.clarificationDashboard();
    cy.openCreditedClarifications();
  });

  it('login, goes to dashboard and checks clarification list', () => {
    cy.demoStudentLogin();
    cy.clarificationDashboard();
    cy.openClarificationList();
  });

  it('login and changes clarification dashboard availability', () => {
    cy.demoStudentLogin();
    cy.clarificationDashboard();
    cy.toggleDashboardAvailability();
    cy.closeSuccessMessage('Dashboard availability changed to: ');
  });

  it('create clarification with another student, login, teacher makes clarification available', () => {
    cy.addClarificationToDB(clarificationHash);
    cy.demoTeacherLogin();
    cy.clarificationList();
    cy.makeClarificationAvailable(clarificationHash);
    cy.closeSuccessMessage('Clarification is now available in anonymity');
  });

  it('login, student sees last clarification available', () => {
    cy.demoStudentLogin();
    cy.availableClarificationsList();
    cy.checkClarification(clarificationHash);
  });
});
