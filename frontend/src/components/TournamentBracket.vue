<template>
  <div class="bracket-wrapper">
    <div class="bracket">
      <div
        v-for="(round, roundIndex) in localRounds"
        :key="roundIndex"
        class="round"
      >
        <div class="round-label">{{ getRoundLabel(roundIndex) }}</div>
        <div class="round-matches">
          <div
            v-for="(match, matchIndex) in round"
            :key="match.matchId"
            class="match-slot"
            :style="{ height: slotHeight(roundIndex) + 'px' }"
          >
            <!-- 垂直連接線（左側，接上一輪） -->
            <div
              v-if="roundIndex > 0"
              class="connector-v-left"
              :class="matchIndex % 2 === 0 ? 'v-top' : 'v-bottom'"
            ></div>

            <!-- 比賽卡片 -->
            <div class="match-card">
              <div
                class="player"
                :class="{
                  winner: match.winner === match.player1,
                  bye: match.player1 === 'BYE',
                  clickable: canClick(match, match.player1)
                }"
                @click="selectWinner(roundIndex, matchIndex, match.player1)"
              >
                <span class="name">{{ match.player1 }}</span>
                <span v-if="match.winner === match.player1" class="trophy">🏆</span>
              </div>
              <div class="divider"></div>
              <div
                class="player"
                :class="{
                  winner: match.winner === match.player2,
                  bye: match.player2 === 'BYE',
                  clickable: canClick(match, match.player2)
                }"
                @click="selectWinner(roundIndex, matchIndex, match.player2)"
              >
                <span class="name">{{ match.player2 }}</span>
                <span v-if="match.winner === match.player2" class="trophy">🏆</span>
              </div>
            </div>

            <!-- 水平 + 垂直連接線（右側，接下一輪） -->
            <template v-if="roundIndex < localRounds.length - 1">
              <div class="connector-h-right"></div>
              <div
                class="connector-v-right"
                :class="matchIndex % 2 === 0 ? 'v-bottom-half' : 'v-top-half'"
              ></div>
            </template>
          </div>
        </div>
      </div>

      <!-- 冠軍 -->
      <div v-if="champion" class="champion-column">
        <div class="round-label">冠軍</div>
        <div class="champion-card">
          <div class="crown">👑</div>
          <div class="champion-name">{{ champion }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  rounds: { type: Array, required: true }
})
const emit = defineEmits(['update:rounds'])

const SLOT_BASE = 80 // px — 第 0 輪每個 match 的 slot 高度

const localRounds = ref(JSON.parse(JSON.stringify(props.rounds)))

watch(() => props.rounds, (val) => {
  localRounds.value = JSON.parse(JSON.stringify(val))
}, { deep: true })

const champion = computed(() => {
  const last = localRounds.value[localRounds.value.length - 1]
  return last?.[0]?.winner || null
})

function slotHeight(roundIndex) {
  return SLOT_BASE * Math.pow(2, roundIndex)
}

function getRoundLabel(index) {
  const total = localRounds.value.length
  if (index === total - 1) return '決賽'
  if (index === total - 2) return '準決賽'
  if (index === total - 3) return '八強'
  return `第 ${index + 1} 輪`
}

function canClick(match, player) {
  if (!player || player === 'TBD' || player === 'BYE') return false
  return !match.winner
}

function selectWinner(roundIndex, matchIndex, winner) {
  const match = localRounds.value[roundIndex][matchIndex]
  if (!canClick(match, winner)) return
  match.winner = winner

  if (roundIndex + 1 < localRounds.value.length) {
    const next = localRounds.value[roundIndex + 1][Math.floor(matchIndex / 2)]
    if (matchIndex % 2 === 0) next.player1 = winner
    else next.player2 = winner
  }

  emit('update:rounds', localRounds.value)
}
</script>

<style scoped>
.bracket-wrapper {
  overflow-x: auto;
  padding: 20px;
}

.bracket {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 0;
  min-width: max-content;
}

/* 每輪欄 */
.round {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.round-label {
  font-size: 12px;
  font-weight: 700;
  color: #6366f1;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 12px;
  width: 210px;
  text-align: center;
}

.round-matches {
  display: flex;
  flex-direction: column;
}

/* 每個 match 的 slot：高度由 slotHeight() 決定，match card 垂直置中 */
.match-slot {
  display: flex;
  align-items: center;
  position: relative;
  width: 210px;
  padding-left: 24px;
}

/* ── Match Card ─────────────────────────────────── */
.match-card {
  width: 160px;
  flex-shrink: 0;
  background: #1e1e2e;
  border: 1px solid #313244;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
  z-index: 1;
}

.player {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  min-height: 38px;
  transition: background 0.15s;
}

.player.clickable { cursor: pointer; }
.player.clickable:hover { background: #2a2a3e; }

.player.winner {
  background: linear-gradient(90deg, #1a1a2e, #16213e);
  border-left: 3px solid #6366f1;
}

.player.bye { opacity: 0.3; cursor: default; }

.name {
  font-size: 13px;
  color: #cdd6f4;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.player.winner .name { color: #a6e3a1; font-weight: 600; }
.trophy { font-size: 12px; }
.divider { height: 1px; background: #313244; }

/* ── 連接線 ──────────────────────────────────────── */

/* 右側水平線：從 card 右緣（padding 24px + card 160px）延伸到 slot 右緣 */
.connector-h-right {
  position: absolute;
  left: calc(24px + 160px);
  right: 0;
  height: 1px;
  background: #45475a;
  top: 50%;
}

/* 右側垂直線：連接同一 round 中上下兩個 match 到下一輪 */
.connector-v-right {
  position: absolute;
  right: 0;
  width: 1px;
  background: #45475a;
}
/* 偶數 match (pair 的上半)：從中心往下延伸到 slot 底部 */
.connector-v-right.v-bottom-half {
  top: 50%;
  bottom: 0;
}
/* 奇數 match (pair 的下半)：從 slot 頂部延伸到中心 */
.connector-v-right.v-top-half {
  top: 0;
  bottom: 50%;
}

/* 左側水平線：從左邊連入 match card */
.connector-v-left {
  position: absolute;
  left: 0;
  width: 20px;
  height: 1px;
  background: #45475a;
  top: 50%;
}

/* 冠軍欄 */
.champion-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-left: 20px;
  padding-top: 0;
  justify-content: center;
  align-self: center;
}

.champion-card {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: 2px solid #6366f1;
  border-radius: 16px;
  padding: 24px 32px;
  text-align: center;
  animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
  from { box-shadow: 0 0 20px rgba(99,102,241,0.3); }
  to   { box-shadow: 0 0 40px rgba(99,102,241,0.6); }
}

.crown { font-size: 36px; margin-bottom: 8px; }
.champion-name { font-size: 18px; font-weight: 700; color: #cba6f7; white-space: nowrap; }
</style>
