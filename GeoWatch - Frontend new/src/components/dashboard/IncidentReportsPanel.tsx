import type { IncidentResponse } from '../../services/api'
import RiskBadge from './RiskBadge'

interface IncidentReportsPanelProps {
  incidents: IncidentResponse[]
  loading?: boolean
  error?: string
}

function IncidentReportsPanel({ incidents, loading, error }: IncidentReportsPanelProps) {
  if (loading) {
    return (
      <div className="rounded-2xl border border-mid/40 bg-deep/60 p-[18px] text-center backdrop-blur-xl">
        <p className="text-sm text-muted">Loading incident reports...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="rounded-2xl border border-rose-500/30 bg-rose-500/10 p-[18px] text-center backdrop-blur-xl">
        <p className="text-sm text-rose-300">Unable to load incident reports.</p>
      </div>
    )
  }

  if (!incidents.length) {
    return (
      <div className="rounded-2xl border border-mid/40 bg-deep/60 p-[18px] text-center backdrop-blur-xl">
        <p className="text-sm text-muted">No incidents reported yet.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-3 max-h-[600px] overflow-y-auto pr-1">
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-sm font-semibold text-light uppercase tracking-wider">Live Incident Reports</h3>
        <span className="text-xs font-medium text-muted">{incidents.length} total</span>
      </div>
      
      {incidents.map((incident) => {
        const hasDescription = incident.description && incident.description.trim().length > 0
        const isResolved = incident.resolved
        
        return (
          <div
            key={incident.id}
            className={`flex flex-col gap-2 rounded-2xl border p-4 backdrop-blur-xl transition ${
              isResolved ? 'border-mid/30 bg-deepest/50 opacity-70' : 'border-mid/60 bg-surface/80 shadow-lg shadow-black/20'
            }`}
          >
            <div className="flex items-start justify-between gap-3">
              <div className="flex flex-col gap-1.5 flex-1 min-w-0">
                <p className={`text-sm leading-relaxed ${hasDescription ? 'text-[#ccd0cf]' : 'text-muted italic'}`}>
                  {hasDescription ? `"${incident.description}"` : 'No description provided'}
                </p>
                
                <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted">
                  <span>Incident #{incident.id}</span>
                  <span>•</span>
                  <span>{new Date(incident.timestamp).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}</span>
                  {incident.incidentType && (
                    <>
                      <span>•</span>
                      <span className="truncate">{incident.incidentType}</span>
                    </>
                  )}
                </div>
              </div>
              
              <div className="flex flex-col items-end gap-2 shrink-0">
                <RiskBadge level={(incident.semanticRisk || 'LOW') as 'HIGH' | 'MEDIUM' | 'LOW'} />
                {isResolved && (
                  <span className="rounded bg-emerald-500/20 px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wider text-emerald-400">
                    Resolved
                  </span>
                )}
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}

export default IncidentReportsPanel
