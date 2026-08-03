/**
 * Maakt automatisch meerkeuzevragen uit de items van een les.
 * Werkt volledig lokaal — geen API of internet nodig.
 *
 * Idee: voor elk 'oefenbaar' item tonen we een aanwijzing (de Darija-vertaling,
 * of bij getallen het cijfer) en laten we de gebruiker het juiste Nederlandse
 * woord kiezen uit enkele opties.
 */

/** Door de war schudden (Fisher–Yates), geeft een nieuwe array terug. */
export function shuffle(arr) {
  const a = arr.slice()
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

/** De aanwijzing (vraagtekst) voor een item, of null als het niet bruikbaar is. */
function promptFor(item, lessonType) {
  if (lessonType === 'numbers' && typeof item.value === 'number') {
    return { text: String(item.value), rtl: false }
  }
  if (item.darija) return { text: item.darija, rtl: true }
  if (item.darijaLat) return { text: item.darijaLat, rtl: false }
  return null
}

/**
 * Bouw een lijst quizvragen uit een les.
 * @returns {Array<{ prompt, rtl, answer, options }>} of [] als quiz niet kan.
 */
export function buildQuiz(lesson) {
  // Alleen items met een bruikbare aanwijzing én een uniek NL-antwoord.
  const seen = new Set()
  const usable = lesson.items.filter((item) => {
    if (!item.nl || seen.has(item.nl)) return false
    if (!promptFor(item, lesson.type)) return false
    seen.add(item.nl)
    return true
  })

  // Met minder dan 3 items is een meerkeuzequiz niet zinvol.
  if (usable.length < 3) return []

  const allAnswers = usable.map((i) => i.nl)

  return shuffle(usable).map((item) => {
    const { text, rtl } = promptFor(item, lesson.type)
    const distractors = shuffle(allAnswers.filter((a) => a !== item.nl)).slice(0, 3)
    return {
      prompt: text,
      rtl,
      answer: item.nl,
      options: shuffle([item.nl, ...distractors]),
    }
  })
}
