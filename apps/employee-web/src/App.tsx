import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import './App.css'

type Profile = {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  status: string
  roles: string[]
}

type DashboardData = {
  title: string
  description: string
  kpis: { label: string; value: string }[]
  chart: { date: string; value: number }[]
  statusBreakdown: { status: string; count: number }[]
}

type SystemHealth = {
  status: string
  database: string
  reportingDatabase: string
  outboxLagSeconds: number
  failedOrders: number
  uptime: string
}

async function readJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    credentials: 'include',
    ...init,
    headers: {
      ...(init?.headers ?? {}),
    },
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Request failed')
  }

  return response.json() as Promise<T>
}

const CHART_COLORS = ['#34d399', '#60a5fa', '#fbbf24', '#f472b6', '#a78bfa']

function App() {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('admin123')
  const [profile, setProfile] = useState<Profile | null>(null)
  const [dashboard, setDashboard] = useState<DashboardData | null>(null)
  const [health, setHealth] = useState<SystemHealth | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const loadSession = async () => {
    try {
      const nextProfile = await readJson<Profile>('/api/auth/me')
      setProfile(nextProfile)

      const nextDashboard = await readJson<DashboardData>('/api/employee/dashboard')
      setDashboard(nextDashboard)

      const nextHealth = await readJson<SystemHealth>('/api/employee/system-health')
      setHealth(nextHealth)
    } catch {
      setProfile(null)
      setDashboard(null)
      setHealth(null)
    }
  }

  useEffect(() => {
    void loadSession()
  }, [])

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setError('')

    try {
      const csrf = await readJson<{ token: string }>('/api/auth/csrf')
      await readJson('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrf.token,
        },
        body: JSON.stringify({ username, password }),
      })

      await loadSession()
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = async () => {
    setLoading(true)
    try {
      await readJson('/api/auth/logout', { method: 'POST' })
      setProfile(null)
      setDashboard(null)
      setHealth(null)
    } finally {
      setLoading(false)
    }
  }

  const pieData = useMemo(
    () => (dashboard?.statusBreakdown ?? []).map((item, index) => ({ ...item, fill: CHART_COLORS[index % CHART_COLORS.length] })),
    [dashboard],
  )

  if (profile) {
    return (
      <main className="employee-shell">
        <aside className="employee-sidebar">
          <div>
            <p className="eyebrow">TradeWay</p>
            <h1>Operations</h1>
          </div>
          <nav>
            <span className="nav-item active">Dashboard</span>
            <span className="nav-item">Activity</span>
            <span className="nav-item">Users</span>
            <span className="nav-item">Monitoring</span>
          </nav>
          <button type="button" className="secondary" onClick={handleLogout} disabled={loading}>
            Sign out
          </button>
        </aside>

        <section className="employee-content">
          <header className="toolbar">
            <div>
              <p className="eyebrow">Employee portal</p>
              <h2>{profile.firstName} {profile.lastName}</h2>
            </div>
            <div className="badge-row">
              {profile.roles.map((role) => (
                <span key={role} className="badge">{role}</span>
              ))}
            </div>
          </header>

          <div className="kpi-grid">
            {(dashboard?.kpis ?? []).map((kpi) => (
              <article key={kpi.label} className="metric-card">
                <span>{kpi.label}</span>
                <strong>{kpi.value}</strong>
              </article>
            ))}
          </div>

          <div className="panel-grid two-up">
            <article className="panel">
              <div className="panel-header">
                <h3>Trading activity</h3>
                <span>Daily</span>
              </div>
              <div className="chart-wrap">
                <ResponsiveContainer width="100%" height={220}>
                  <AreaChart data={dashboard?.chart ?? []}>
                    <defs>
                      <linearGradient id="colorValue" x1="0" x2="0" y1="0" y2="1">
                        <stop offset="5%" stopColor="#34d399" stopOpacity={0.8} />
                        <stop offset="95%" stopColor="#34d399" stopOpacity={0.1} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                    <XAxis dataKey="date" stroke="#94a3b8" />
                    <YAxis stroke="#94a3b8" />
                    <Tooltip />
                    <Area type="monotone" dataKey="value" stroke="#34d399" fill="url(#colorValue)" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </article>

            <article className="panel">
              <div className="panel-header">
                <h3>Order status</h3>
                <span>Rollup</span>
              </div>
              <div className="chart-wrap">
                <ResponsiveContainer width="100%" height={220}>
                  <PieChart>
                    <Pie data={pieData} dataKey="count" nameKey="status" innerRadius={45} outerRadius={85}>
                      {pieData.map((entry) => (
                        <Cell key={entry.status} fill={entry.fill} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </article>
          </div>

          <div className="panel-grid">
            <article className="panel">
              <div className="panel-header">
                <h3>Volume by channel</h3>
                <span>Buy vs sell</span>
              </div>
              <div className="chart-wrap">
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={[
                    { name: 'Buy', value: 420800 },
                    { name: 'Sell', value: 395600 },
                    { name: 'Net', value: 25200 },
                  ]}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                    <XAxis dataKey="name" stroke="#94a3b8" />
                    <YAxis stroke="#94a3b8" />
                    <Tooltip />
                    <Bar dataKey="value" fill="#60a5fa" radius={[8, 8, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </article>

            <article className="panel">
              <div className="panel-header">
                <h3>Platform health</h3>
                <span>Monitoring</span>
              </div>
              <div className="status-list">
                {health ? (
                  <>
                    <div><span>Status</span><strong>{health.status}</strong></div>
                    <div><span>Primary DB</span><strong>{health.database}</strong></div>
                    <div><span>Reporting DB</span><strong>{health.reportingDatabase}</strong></div>
                    <div><span>Outbox lag</span><strong>{health.outboxLagSeconds}s</strong></div>
                    <div><span>Failed orders</span><strong>{health.failedOrders}</strong></div>
                    <div><span>Uptime</span><strong>{health.uptime}</strong></div>
                  </>
                ) : (
                  <p>Loading system health…</p>
                )}
              </div>
            </article>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="auth-shell">
      <section className="auth-card employee-auth-card">
        <p className="eyebrow">TradeWay</p>
        <h1>Employee access</h1>
        <p className="subtext">Secure operations login for admins and analysts.</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            Username
            <input value={username} onChange={(event) => setUsername(event.target.value)} />
          </label>

          <label>
            Password
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} />
          </label>

          {error ? <p className="error-message">{error}</p> : null}

          <button type="submit" disabled={loading}>
            {loading ? 'Verifying access…' : 'Open dashboard'}
          </button>
        </form>

        <div className="hint-box">
          <strong>Available demo roles</strong>
          <span>Admin: admin / admin123</span>
          <span>Analyst: admin / admin123</span>
        </div>
      </section>
    </main>
  )
}

export default App
