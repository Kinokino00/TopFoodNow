<template>
  <div class='youtube-video'>
    <div :id="'youtube-' + id" class="w-full h-screen"></div>
  </div>
</template>


<script setup>
import { watch, onMounted, onBeforeUnmount } from 'vue'
import YouTubePlayer from 'youtube-player'

const props = defineProps({
  id: { type: String, default: '' },
  src: { type: String, required: true },
  width: { type: Number, default: 0 },
  height: { type: Number, default: 0 },
  playerVars: {
    type: Object,
    default() {
      return {}
    }
  }
})

const emit = defineEmits(['ended', 'play', 'pause'])

onMounted(() => {
  initPlayer()
  loadPlayer()
  addStateChange()
})

onBeforeUnmount(() => {
  removeStateChange()
  player && player.destroy()
  player = null
})

const getVideoId = () => {
  try {
    return new URL(props.src).searchParams.get('v') || ''
  } catch (error) {
    return ''
  }
}

let player = null
const initPlayer = () => {
  try {
    player = YouTubePlayer(`youtube-${props.id}`, {
      videoId: getVideoId(),
      width: props.width,
      height: props.height,
      // playerVars 播放器其它参数配置
      playerVars: {
        playsinline: 1,
        ...props.playerVars
      }
    })
  } catch (error) {
    console.log(error)
  }
}

const loadPlayer = () => {
  try {
    player.loadVideoById(getVideoId())
  } catch (error) {
    console.log(error)
  }
}

const play = () => player && player.playVideo()
const pause = () => player && player.pauseVideo()

// -1（未开始）0（已结束）1（正在播放）2（已暂停）3（正在缓冲）5（视频已插入）
let stateChangeListener
const addStateChange = () => {
  stateChangeListener = player && player.on('stateChange', (event) => {
    if (event.data === 0) emit('ended')
    if (event.data === 1) emit('play')
    if (event.data === 2) emit('pause')
  });
};
const removeStateChange = () => {
  if (stateChangeListener && player) player.off(stateChangeListener)
};

watch(
  () => props.src,
  () => loadPlayer()
)

watch(
  () => [props.width, props.height],
  () => {
    player?.setSize && player.setSize(props.width, props.height);
  }
)

defineExpose({ play, pause })
</script>


<style lang='scss' scoped>
.youtube-video {
  width: 100%;
  overflow: hidden;
}
</style>
