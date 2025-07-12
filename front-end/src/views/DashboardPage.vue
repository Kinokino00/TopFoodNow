<template>
  <div class="flex">
    <ScrollBar class="w-2/12 p-3 max-h-screen">
      <button
        class="store"
        v-for="(store, index) in storeList"
        :key="store.name"
        @click="goToStore(store, index)"
      >
        {{ store.name }}
      </button>
    </ScrollBar>
    <div class="map w-7/12" id="map"></div>
    <div class="streetView w-3/12" id="streetView"></div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { Map, Marker, InfoWindow, Icon } from '@types/google.maps'
import { Easing, Tween, Group } from '@tweenjs/tween.js'
import store_list from '@/assets/dataset/store_list.json'
import officeMaps from '@/assets/dataset/TOWN_MOI_CLEAN_MERGE.json'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'

const infoWindows = ref<InfoWindow[]>([])
const markers = ref<Marker[]>([])
const customIcon = ref<Icon | null>(null)
const map = ref<Map | null>(null)
const streetView = ref<google.maps.StreetViewPanorama | null>(null)

const storeList = store_list.slice(0, 20).map((store: any) => ({
  name: store.name,
  lat: Number(store.lat),
  lng: Number(store.lng),
}))
const defaultPosition = { lat: storeList[0].lat, lng: storeList[0].lng }
const isWindowShow = ref(storeList.map(() => false))

const cameraOptions: google.maps.CameraOptions = {
  tilt: 0,
  heading: 0,
  zoom: 3,
  center: defaultPosition,
}

const mapOptions = {
  ...cameraOptions,
  mapId: '15431d2b469f209e',
}

function initMap(): void {
  const mapContainer = document.getElementById('map') as HTMLElement
  if (!mapContainer) return

  map.value = new google.maps.Map(mapContainer, mapOptions)
  map.value.data.addGeoJson(officeMaps)
  map.value.data.setStyle({
    strokeWeight: 1,
    strokeOpacity: 0.4,
    strokeColor: '#007458',
    fillColor: '#92C7BA',
    fillOpacity: 0.4,
  })

  customIcon.value = {
    url: new URL('@/assets/images/mark.png', import.meta.url).href,
    scaledSize: new google.maps.Size(33, 40),
  }

  storeList.forEach((store, i) => {
    const marker = new google.maps.Marker({
      position: { lat: store.lat, lng: store.lng },
      map: map.value!,
      icon: customIcon.value,
    })
    markers.value.push(marker)

    const infoWindow = new google.maps.InfoWindow({ content: store.name })
    infoWindows.value.push(infoWindow)

    marker.addListener('click', () => {
      closeAllInfoWindows()
      infoWindow.open(map.value, marker)
    })
    isWindowShow.value[i] = false
  })

  // 預設打開第一個門市的 infoWindow
  if (infoWindows.value[0] && markers.value[0]) {
    infoWindows.value[0].open(map.value, markers.value[0])
  }

  google.maps.event.addListener(map.value, 'zoom_changed', () => {
    const zoomLevel = map.value!.getZoom()
    if (zoomLevel >= 10) loadNextBatch()
  })
  
  const tween = new Tween(cameraOptions)
    .to({ tilt: 90, heading: 0, zoom: 18 }, 5000)
    .easing(Easing.Back.Out)
    .onUpdate(() => map.value!.moveCamera(cameraOptions))
    .start()
  const group = new Group()
  group.add(tween)
  requestAnimationFrame(
    function loop(time) {
      group.update(time)
      requestAnimationFrame(loop)
    }
  )
}

// 初始化街景
function initStreetView() {
  const streetViewContainer = document.getElementById('streetView') as HTMLElement
  if (!streetViewContainer) return

  streetView.value = new google.maps.StreetViewPanorama(streetViewContainer, {
    position: defaultPosition,
    pov: { heading: 165, pitch: 0 },
    zoom: 1,
  })
}

// 前往該門市
function goToStore(store: { lat: number; lng: number; name: string }, index: number) {
  if (map.value && streetView.value) {
    map.value.panTo({ lat: store.lat, lng: store.lng })
    map.value.setZoom(15)

    closeAllInfoWindows()
    if (infoWindows.value[index] && markers.value[index]) {
      infoWindows.value[index].open(map.value, markers.value[index])
    }

    // 顯示對應位置的街景
    streetView.value.setPosition({ lat: store.lat, lng: store.lng })
    streetView.value.setVisible(true)
  }
}

function closeAllInfoWindows() {
  infoWindows.value.forEach((infoWindow: { close: () => any }) => infoWindow.close())
}


// 批次加載標記
const BATCH_SIZE = 100
let currentBatch = 1
function loadNextBatch(): void {
  const start = currentBatch * BATCH_SIZE
  const end = start + BATCH_SIZE
  const storesToLoad = storeList.slice(start, end)

  storesToLoad.forEach(store => {
    const marker = new google.maps.Marker({
      position: { lat: store.lat, lng: store.lng },
      map: map.value!,
      icon: customIcon.value,
    })
    markers.value.push(marker)
  })

  currentBatch++
}

// 在組件掛載後初始化地圖和街景
onMounted(() => {
  if (window.google && window.google.maps) {
    initMap()
    initStreetView()
    loadNextBatch()
  }
})
</script>

<style lang="scss">
.store {
  @apply p-2 w-full text-left border-b border-gray-100 hover:bg-primary-50;
}
.map {
  @apply h-screen;
}
.streetView {
  @apply h-screen;
}
.gm-ui-hover-effect {
  width: 22px !important;
  height: 22px !important;
  span {
    margin: 4px !important;
    width: 16px !important;
    height: 16px !important;
  }
}
</style>
