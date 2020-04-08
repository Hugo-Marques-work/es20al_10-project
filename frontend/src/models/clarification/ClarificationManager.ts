import RemoteServices from '@/services/RemoteServices';
import Clarification from '@/models/clarification/Clarification';

export default class ClarificationManager {
  clarification: Clarification | null = null;

  private static _manager: ClarificationManager = new ClarificationManager();

  static get getInstance(): ClarificationManager {
    return this._manager;
  }

  // async getQuizStatement() {
  //   let params = {
  //     // topic: this.topic,
  //     questionType: this.questionType,
  //     assessment: this.assessment,
  //     numberOfQuestions: +this.numberOfQuestions
  //   };
  //
  //   this.statementQuiz = await RemoteServices.generateStatementQuiz(params);
  // }
  //
  // async concludeQuiz() {
  //   if (this.statementQuiz) {
  //     let answers = await RemoteServices.concludeQuiz(this.statementQuiz.id);
  //     if (answers) {
  //       this.correctAnswers = answers;
  //     }
  //   } else {
  //     throw Error('No quiz');
  //   }
  // }
  //
  // reset() {
  //   this.statementQuiz = null;
  //   this.correctAnswers = [];
  // }
  //
  // isEmpty(): boolean {
  //   return this.statementQuiz == null;
  // }
}
