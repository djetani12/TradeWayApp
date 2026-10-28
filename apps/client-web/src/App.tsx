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

type Account = {
  id: number
  accountNumber: string
  status: string
  cashBalance: number
  buyingPower: number
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
  const [username, setUsername] = useState('demo')
  const [password, setPassword] = useState('demo123')
  const [profile, setProfile] = useState<Profile | null>(null)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const loadSession = async () => {
    try {
      const user = await readJson<Profile>('/api/auth/me')
      setProfile(user)
      const accountList = await readJson<Account[]>('/api/accounts')
      setAccounts(accountList)
    } catch {
      setProfile(null)
      setAccounts([])
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
      setAccounts([])
    } catch {
      setProfile(null)
      setAccounts([])
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
              <p className="eyebrow">Customer portal</p>
              <h1>Welcome back, {profile.firstName}</h1>
            </div>
            <button type="button" onClick={handleLogout} disabled={loading}>
              Sign out
            </button>
          </div>

          <div className="summary-grid">
            <article>
              <span>Username</span>
              <strong>{profile.username}</strong>
            </article>
            <article>
              <span>Email</span>
              <strong>{profile.email}</strong>
            </article>
            <article>
              <span>Roles</span>
              <strong>{profile.roles.join(', ')}</strong>
            </article>
          </div>

          <div className="account-list">
            <h2>Your accounts</h2>
            {accounts.length === 0 ? (
              <p>No accounts available.</p>
            ) : (
              <ul>
                {accounts.map((account) => (
                  <li key={account.id}>
                    <div>
                      <strong>{account.accountNumber}</strong>
                      <span>{account.status}</span>
                    </div>
                    <div className="account-values">
                      <span>Cash: ${account.cashBalance.toFixed(2)}</span>
                      <span>Buying power: ${account.buyingPower.toFixed(2)}</span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">TradeWay</p>
        <h1>Customer login</h1>
        <p className="subtext">Sign in with the demo customer account to preview the dashboard.</p>

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
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <div className="hint-box">
          <strong>Demo account</strong>
          <span>Username: demo</span>
          <span>Password: demo123</span>
        </div>
      </section>
    </main>
  )
}

export default App
