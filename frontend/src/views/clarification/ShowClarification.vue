<template>
  <div>
    <br />
    <span
      class="clarificationContent"
      v-html="convertMarkDown(clarification.content)"
    />
    <span>{{ clarification.user.name }}</span>
    <br />
    <br />
    <br />
    <div>
      <ul class="answerContent">
        <li v-for="answer in answers" :key="answer.id">
          <span
            class="clarificationContent"
            v-html="convertMarkDown(answer.content)"
          />
          <span v-html="convertMarkDown(answer.user.name)" />
          <br />
        </li>
      </ul>
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

  @Watch('clarification')
  @Watch('refresh')
  async onChildChanged() {
    await this.$store.dispatch('loading');
    try {
      this.answers = await RemoteServices.getClarificationAnswers(
        this.clarification
      );
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
