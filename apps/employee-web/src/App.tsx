import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
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

function App() {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('admin123')
  const [profile, setProfile] = useState<Profile | null>(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const loadSession = async () => {
    try {
      const user = await readJson<Profile>('/api/auth/me')
      setProfile(user)
      const adminSummary = await readJson<{ message?: string } | string>('/api/admin/summary')
      setMessage(typeof adminSummary === 'string' ? adminSummary : adminSummary.message || 'System healthy')
    } catch {
      setProfile(null)
      setMessage('')
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
      setMessage('')
    } finally {
      setLoading(false)
    }
  }

  if (profile) {
    return (
      <main className="dashboard-shell">
        <section className="dashboard-card">
          <div className="topbar">
            <div>
              <p className="eyebrow">Employee portal</p>
              <h1>Operations cockpit</h1>
            </div>
            <button type="button" onClick={handleLogout} disabled={loading}>
              Sign out
            </button>
          </div>

          <div className="summary-grid">
            <article>
              <span>Employee</span>
              <strong>{profile.firstName} {profile.lastName}</strong>
            </article>
            <article>
              <span>Username</span>
              <strong>{profile.username}</strong>
            </article>
            <article>
              <span>Roles</span>
              <strong>{profile.roles.join(', ')}</strong>
            </article>
          </div>

          <div className="system-status">
            <h2>System status</h2>
            <p>{message || 'No summary returned yet.'}</p>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">TradeWay</p>
        <h1>Employee login</h1>
        <p className="subtext">Sign in with an administrator account to review platform health.</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            Username
            <input value={username} onChange={(event) => setUsername(event.target.value)} />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>

          {error ? <p className="error-message">{error}</p> : null}

          <button type="submit" disabled={loading}>
            {loading ? 'Checking access…' : 'Continue'}
          </button>
        </form>

        <div className="hint-box">
          <strong>Admin demo</strong>
          <span>Username: admin</span>
          <span>Password: admin123</span>
        </div>
      </section>
    </main>
  )
}

export default App
