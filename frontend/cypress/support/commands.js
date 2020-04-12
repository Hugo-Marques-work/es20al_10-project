// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
/// <reference types="Cypress" />
Cypress.Commands.add('demoAdminLogin', () => {
    cy.visit('/')
    cy.get('[data-cy="adminButton"]').click()
    cy.contains('Administration').click()
    cy.contains('Manage Courses').click()
})

Cypress.Commands.add('demoStudentLoginTournaments', () => {
    cy.visit('/')
    cy.get('[data-cy="studentButton"]').click()
    cy.contains('Tournaments').click()
})

Cypress.Commands.add('createTournament', (name, startShift, endShift) => {
    let start = new Date()
    let end = new Date()
    start.setDate(start.getDate() + startShift)
    end.setDate(end.getDate() + endShift)

    cy.get('[data-cy="createButton"]').click()

    cy.get('label').contains('Starting Date').parent().children('input').click()
    cy.get('button:contains(' + start.getDate() +')').eq(0).click()
    cy.get('button:contains("OK")').eq(0).click()

    cy.get('label').contains('Conclusion Date').parent().children('input').click()
    cy.get('button:contains(' + end.getDate() +')').eq(1).click()
    cy.get('button:contains("OK")').eq(1).click()

    cy.get('[data-cy="Topics"]').click()
    cy.get('[role="listbox"]').children().eq(0).click()
    cy.get('[role="listbox"]').children().eq(1).click()
    if (name) {
        cy.get('[data-cy="Title"]').type(name)
    }

    cy.get('[data-cy="saveButton"]').click()
})

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
    cy.get('[data-cy="createButton"]').click()
    cy.get('[data-cy="Name"]').type(name)
    cy.get('[data-cy="Acronym"]').type(acronym)
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm)
    cy.get('[data-cy="saveButton"]').click()
})

Cypress.Commands.add('closeErrorMessage', (name, acronym, academicTerm) => {
    cy.contains('Error')
        .parent()
        .find('button')
        .click()
})

Cypress.Commands.add('deleteCourseExecution', (acronym) => {
    cy.contains(acronym)
        .parent()
        .should('have.length', 1)
        .children()
        .should('have.length', 7)
        .find('[data-cy="deleteCourse"]')
        .click()
})

Cypress.Commands.add('createFromCourseExecution', (name, acronym, academicTerm) => {
    cy.contains(name)
        .parent()
        .should('have.length', 1)
        .children()
        .should('have.length', 7)
        .find('[data-cy="createFromCourse"]')
        .click()
    cy.get('[data-cy="Acronym"]').type(acronym)
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm)
    cy.get('[data-cy="saveButton"]').click()
})

