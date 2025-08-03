<template>
  <div class="bg-right"></div>
  <div class="container">
    <slot />
  </div>
  <div class="bg-left"></div>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
.container {
  @apply flex flex-col gap-4 mt-16 mx-auto max-w-[calc(100vw-40px)] sm:mt-[4.5rem] sm:w-[90vw] md:mt-24 md:flex-row 2xl:w-[1200px];
}
.bg-right {
  @apply absolute -z-[1] top-0 right-0 w-[206px] h-[304px] bg-cover bg-no-repeat bg-[url('@/assets/images/bg-right.png')];
  // @apply sm:w-[370px] sm:h-[650px];
  // @apply md:w-[468px] md:h-[820px];
  // @apply lg:w-[532px] lg:h-[930px];
  // @apply xl:w-[576px] xl:h-[1010px];
  // @apply 2xl:w-[646px] 2xl:h-[1130px];
}
</style>
