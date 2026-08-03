import { useState } from 'react'
import Header from './components/Header.jsx'
import Dashboard from './components/Dashboard.jsx'
import LevelView from './components/LevelView.jsx'
import LessonPlayer from './components/LessonPlayer.jsx'
import Settings from './components/Settings.jsx'

/**
 * Wortelcomponent. Eenvoudige navigatie via lokale state (geen router nodig
 * voor deze omvang): 'dashboard' <-> een geopend niveau, met een optioneel
 * les-speler of instellingenpaneel erbovenop.
 */
export default function App() {
  const [activeLevel, setActiveLevel] = useState(null)
  const [activeLesson, setActiveLesson] = useState(null)
  const [showSettings, setShowSettings] = useState(false)

  return (
    <div className="min-h-dvh">
      <Header
        onBack={activeLevel ? () => setActiveLevel(null) : undefined}
        subtitle={activeLevel ? `${activeLevel.title} · ${activeLevel.subtitle}` : undefined}
        onSettings={() => setShowSettings(true)}
      />

      <main className="pb-16">
        {activeLevel ? (
          <LevelView level={activeLevel} onOpenLesson={setActiveLesson} />
        ) : (
          <Dashboard onOpenLevel={setActiveLevel} />
        )}
      </main>

      {activeLesson && (
        <LessonPlayer
          lesson={activeLesson}
          onClose={() => setActiveLesson(null)}
          onOpenSettings={() => setShowSettings(true)}
        />
      )}

      {showSettings && <Settings onClose={() => setShowSettings(false)} />}
    </div>
  )
}
