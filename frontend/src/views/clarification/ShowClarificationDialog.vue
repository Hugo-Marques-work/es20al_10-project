<template>
  <v-dialog
    v-on:click:outside="closeClarificationDialog"
    v-model="dialog"
    @keydown.esc="closeClarificationDialog"
    max-width="95%"
    content-class="ClarificationDialog"
  >
    <v-card class="InformationCard">
      <v-card-title>
        <span class="headline">{{ clarification.question.title }}</span>
      </v-card-title>

      <v-card-text class="text-left">
        <show-question :question="clarification.question" />
        <v-divider />
        <show-clarification :clarification="clarification" :refresh="refresh" />
      </v-card-text>
    </v-card>

    <v-card class="WriteAnswer">
      <v-card-text style="padding-bottom: 0">
        <div v-if="isTeacher" style="margin-top: -3pt">
          <v-textarea
            clearable
            auto-grow
            rows="1"
            ref="input"
            placeholder="Answer clarification"
            v-model="answerContent"
            v-on:keydown.enter="sendClarificationAnswer"
            v-on:keydown.shift="canSend = false"
            v-on:keyup.shift="canSend = true"
          >
          </v-textarea>
        </div>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn dark color="secondary" @click="closeClarificationDialog"
          >close</v-btn
        >
        <v-btn dark color="blue darken-1" @click="sendClarificationAnswer"
          >send</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Emit, Prop, Vue } from 'vue-property-decorator';
import Clarification from '@/models/clarification/Clarification';
import ShowQuestion from '@/views/teacher/questions/ShowQuestion.vue';
import ShowClarification from '@/views/clarification/ShowClarification.vue';
import RemoteServices from '@/services/RemoteServices';
import ClarificationAnswer from '@/models/clarification/ClarificationAnswer';

@Component({
  components: {
    'show-question': ShowQuestion,
    'show-clarification': ShowClarification
  }
})
export default class ShowClarificationDialog extends Vue {
  @Prop({ type: Clarification, required: true })
  readonly clarification!: Clarification;
  @Prop({ type: Boolean, required: true }) readonly dialog!: boolean;
  @Prop({ type: Boolean, required: true }) readonly isTeacher!: boolean;
  answerContent: string = '';
  refresh: boolean = false;
  clarificationAnswer: ClarificationAnswer | null = null;
  canSend: boolean = true;

  closeClarificationDialog() {
    this.answerContent = '';
    this.$emit('close-show-clarification-dialog');
  }

  async sendClarificationAnswer() {
    if (this.canSend) {
      await this.$store.dispatch('loading');

      if (this.answerContent != null) {
      } else {
        await this.$store.dispatch('error', 'Answer can not be empty');
        return;
      }

      try {
        if (this.clarification != null && this.clarification.id != null) {
          this.clarificationAnswer = await RemoteServices.createClarificationAnswer(
            this.clarification.id,
            this.answerContent
          );
          this.refresh = !this.refresh;
          this.answerContent = '';
          this.updateAnsweredStatus();
          await this.$store.dispatch('confirmation', 'Answer sent');
        }
      } catch (error) {
        await this.$store.dispatch('error', error);
      }

      await this.$store.dispatch('clearLoading');
    }
  }

  @Emit()
  updateAnsweredStatus() {
    return;
  }
}
</script>

<style lang="css">
.ClarificationDialog {
  height: 80% !important;
  overflow: hidden !important;
}
.InformationCard {
  height: 84% !important;
  overflow: auto !important;
}

.WriteAnswer {
  height: 16%;
}
</style>
