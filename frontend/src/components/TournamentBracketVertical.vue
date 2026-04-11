<template>
  <div class="bracket-v-wrapper">
    <div class="bracket-v" :style="{ minWidth: totalWidth + 'px' }">

      <template v-for="(round, roundIndex) in localRounds" :key="roundIndex">
        <!-- 輪次標籤 -->
        <div class="round-label-v">{{ getRoundLabel(roundIndex) }}</div>

        <!-- 比賽列 -->
        <div class="matches-row-v">
          <div
            v-for="(match, matchIndex) in round"
            :key="match.matchId"
            class="match-slot-v"
            :style="{ width: slotWidth(roundIndex) + 'px' }"
          >
            <div class="match-card-v">
              <!-- Player 1 -->
              <div
                class="player-v"
                :class="{
                  winner:    match.winner === match.player1,
                  bye:       isBye(match.player1),
                  clickable: canClick(match, match.player1)
                }"
                @click="selectWinner(roundIndex, matchIndex, match.player1)"
              >
                <span v-if="match.winner === match.player1" class="trophy-v">🏆</span>
                <span class="name-v">{{ match.player1 }}</span>
              </div>
              <!-- VS -->
              <div class="vs-badge">VS</div>
              <!-- Player 2 -->
              <div
                class="player-v"
                :class="{
                  winner:    match.winner === match.player2,
                  bye:       isBye(match.player2),
                  clickable: canClick(match, match.player2)
                }"
                @click="selectWinner(roundIndex, matchIndex, match.player2)"
              >
                <span class="name-v">{{ match.player2 }}</span>
                <span v-if="match.winner === match.player2" class="trophy-v">🏆</span>
              </div>
            </div>
          </div>
        </div>

        <!-- SVG 連接線（連到下一輪，貝茲曲線匯流） -->
        <svg
          v-if="roundIndex < localRounds.length - 1"
          :width="totalWidth"
          height="40"
          class="connector-svg"
        >
          <g v-for="(_, pairIdx) in Math.floor(round.length / 2)" :key="pairIdx">
            <!-- 左 match → 中心（直角折線） -->
            <path
              :d="`M ${(pairIdx*2+0.5)*slotWidth(roundIndex)},0
                   L ${(pairIdx*2+0.5)*slotWidth(roundIndex)},20
                   L ${(pairIdx*2+1)*slotWidth(roundIndex)},20
                   L ${(pairIdx*2+1)*slotWidth(roundIndex)},40`"
              fill="none" stroke="#45475a" stroke-width="1"
            />
            <!-- 右 match → 水平匯流 -->
            <path
              :d="`M ${(pairIdx*2+1.5)*slotWidth(roundIndex)},0
                   L ${(pairIdx*2+1.5)*slotWidth(roundIndex)},20
                   L ${(pairIdx*2+1)*slotWidth(roundIndex)},20`"
              fill="none" stroke="#45475a" stroke-width="1"
            />
          </g>
        </svg>
      </template>

      <!-- 冠軍 -->
      <div v-if="champion" class="champion-row-v">
        <div class="champion-card-v">
          <div class="crown">👑</div>
          <div class="champion-name">{{ champion }}</div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({ rounds: { type: Array, required: true } })
const emit = defineEmits(['update:rounds'])

const SLOT_BASE = 200

const localRounds = ref(JSON.parse(JSON.stringify(props.rounds)))

watch(() => props.rounds, (val) => {
  localRounds.value = JSON.parse(JSON.stringify(val))
}, { deep: true })

const champion = computed(() => {
  const last = localRounds.value[localRounds.value.length - 1]
  return last?.[0]?.winner || null
})

const totalWidth = computed(() => localRounds.value[0].length * SLOT_BASE)

function slotWidth(roundIndex) {
  return SLOT_BASE * Math.pow(2, roundIndex)
}

function getRoundLabel(index) {
  const total = localRounds.value.length
  if (index === total - 1) return '決賽'
  if (index === total - 2) return '準決賽'
  if (index === total - 3) return '八強'
  return `第 ${index + 1} 輪`
}

function isBye(player) {
  return !player || player === 'BYE'
}

function canClick(match, player) {
  if (!player || player === '未知' || player === 'BYE') return false
  return true
}

function cascadeReset(roundIndex, matchIndex, player) {
  if (roundIndex >= localRounds.value.length) return
  const match = localRounds.value[roundIndex][matchIndex]
  if (match.player1 === player) match.player1 = '未知'
  if (match.player2 === player) match.player2 = '未知'
  if (match.winner === player) {
    match.winner = null
    cascadeReset(roundIndex + 1, Math.floor(matchIndex / 2), player)
  }
}

function selectWinner(roundIndex, matchIndex, winner) {
  const match = localRounds.value[roundIndex][matchIndex]
  if (!canClick(match, winner)) return
  if (match.winner === winner) return

  const oldWinner = match.winner
  if (oldWinner) cascadeReset(roundIndex + 1, Math.floor(matchIndex / 2), oldWinner)

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
.bracket-v-wrapper {
  overflow-x: auto;
  padding: 20px;
}

.bracket-v {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: max-content;
  margin: 0 auto;
}

/* 輪次標籤 */
.round-label-v {
  font-size: 12px;
  font-weight: 700;
  color: #6366f1;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  text-align: center;
  margin-bottom: 6px;
  width: v-bind(totalWidth + "px");
}

/* 比賽列 */
.matches-row-v {
  display: flex;
  flex-direction: row;
  margin-bottom: 0;
}

/* ── Match Slot ─────────────────────────────────── */
.match-slot-v {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex-shrink: 0;
}

.connector-svg {
  display: block;
  flex-shrink: 0;
}

/* ── Match Card ─────────────────────────────────── */
.match-card-v {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  width: min(calc(100% - 16px), 260px);
  background: #1e1e2e;
  border: 1px solid #313244;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
  z-index: 1;
}

/* Player 半側 */
.player-v {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 8px;
  min-height: 48px;
  transition: background 0.15s;
}

.player-v.clickable { cursor: pointer; }
.player-v.clickable:hover { background: #2a2a3e; }

.player-v.winner {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-bottom: 2px solid #6366f1;
}

.player-v.bye { opacity: 0.3; cursor: default; }

.name-v {
  font-size: 12px;
  color: #cdd6f4;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80px;
}

.player-v.winner .name-v { color: #a6e3a1; font-weight: 600; }
.trophy-v { font-size: 11px; flex-shrink: 0; }

/* VS 分隔 */
.vs-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  font-size: 9px;
  font-weight: 700;
  color: #585b70;
  border-left: 1px solid #313244;
  border-right: 1px solid #313244;
  flex-shrink: 0;
}

/* ── 冠軍 ─────────────────────────────────────────── */
.champion-row-v {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.champion-card-v {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: 2px solid #6366f1;
  border-radius: 16px;
  padding: 20px 32px;
  text-align: center;
  animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
  from { box-shadow: 0 0 20px rgba(99,102,241,0.3); }
  to   { box-shadow: 0 0 40px rgba(99,102,241,0.6); }
}

.crown { font-size: 32px; margin-bottom: 6px; }
.champion-name { font-size: 16px; font-weight: 700; color: #cba6f7; }
</style>
