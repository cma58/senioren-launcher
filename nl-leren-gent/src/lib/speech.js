/**
 * Browser-native Text-to-Speech (Web Speech API).
 * Leest Nederlandse tekst voor, bij voorkeur met een Vlaamse (nl-BE) stem.
 * Geen externe kosten of API-sleutels nodig.
 *
 * In Stap 2 wordt dit uitgebreid met de oefen-flow; hier is het de basis.
 */

let cachedVoice = null

/** Kies de beste beschikbare Nederlandse stem (Vlaams eerst). */
function pickDutchVoice() {
  if (cachedVoice) return cachedVoice
  const voices = window.speechSynthesis?.getVoices?.() ?? []
  cachedVoice =
    voices.find((v) => v.lang === 'nl-BE') ||
    voices.find((v) => v.lang?.startsWith('nl')) ||
    null
  return cachedVoice
}

// Stemmen laden soms asynchroon in.
if (typeof window !== 'undefined' && window.speechSynthesis) {
  window.speechSynthesis.onvoiceschanged = () => {
    cachedVoice = null
    pickDutchVoice()
  }
}

/** Is voorlezen beschikbaar in deze browser? */
export function isTTSAvailable() {
  return typeof window !== 'undefined' && 'speechSynthesis' in window
}

/**
 * Lees een tekst voor in het Nederlands.
 * @param {string} text
 * @param {{ rate?: number, lang?: string }} [opts]
 */
export function speak(text, opts = {}) {
  if (!isTTSAvailable() || !text) return
  window.speechSynthesis.cancel() // stop wat nog bezig is
  const u = new SpeechSynthesisUtterance(text)
  u.lang = opts.lang || 'nl-BE'
  u.rate = opts.rate ?? 0.9 // iets trager voor beginners
  const voice = pickDutchVoice()
  if (voice) u.voice = voice
  window.speechSynthesis.speak(u)
}
