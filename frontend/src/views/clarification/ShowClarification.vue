<template>
  <div>
    <br />
    <span
      class="clarificationContent"
      v-html="convertMarkDown(clarification.content)"
    />
    <br />
    <span>{{ clarification.user.name }}</span>
    <br />
    <br />
    <v-divider />
    <br />
    <div v-if="hasAnswers">
      <ul class="answerContent">
        <li v-for="answer in answers" :key="answer.id">
          <span
            class="clarificationContent"
            v-html="convertMarkDown(answer.content)"
          />
          <br />
          <span v-html="convertMarkDown(answer.user.name)" />
          <br />
          <br />
        </li>
      </ul>
    </div>
    <div v-else>
      <b
        ><span style="font-size: 15pt" v-html="convertMarkDown('No answers')"
      /></b>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import Clarification from '@/models/clarification/Clarification';
import ClarificationAnswer from '@/models/clarification/ClarificationAnswer';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class ShowClarification extends Vue {
  @Prop({ type: Clarification, required: true })
  readonly clarification!: Clarification;
  @Prop({ type: Boolean, required: true })
  readonly refresh!: boolean;
  answers: ClarificationAnswer[] = [];
  hasAnswers: boolean = true;

  @Watch('clarification')
  @Watch('refresh')
  async onChildChanged() {
    await this.$store.dispatch('loading');
    try {
      this.answers = await RemoteServices.getClarificationAnswers(
        this.clarification
      );
      this.hasAnswers = this.answers.length != 0;
      this.$forceUpdate();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.answers = await RemoteServices.getClarificationAnswers(
        this.clarification
      );
      if (this.answers.length != 0) this.hasAnswers = true;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>

<style lang="scss" scoped>
.clarificationContent {
  font-size: 15pt;
  color: black;
  white-space: pre-line;
}

.answerContent {
  list-style-type: none;
  padding: 0;
  white-space: pre-line;
}
</style>
