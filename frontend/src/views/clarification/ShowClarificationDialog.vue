<template>
  <v-dialog
    v-model="dialog"
    @keydown.esc="closeClarificationDialog"
    max-width="95%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">{{ clarification.question.title }}</span>
      </v-card-title>

      <v-card-text class="text-left">
        <show-question :question="clarification.question" />
      </v-card-text>
    </v-card>

    <v-card>
      <v-card-title>
        <span class="headline"
          >Clarification - {{ clarification.user.name }}</span
        >
      </v-card-title>

      <v-card-text class="text-left">
        <show-clarification :clarification="clarification" />
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn dark color="blue darken-1" @click="closeClarificationDialog"
          >close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import Clarification from '@/models/clarification/Clarification';
import ShowQuestion from '@/views/teacher/questions/ShowQuestion.vue';
import ShowClarification from '@/views/clarification/ShowClarification.vue';

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

  closeClarificationDialog() {
    this.$emit('close-show-clarification-dialog');
  }
}
</script>

<style lang="scss" scoped></style>
