<template>
  <div class="app">
    <header class="header">
      <h1>🏆 三重陀螺宮陀螺賽淘汰賽系統</h1>
      <p class="subtitle">單淘汰制 · 兩人一組對決</p>
    </header>

    <!-- 設定區 -->
    <div v-if="!bracket" class="setup-panel">
      <div class="card">
        <h2>建立賽事</h2>

        <div class="field">
          <label>參賽人數</label>
          <div class="count-row">
            <input
              type="number"
              v-model.number="playerCount"
              min="2"
              max="64"
              class="count-input"
              @input="clampMax"
            />
            <div class="quick-btns">
              <button
                v-for="n in [8, 16, 32, 64]"
                :key="n"
                class="quick-btn"
                :class="{ active: playerCount === n }"
                @click="playerCount = n"
              >{{ n }}</button>
            </div>
          </div>
          <p class="hint">2 ~ 64 人 · 實際 bracket 大小：{{ bracketSize }} 人（2 的冪次）</p>
        </div>

        <div class="field">
          <label>輸入選手名稱（選填）</label>
          <div class="names-grid">
            <div
              v-for="i in playerCount"
              :key="i"
              class="name-input-wrapper"
            >
              <span class="name-index">{{ i }}</span>
              <input
                v-model="playerNames[i - 1]"
                :placeholder="`Player ${i}`"
                class="name-input"
                maxlength="20"
              />
            </div>
          </div>
        </div>

        <div class="btn-group">
          <button class="btn-generate" @click="generate(false, 'horizontal')" :disabled="loading">
            <span v-if="loading">產生中...</span>
            <span v-else>產生淘汰賽 →</span>
          </button>
          <button class="btn-random" @click="generate(true, 'horizontal')" :disabled="loading">
            🔀 產生隨機淘汰賽
          </button>
          <button class="btn-generate btn-vertical" @click="generate(false, 'vertical')" :disabled="loading">
            產生淘汰賽 - 直列式 ↓
          </button>
          <button class="btn-random btn-vertical" @click="generate(true, 'vertical')" :disabled="loading">
            🔀 產生隨機淘汰賽 - 直列式
          </button>
        </div>
      </div>
    </div>

    <!-- 對戰表 -->
    <div v-else class="bracket-panel">
      <div class="bracket-toolbar">
        <div class="bracket-info">
          <span>{{ bracket.totalPlayers }} 位選手</span>
          <span class="sep">·</span>
          <span>{{ bracket.rounds.length }} 輪</span>
          <span class="sep">·</span>
          <span>點擊選手選擇勝者</span>
        </div>
        <button class="btn-reset" @click="reset">重新設定</button>
      </div>

      <TournamentBracket
        v-if="bracketMode === 'horizontal'"
        :rounds="bracket.rounds"
        @update:rounds="onRoundsUpdate"
      />
      <TournamentBracketVertical
        v-else
        :rounds="bracket.rounds"
        @update:rounds="onRoundsUpdate"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import TournamentBracket from './components/TournamentBracket.vue'
import TournamentBracketVertical from './components/TournamentBracketVertical.vue'

const playerCount = ref(16)
const playerNames = ref(Array(64).fill(''))
const bracket = ref(null)
const bracketMode = ref('horizontal')
const loading = ref(false)

const bracketSize = computed(() => {
  let size = 1
  while (size < playerCount.value) size *= 2
  return size
})

function clampMax() {
  if (playerCount.value > 64) playerCount.value = 64
  if (playerCount.value) playerCount.value = Math.floor(playerCount.value)
}

async function generate(shuffle = false, mode = 'horizontal') {
  bracketMode.value = mode
  const count = playerCount.value
  if (!count || count < 2 || count > 64) {
    alert('請輸入有效的參賽人數（2 ~ 64 人）')
    return
  }
  loading.value = true
  try {
    const names = playerNames.value.slice(0, playerCount.value)
    const res = await axios.post('/api/tournament/generate', {
      playerCount: playerCount.value,
      playerNames: shuffle ? shuffled(names) : names
    })
    bracket.value = res.data
  } catch (e) {
    bracket.value = generateLocal(shuffle)
  } finally {
    loading.value = false
  }
}

function shuffled(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

function generateLocal(shuffle = false) {
  const count = playerCount.value
  let names = []
  for (let i = 0; i < count; i++) {
    names.push(playerNames.value[i]?.trim() || `Player ${i + 1}`)
  }
  if (shuffle) names = shuffled(names)

  let size = 1
  while (size < count) size *= 2
  while (names.length < size) names.push('BYE')

  const rounds = []
  let matchId = 1

  // Round 1
  const firstRound = []
  for (let i = 0; i < size; i += 2) {
    firstRound.push({ matchId: matchId++, player1: names[i], player2: names[i + 1], winner: null })
  }
  rounds.push(firstRound)

  // Remaining rounds
  let matchesInRound = size / 4
  while (matchesInRound >= 1) {
    const round = []
    for (let i = 0; i < matchesInRound; i++) {
      round.push({ matchId: matchId++, player1: '未知', player2: '未知', winner: null })
    }
    rounds.push(round)
    matchesInRound = Math.floor(matchesInRound / 2)
  }

  return { totalPlayers: count, bracketSize: size, rounds }
}

function onRoundsUpdate(newRounds) {
  bracket.value = { ...bracket.value, rounds: newRounds }
}

function reset() {
  bracket.value = null
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  background: #11111b;
  color: #cdd6f4;
  font-family: 'Segoe UI', system-ui, sans-serif;
  min-height: 100vh;
}

.app {
  min-height: 100vh;
}

.header {
  background: linear-gradient(135deg, #1e1e2e, #181825);
  border-bottom: 1px solid #313244;
  padding: 28px 40px;
  text-align: center;
}

.header h1 {
  font-size: 28px;
  font-weight: 800;
  color: #cba6f7;
  letter-spacing: -0.5px;
}

.subtitle {
  margin-top: 6px;
  font-size: 14px;
  color: #6c7086;
}

/* 設定區 */
.setup-panel {
  max-width: 600px;
  margin: 48px auto;
  padding: 0 20px;
}

.card {
  background: #1e1e2e;
  border: 1px solid #313244;
  border-radius: 16px;
  padding: 32px;
}

.card h2 {
  font-size: 20px;
  font-weight: 700;
  color: #cdd6f4;
  margin-bottom: 28px;
}

.field {
  margin-bottom: 28px;
}

.field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #a6adc8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}

.count-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quick-btns {
  display: flex;
  gap: 6px;
}

.quick-btn {
  padding: 8px 14px;
  background: #181825;
  border: 1px solid #45475a;
  border-radius: 8px;
  color: #a6adc8;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.quick-btn:hover {
  border-color: #6366f1;
  color: #cdd6f4;
}

.quick-btn.active {
  background: #6366f1;
  border-color: #6366f1;
  color: white;
}

.count-input {
  width: 120px;
  padding: 10px 14px;
  background: #181825;
  border: 1px solid #45475a;
  border-radius: 10px;
  color: #cba6f7;
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  outline: none;
  transition: border-color 0.15s;
}

.count-input:focus {
  border-color: #6366f1;
}

.count-input::-webkit-inner-spin-button,
.count-input::-webkit-outer-spin-button {
  opacity: 1;
}

.hint {
  margin-top: 8px;
  font-size: 12px;
  color: #585b70;
}

.names-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
  padding-right: 4px;
}

.name-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #181825;
  border: 1px solid #313244;
  border-radius: 8px;
  padding: 6px 10px;
}

.name-index {
  font-size: 11px;
  color: #585b70;
  width: 18px;
  flex-shrink: 0;
  text-align: right;
}

.name-input {
  background: transparent;
  border: none;
  outline: none;
  color: #cdd6f4;
  font-size: 13px;
  width: 100%;
}

.name-input::placeholder {
  color: #45475a;
}

.btn-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.btn-generate {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.1s;
}

.btn-random {
  width: 100%;
  padding: 14px;
  background: transparent;
  border: 1px solid #45475a;
  border-radius: 12px;
  color: #a6adc8;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-random:hover:not(:disabled) {
  border-color: #f9e2af;
  color: #f9e2af;
  background: rgba(249, 226, 175, 0.05);
}

.btn-vertical {
  border-style: dashed;
}

.btn-generate.btn-vertical {
  background: linear-gradient(135deg, #1e6352, #2d7a5f);
}

.btn-random.btn-vertical:hover:not(:disabled) {
  border-color: #94e2d5;
  color: #94e2d5;
  background: rgba(148, 226, 213, 0.05);
}

.btn-generate:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-generate:active,
.btn-random:active {
  transform: translateY(0);
}

.btn-generate:disabled,
.btn-random:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 對戰表 */
.bracket-panel {
  padding: 24px 32px;
}

.bracket-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.bracket-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #a6adc8;
}

.sep {
  color: #45475a;
}

.btn-reset {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid #45475a;
  border-radius: 8px;
  color: #a6adc8;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-reset:hover {
  border-color: #f38ba8;
  color: #f38ba8;
}
</style>
