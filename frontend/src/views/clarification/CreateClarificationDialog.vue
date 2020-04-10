<template>
  <v-dialog
    v-on:click:outside="cancelClarification"
    v-model="dialog"
    width="50%"
  >
    <v-card>
      <v-card-title primary-title class="secondary white--text headline">
        Send Clarification
      </v-card-title>

      <v-card-text class="text--black title">
        <v-textarea
          clearable
          auto-grow
          rows="1"
          v-on:keydown.esc="cancelClarification"
          v-on:keydown.enter="createClarification"
          v-on:keydown.shift="canSend = false"
          v-on:keyup.shift="canSend = true"
          class="clarificationMessage"
          v-model="clarificationContent"
          placeholder="write clarification message here"
        >
        </v-textarea>
      </v-card-text>

      <v-divider />

      <v-card-actions>
        <v-spacer />
        <v-btn color="secondary" text @click="cancelClarification">
          Cancel
        </v-btn>
        <v-btn color="primary" text @click="createClarification">
          Send
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import StatementQuestion from '@/models/statement/StatementQuestion';
import Clarification from '@/models/clarification/Clarification';

@Component
export default class CreateClarificationDialog extends Vue {
  @Prop({ type: Boolean, required: true }) readonly dialog!: boolean;
  @Prop(StatementQuestion) readonly question!: StatementQuestion;
  clarification: Clarification | null = null;
  clarificationContent: string = '';
  canSend: boolean = true;

  async createClarification() {
    if (this.canSend) {
      await this.$store.dispatch('loading');

      if (this.clarificationContent != '') {
      } else {
        await this.$store.dispatch('error', 'Clarification can not be empty');
        return;
      }

      try {
        this.clarification = await RemoteServices.createClarification(
          this.question.quizQuestionId,
          this.clarificationContent
        );
        this.clarificationContent = '';
        this.cancelClarification();
        await this.$store.dispatch('confirmation', 'Clarification created');
      } catch (error) {
        await this.$store.dispatch('error', error);
      }

      await this.$store.dispatch('clearLoading');
    }
  }

  @Emit()
  cancelClarification() {
    this.clarificationContent = '';
  }
}
</script>

<style lang="scss" scoped>
.clarificationMessage {
  margin-top: 20pt;
}
</style>
