describe('Tournament walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLoginTournaments()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('login creates a tournament and sees it on open tournaments', () => {
    let name = Math.random().toString(36);
    cy.createTournament(name, 12, 13)

    cy.contains(name).parent().should(item => {
      // expect one table row
      expect(item).to.have.length(1)
    })

    //TODO: cancel tournament as tearDown
  });

  it('login creates a tournament and cancel it', () => {
    let name = Math.random().toString(36);
    cy.createTournament(name, 12, 13)
    cy.cancelTournament(name)

    cy.wait(500)
    cy.contains(name).should('not.exist')

    cy.seeSignedUpTournaments()
    cy.contains(name).should('not.exist')

    cy.seeRunningTournaments()
    cy.contains(name).should('not.exist')

    cy.seeMyTournaments()
    cy.contains(name).should('not.exist')
  });

  it('login create tournaments with missing title', () => {
    cy.createTournament('', 12, 13)
    cy.closeErrorMessage()
  });

  it('login create tournaments with starting date before today', () => {
    cy.get('[data-cy="createButton"]').click()

    // Go back one month
    cy.get('label').contains('Starting Date').parent().children('input').click()
    cy.get('.v-date-picker-header').eq(0).children().eq(0).click()
    cy.get('button:contains(' + 11 +')').eq(1).click()
    cy.get('button:contains("OK")').eq(0).click()

    // Same as createTournament
    cy.get('label').contains('Conclusion Date').parent().children('input').click()
    cy.get('.v-date-picker-header').eq(1).children().eq(2).click()
    cy.get('button:contains(' + 11 +')').eq(2).click()
    cy.get('button:contains("OK")').eq(1).click()

    cy.get('[data-cy="Topics"]').click()
    cy.get('[role="listbox"]').children().eq(0).click()
    cy.get('[role="listbox"]').children().eq(1).click()
    cy.get('[data-cy="Title"]').type('test')

    cy.get('[data-cy="saveButton"]').click()

    cy.closeErrorMessage()
  });

  it('login create tournaments with starting date after conclusion date', () => {
    cy.createTournament('Test', 13, 12)
    cy.closeErrorMessage()
  });
});
