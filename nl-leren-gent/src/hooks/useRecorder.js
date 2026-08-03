import { useCallback, useRef, useState } from 'react'

/**
 * Kleine hook rond de MediaRecorder-API om audio op te nemen met de microfoon.
 *
 * Geeft terug:
 *   supported   – of opnemen mogelijk is in deze browser
 *   recording   – of er nu opgenomen wordt
 *   error       – eventuele foutmelding (bv. geen microfoontoestemming)
 *   start()     – begin met opnemen
 *   stop()      – stop; retourneert een Promise met de audio-Blob
 *   reset()     – wis de laatste opname/fout
 */
export function useRecorder() {
  const supported =
    typeof navigator !== 'undefined' &&
    !!navigator.mediaDevices?.getUserMedia &&
    typeof window !== 'undefined' &&
    'MediaRecorder' in window

  const [recording, setRecording] = useState(false)
  const [error, setError] = useState(null)

  const mediaRef = useRef(null)
  const chunksRef = useRef([])
  const streamRef = useRef(null)

  const start = useCallback(async () => {
    setError(null)
    if (!supported) {
      setError('Opnemen wordt niet ondersteund in deze browser.')
      return
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      streamRef.current = stream
      chunksRef.current = []

      // Kies een formaat dat de browser ondersteunt (Whisper aanvaardt webm/ogg/mp4).
      const mime =
        ['audio/webm', 'audio/ogg', 'audio/mp4'].find(
          (t) => window.MediaRecorder.isTypeSupported?.(t),
        ) || ''
      const rec = new MediaRecorder(stream, mime ? { mimeType: mime } : undefined)

      rec.ondataavailable = (e) => e.data.size > 0 && chunksRef.current.push(e.data)
      mediaRef.current = rec
      rec.start()
      setRecording(true)
    } catch (e) {
      setError(
        e?.name === 'NotAllowedError'
          ? 'Geen toestemming voor de microfoon. Sta het toe in je browser.'
          : 'Kon de microfoon niet starten.',
      )
    }
  }, [supported])

  const stop = useCallback(() => {
    return new Promise((resolve) => {
      const rec = mediaRef.current
      if (!rec || rec.state === 'inactive') {
        resolve(null)
        return
      }
      rec.onstop = () => {
        const blob = new Blob(chunksRef.current, { type: rec.mimeType || 'audio/webm' })
        streamRef.current?.getTracks().forEach((t) => t.stop())
        streamRef.current = null
        setRecording(false)
        resolve(blob)
      }
      rec.stop()
    })
  }, [])

  const reset = useCallback(() => setError(null), [])

  return { supported, recording, error, start, stop, reset }
}
