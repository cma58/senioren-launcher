/**
 * Google Gemini API — antwoord- en grammaticacheck.
 *
 * `evaluateAnswer` stuurt het antwoord van de leerling naar Gemini met een
 * systeemprompt in de rol van een geduldige NT2-docent in Gent. Het resultaat
 * komt terug als gestructureerde JSON, zodat de UI het netjes kan tonen.
 */

import { getGeminiKey, GEMINI_MODEL } from './config.js'

const SYSTEM_PROMPT = `Je bent een geduldige NT2-docent in Gent (Vlaanderen). De leerling heeft als moedertaal Marokkaans-Arabisch (Darija) en komt uit Oujda. Bekijk het antwoord van de gebruiker op de opdracht. Controleer op grammatica en spelling. Als het goed is, geef een kort compliment. Als het fout is, leg kort en eenvoudig in het Nederlands uit wat er mis is en geef de juiste correctie. Voeg altijd een korte, bemoedigende toelichting in het Darija (Arabisch schrift) toe waar dat nuttig is. Wees vriendelijk en gebruik eenvoudige taal, want de leerling is een absolute beginner.`

// Gestructureerd antwoord zodat de app het betrouwbaar kan weergeven.
const RESPONSE_SCHEMA = {
  type: 'object',
  properties: {
    correct: { type: 'boolean' },
    feedback_nl: { type: 'string' },
    feedback_darija: { type: 'string' },
    correction: { type: 'string' },
  },
  required: ['correct', 'feedback_nl'],
}

/**
 * @param {string} userAnswer      Wat de leerling zei/typte.
 * @param {string} expectedAnswer  De verwachte zin/opdracht.
 * @param {string} [contextPrompt] Extra context (bv. de instructie van de les).
 * @returns {Promise<{correct:boolean, feedback_nl:string, feedback_darija?:string, correction?:string}>}
 */
export async function evaluateAnswer(userAnswer, expectedAnswer, contextPrompt = '') {
  const key = getGeminiKey()
  if (!key) throw new Error('GEEN_GEMINI_SLEUTEL')

  const userText = [
    contextPrompt && `Opdracht/context: ${contextPrompt}`,
    `Verwacht antwoord: "${expectedAnswer}"`,
    `Antwoord van de gebruiker: "${userAnswer}"`,
  ]
    .filter(Boolean)
    .join('\n')

  const url =
    `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=` +
    encodeURIComponent(key)

  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      systemInstruction: { parts: [{ text: SYSTEM_PROMPT }] },
      contents: [{ role: 'user', parts: [{ text: userText }] }],
      generationConfig: {
        temperature: 0.3,
        responseMimeType: 'application/json',
        responseSchema: RESPONSE_SCHEMA,
      },
    }),
  })

  if (!res.ok) {
    const detail = await res.text().catch(() => '')
    throw new Error(`Gemini-fout (${res.status}): ${detail.slice(0, 200)}`)
  }

  const data = await res.json()
  const text = data?.candidates?.[0]?.content?.parts?.[0]?.text
  if (!text) throw new Error('Leeg antwoord van Gemini.')

  try {
    return JSON.parse(text)
  } catch {
    // Terugval: toon de ruwe tekst als feedback.
    return { correct: false, feedback_nl: text }
  }
}
