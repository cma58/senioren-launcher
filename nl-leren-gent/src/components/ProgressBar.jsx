/**
 * Eenvoudige, toegankelijke voortgangsbalk.
 * ratio = getal tussen 0 en 1.
 */
export default function ProgressBar({ ratio = 0, accent = 'gent', showLabel = true }) {
  const pct = Math.round(Math.min(1, Math.max(0, ratio)) * 100)
  const fill = accent === 'saffraan' ? 'bg-saffraan-500' : 'bg-gent-600'

  return (
    <div className="flex items-center gap-3">
      <div
        className="h-2.5 flex-1 overflow-hidden rounded-full bg-slate-200"
        role="progressbar"
        aria-valuenow={pct}
        aria-valuemin={0}
        aria-valuemax={100}
      >
        <div
          className={`h-full rounded-full ${fill} transition-[width] duration-500 ease-out`}
          style={{ width: `${pct}%` }}
        />
      </div>
      {showLabel && (
        <span className="w-10 shrink-0 text-right text-sm font-semibold tabular-nums text-slate-600">
          {pct}%
        </span>
      )}
    </div>
  )
}
