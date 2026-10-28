import { useEffect, useMemo, useState } from 'react'
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

type Instrument = {
  id: number
  symbol: string
  name: string
  assetClass: string
  price: number
  active: boolean
}

type Position = {
  symbol: string
  quantity: number
  averageCost: number
}

type Order = {
  id: number
  accountNumber: string
  symbol: string
  side: 'BUY' | 'SELL'
  status: 'PENDING' | 'FILLED' | 'REJECTED' | 'CANCELLED'
  quantity: number
  filledQuantity: number
  remainingQuantity: number
  averageFillPrice: number | null
  reason: string | null
}

type AlertState = {
  kind: 'success' | 'error' | 'info'
  text: string
}

type AuthMode = 'login' | 'register'

type RegistrationForm = {
  username: string
  email: string
  password: string
  firstName: string
  lastName: string
}

const defaultRegistration: RegistrationForm = {
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
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

const formatCurrency = (value: number) =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
  }).format(value)

const formatCompact = (value: number) =>
  new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 0,
  }).format(value)

function App() {
  const [authMode, setAuthMode] = useState<AuthMode>('login')
  const [loginUsername, setLoginUsername] = useState('demo')
  const [loginPassword, setLoginPassword] = useState('demo123')
  const [registration, setRegistration] = useState<RegistrationForm>(defaultRegistration)
  const [profile, setProfile] = useState<Profile | null>(null)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null)
  const [market, setMarket] = useState<Instrument[]>([])
  const [positions, setPositions] = useState<Position[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [orderSymbol, setOrderSymbol] = useState('AAPL')
  const [orderSide, setOrderSide] = useState<'BUY' | 'SELL'>('BUY')
  const [orderQuantity, setOrderQuantity] = useState(10)
  const [appError, setAppError] = useState('')
  const [alert, setAlert] = useState<AlertState | null>(null)
  const [loading, setLoading] = useState(false)
  const [dashboardLoading, setDashboardLoading] = useState(false)
  const [accountSettings, setAccountSettings] = useState({
    riskLevel: 'Moderate',
    maxDailyLoss: 2500,
    notifications: true,
    accessibility: 'High contrast',
  })

  const marketBySymbol = useMemo(
    () => Object.fromEntries(market.map((instrument) => [instrument.symbol, instrument])),
    [market],
  )

  const selectedInstrument = marketBySymbol[orderSymbol.toUpperCase()] ?? null

  const portfolioSummary = useMemo(() => {
    const marketValue = positions.reduce((total, position) => {
      const price = marketBySymbol[position.symbol]?.price ?? position.averageCost
      return total + position.quantity * price
    }, 0)

    const pnl = positions.reduce((total, position) => {
      const price = marketBySymbol[position.symbol]?.price ?? position.averageCost
      return total + (price - position.averageCost) * position.quantity
    }, 0)

    return {
      cashBalance: selectedAccount?.cashBalance ?? 0,
      totalMarketValue: marketValue,
      totalPortfolioValue: (selectedAccount?.cashBalance ?? 0) + marketValue,
      pnl,
      buyingPower: selectedAccount?.buyingPower ?? 0,
    }
  }, [marketBySymbol, positions, selectedAccount])

  const diversification = useMemo(() => {
    const map = new Map<string, number>()

    positions.forEach((position) => {
      const instrument = marketBySymbol[position.symbol]
      const value = position.quantity * (instrument?.price ?? position.averageCost)
      const key = instrument?.assetClass ?? 'Unassigned'
      map.set(key, (map.get(key) ?? 0) + value)
    })

    return [...map.entries()]
      .map(([name, value]) => ({ name, value }))
      .sort((left, right) => right.value - left.value)
  }, [marketBySymbol, positions])

  const pendingOrders = orders.filter((order) => order.status === 'PENDING')

  const loadDashboard = async () => {
    try {
      setDashboardLoading(true)
      const user = await readJson<Profile>('/api/auth/me')
      setProfile(user)

      const accountList = await readJson<Account[]>('/api/accounts')
      setAccounts(accountList)
      setSelectedAccount((current) => {
        if (current && accountList.some((account) => account.accountNumber === current.accountNumber)) {
          return current
        }
        return accountList[0] ?? null
      })

      const marketData = await readJson<Instrument[]>('/api/instruments')
      setMarket(marketData)

      if (marketData.length > 0 && !marketData.some((instrument) => instrument.symbol === orderSymbol.toUpperCase())) {
        setOrderSymbol(marketData[0].symbol)
      }

      const positionsData = await readJson<Position[]>('/api/trading/portfolio')
      setPositions(positionsData)

      const orderData = await readJson<Order[]>('/api/trading/orders')
      setOrders(orderData)
    } catch {
      setProfile(null)
      setAccounts([])
      setSelectedAccount(null)
      setPositions([])
      setOrders([])
      setMarket([])
    } finally {
      setDashboardLoading(false)
    }
  }

  useEffect(() => {
    void loadDashboard()
  }, [])

  useEffect(() => {
    if (!profile) {
      return
    }

    const interval = window.setInterval(() => {
      void loadDashboard()
    }, 15000)

    return () => window.clearInterval(interval)
  }, [profile])

  const handleLogin = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setAppError('')

    try {
      const csrf = await readJson<{ token: string }>('/api/auth/csrf')
      await readJson('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrf.token,
        },
        body: JSON.stringify({ username: loginUsername, password: loginPassword }),
      })

      await loadDashboard()
    } catch (submitError) {
      setAppError(submitError instanceof Error ? submitError.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setAppError('')

    try {
      const csrf = await readJson<{ token: string }>('/api/auth/csrf')
      await readJson('/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrf.token,
        },
        body: JSON.stringify({
          username: registration.username,
          email: registration.email,
          password: registration.password,
          firstName: registration.firstName,
          lastName: registration.lastName,
        }),
      })

      setAuthMode('login')
      setLoginUsername(registration.username)
      setLoginPassword(registration.password)
      setRegistration(defaultRegistration)
      setAlert({ kind: 'success', text: 'Account created successfully. Welcome to TradeWay.' })
      await loadDashboard()
    } catch (submitError) {
      setAppError(submitError instanceof Error ? submitError.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = async () => {
    setLoading(true)
    try {
      const csrf = await readJson<{ token: string }>('/api/auth/csrf')
      await readJson('/api/auth/logout', {
        method: 'POST',
        headers: { 'X-XSRF-TOKEN': csrf.token },
      })
      setProfile(null)
      setAccounts([])
      setSelectedAccount(null)
      setMarket([])
      setPositions([])
      setOrders([])
      setAlert(null)
    } catch {
      setProfile(null)
      setAccounts([])
      setSelectedAccount(null)
      setMarket([])
      setPositions([])
      setOrders([])
    } finally {
      setLoading(false)
    }
  }

  const handleTrade = async (event: FormEvent) => {
    event.preventDefault()
    if (!selectedAccount) {
      setAlert({ kind: 'error', text: 'Select an account before placing a trade.' })
      return
    }

    setLoading(true)
    setAlert(null)

    try {
      const csrf = await readJson<{ token: string }>('/api/auth/csrf')
      await readJson('/api/trading/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrf.token,
        },
        body: JSON.stringify({
          symbol: orderSymbol.toUpperCase(),
          side: orderSide,
          quantity: Number(orderQuantity),
        }),
      })

      setAlert({ kind: 'success', text: `${orderSide} order for ${orderSymbol.toUpperCase()} was placed successfully.` })
      await loadDashboard()
    } catch (tradeError) {
      setAlert({
        kind: 'error',
        text: tradeError instanceof Error ? tradeError.message : 'Trade could not be placed.',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleCancelOrder = async (orderId: number) => {
    setLoading(true)
    setAlert(null)

    try {
      const csrf = await readJson<{ token: string }>('/api/auth/csrf')
      await readJson(`/api/trading/orders/${orderId}/cancel`, {
        method: 'POST',
        headers: { 'X-XSRF-TOKEN': csrf.token },
      })
      setAlert({ kind: 'info', text: 'Pending order cancelled.' })
      await loadDashboard()
    } catch (cancelError) {
      setAlert({
        kind: 'error',
        text: cancelError instanceof Error ? cancelError.message : 'Order cancellation failed.',
      })
    } finally {
      setLoading(false)
    }
  }

  const renderAuth = () => (
    <main className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">TradeWay</p>
        <h1>{authMode === 'login' ? 'Customer login' : 'Create account'}</h1>
        <p className="subtext">
          {authMode === 'login'
            ? 'Sign in to review your account, positions, and live order flow.'
            : 'Open a new investor profile with a seeded demo cash balance.'}
        </p>

        <div className="tab-row" role="tablist" aria-label="Authentication mode selector">
          <button
            type="button"
            className={authMode === 'login' ? 'tab-button active' : 'tab-button'}
            onClick={() => setAuthMode('login')}
          >
            Login
          </button>
          <button
            type="button"
            className={authMode === 'register' ? 'tab-button active' : 'tab-button'}
            onClick={() => setAuthMode('register')}
          >
            Register
          </button>
        </div>

        {authMode === 'login' ? (
          <form className="auth-form" onSubmit={handleLogin}>
            <label>
              Username
              <input value={loginUsername} onChange={(event) => setLoginUsername(event.target.value)} />
            </label>

            <label>
              Password
              <input
                type="password"
                value={loginPassword}
                onChange={(event) => setLoginPassword(event.target.value)}
              />
            </label>

            {appError ? <p className="error-message">{appError}</p> : null}

            <button type="submit" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
        ) : (
          <form className="auth-form" onSubmit={handleRegister}>
            <div className="inline-fields">
              <label>
                First name
                <input
                  value={registration.firstName}
                  onChange={(event) =>
                    setRegistration((current) => ({ ...current, firstName: event.target.value }))
                  }
                />
              </label>
              <label>
                Last name
                <input
                  value={registration.lastName}
                  onChange={(event) =>
                    setRegistration((current) => ({ ...current, lastName: event.target.value }))
                  }
                />
              </label>
            </div>

            <label>
              Username
              <input
                value={registration.username}
                onChange={(event) =>
                  setRegistration((current) => ({ ...current, username: event.target.value }))
                }
              />
            </label>

            <label>
              Email
              <input
                type="email"
                value={registration.email}
                onChange={(event) =>
                  setRegistration((current) => ({ ...current, email: event.target.value }))
                }
              />
            </label>

            <label>
              Password
              <input
                type="password"
                value={registration.password}
                onChange={(event) =>
                  setRegistration((current) => ({ ...current, password: event.target.value }))
                }
              />
            </label>

            {appError ? <p className="error-message">{appError}</p> : null}

            <button type="submit" disabled={loading}>
              {loading ? 'Creating…' : 'Create account'}
            </button>
          </form>
        )}

        <div className="hint-box">
          <strong>Demo access</strong>
          <span>Username: demo</span>
          <span>Password: demo123</span>
        </div>
      </section>
    </main>
  )

  if (!profile) {
    return renderAuth()
  }

  const orderHistory = orders.slice(0, 5)

  return (
    <main className="dashboard-shell">
      <div className="dashboard-layout">
        <aside className="sidebar">
          <div className="brand-header">
            <div className="brand-mark">TW</div>
            <div>
              <p className="eyebrow">Client portal</p>
              <h2>TradeWay</h2>
            </div>
          </div>

          <div className="sidebar-card">
            <span>Active account</span>
            <select
              value={selectedAccount?.accountNumber ?? ''}
              onChange={(event) => {
                const nextAccount = accounts.find((account) => account.accountNumber === event.target.value)
                setSelectedAccount(nextAccount ?? null)
              }}
            >
              {accounts.length === 0 ? (
                <option value="">No account</option>
              ) : (
                accounts.map((account) => (
                  <option key={account.accountNumber} value={account.accountNumber}>
                    {account.accountNumber}
                  </option>
                ))
              )}
            </select>
            <small>{profile.firstName} {profile.lastName}</small>
          </div>

          <div className="sidebar-card muted">
            <span>Market status</span>
            <strong>Open</strong>
            <small>NYSE · 09:30 - 16:00 ET</small>
          </div>
        </aside>

        <section className="main-panel">
          <header className="topbar">
            <div>
              <p className="eyebrow">Welcome back</p>
              <h1>{profile.firstName} {profile.lastName}</h1>
            </div>
            <div className="topbar-actions">
              <button type="button" className="ghost-button" onClick={() => void loadDashboard()}>
                {dashboardLoading ? 'Refreshing…' : 'Refresh'}
              </button>
              <button type="button" onClick={handleLogout} disabled={loading}>
                Sign out
              </button>
            </div>
          </header>

          <div className="summary-grid">
            <article className="stat-card accent">
              <span>Total portfolio</span>
              <strong>{formatCurrency(portfolioSummary.totalPortfolioValue)}</strong>
              <small>{portfolioSummary.totalMarketValue > 0 ? `${formatCurrency(portfolioSummary.totalMarketValue)} invested` : 'No positions yet'}</small>
            </article>
            <article className="stat-card">
              <span>Cash available</span>
              <strong>{formatCurrency(portfolioSummary.cashBalance)}</strong>
              <small>Buying power {formatCurrency(portfolioSummary.buyingPower)}</small>
            </article>
            <article className="stat-card">
              <span>Day P&amp;L</span>
              <strong className={portfolioSummary.pnl >= 0 ? 'positive' : 'negative'}>
                {formatCurrency(portfolioSummary.pnl)}
              </strong>
              <small>{positions.length} active positions</small>
            </article>
            <article className="stat-card">
              <span>Pending orders</span>
              <strong>{pendingOrders.length}</strong>
              <small>{pendingOrders.length > 0 ? 'Awaiting execution' : 'No open orders'}</small>
            </article>
          </div>

          {alert ? <div className={`alert ${alert.kind}`}>{alert.text}</div> : null}

          <div className="panel-grid">
            <section className="card card-wide">
              <div className="card-header">
                <h3>Portfolio overview</h3>
                <span className="pill">Live snapshot</span>
              </div>

              {positions.length === 0 ? (
                <div className="empty-state">
                  <p>No holdings yet. Search the market and place your first trade.</p>
                </div>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Symbol</th>
                      <th>Quantity</th>
                      <th>Avg cost</th>
                      <th>Market</th>
                      <th>Value</th>
                      <th>Gain/Loss</th>
                    </tr>
                  </thead>
                  <tbody>
                    {positions.map((position) => {
                      const instrument = marketBySymbol[position.symbol]
                      const marketPrice = instrument?.price ?? position.averageCost
                      const value = position.quantity * marketPrice
                      const pnlValue = (marketPrice - position.averageCost) * position.quantity

                      return (
                        <tr key={position.symbol}>
                          <td>
                            <div className="symbol-stack">
                              <strong>{position.symbol}</strong>
                              <span>{instrument?.name ?? position.symbol}</span>
                            </div>
                          </td>
                          <td>{formatCompact(position.quantity)}</td>
                          <td>{formatCurrency(position.averageCost)}</td>
                          <td>{formatCurrency(marketPrice)}</td>
                          <td>{formatCurrency(value)}</td>
                          <td className={pnlValue >= 0 ? 'positive' : 'negative'}>
                            {formatCurrency(pnlValue)}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              )}
            </section>

            <section className="card">
              <div className="card-header">
                <h3>Sector mix</h3>
                <span className="pill muted">Diversification</span>
              </div>
              {diversification.length === 0 ? (
                <div className="empty-state">
                  <p>Add positions to view diversification.</p>
                </div>
              ) : (
                <div className="sector-list">
                  {diversification.map((sector) => (
                    <div key={sector.name} className="sector-row">
                      <div className="sector-meta">
                        <strong>{sector.name}</strong>
                        <span>{formatCurrency(sector.value)}</span>
                      </div>
                      <div className="progress-track">
                        <span
                          style={{
                            width: `${Math.max(12, (sector.value / Math.max(1, diversification[0].value)) * 100)}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </div>

          <div className="panel-grid">
            <section className="card card-wide">
              <div className="card-header">
                <h3>Order ticket</h3>
                <span className="pill">{selectedInstrument ? selectedInstrument.symbol : 'Symbol'}</span>
              </div>

              <form className="trade-form" onSubmit={handleTrade}>
                <label>
                  Symbol
                  <input
                    value={orderSymbol}
                    onChange={(event) => setOrderSymbol(event.target.value.toUpperCase())}
                    placeholder="AAPL"
                  />
                </label>

                <label>
                  Side
                  <select value={orderSide} onChange={(event) => setOrderSide(event.target.value as 'BUY' | 'SELL')}>
                    <option value="BUY">Buy</option>
                    <option value="SELL">Sell</option>
                  </select>
                </label>

                <label>
                  Quantity
                  <input
                    type="number"
                    min={1}
                    value={orderQuantity}
                    onChange={(event) => setOrderQuantity(Number(event.target.value) || 1)}
                  />
                </label>

                <button type="submit" className="primary-button" disabled={loading}>
                  {loading ? 'Processing…' : `${orderSide} ${formatCompact(orderQuantity)} shares`}
                </button>
              </form>

              {selectedInstrument ? (
                <div className="quote-card">
                  <div>
                    <span>{selectedInstrument.name}</span>
                    <strong>{selectedInstrument.symbol}</strong>
                  </div>
                  <div>
                    <span>Last price</span>
                    <strong>{formatCurrency(selectedInstrument.price)}</strong>
                  </div>
                  <div>
                    <span>Asset class</span>
                    <strong>{selectedInstrument.assetClass}</strong>
                  </div>
                </div>
              ) : null}
            </section>

            <section className="card">
              <div className="card-header">
                <h3>Market movers</h3>
                <span className="pill muted">Watchlist</span>
              </div>

              <div className="market-list">
                {market.map((instrument) => (
                  <button
                    key={instrument.symbol}
                    type="button"
                    className={instrument.symbol === orderSymbol ? 'market-row selected' : 'market-row'}
                    onClick={() => setOrderSymbol(instrument.symbol)}
                  >
                    <div>
                      <strong>{instrument.symbol}</strong>
                      <span>{instrument.assetClass}</span>
                    </div>
                    <strong>{formatCurrency(instrument.price)}</strong>
                  </button>
                ))}
              </div>
            </section>
          </div>

          <div className="panel-grid">
            <section className="card">
              <div className="card-header">
                <h3>Recent orders</h3>
                <span className="pill muted">History</span>
              </div>

              {orderHistory.length === 0 ? (
                <div className="empty-state">
                  <p>No trades yet. Your execution history will appear here.</p>
                </div>
              ) : (
                <ul className="order-list">
                  {orderHistory.map((order) => (
                    <li key={order.id} className="order-item">
                      <div>
                        <strong>{order.symbol}</strong>
                        <span>{order.side} · {order.quantity} shares</span>
                      </div>
                      <div>
                        <span className={`tag ${order.status.toLowerCase()}`}>{order.status}</span>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section className="card">
              <div className="card-header">
                <h3>Account settings</h3>
                <span className="pill muted">Preferences</span>
              </div>

              <div className="settings-list">
                <label>
                  Risk profile
                  <select
                    value={accountSettings.riskLevel}
                    onChange={(event) =>
                      setAccountSettings((current) => ({ ...current, riskLevel: event.target.value }))
                    }
                  >
                    <option value="Conservative">Conservative</option>
                    <option value="Moderate">Moderate</option>
                    <option value="Aggressive">Aggressive</option>
                  </select>
                </label>

                <label>
                  Daily loss limit
                  <input
                    type="range"
                    min={500}
                    max={10000}
                    step={250}
                    value={accountSettings.maxDailyLoss}
                    onChange={(event) =>
                      setAccountSettings((current) => ({
                        ...current,
                        maxDailyLoss: Number(event.target.value),
                      }))
                    }
                  />
                  <strong>{formatCurrency(accountSettings.maxDailyLoss)}</strong>
                </label>

                <label className="toggle-row">
                  <span>Notifications</span>
                  <input
                    type="checkbox"
                    checked={accountSettings.notifications}
                    onChange={(event) =>
                      setAccountSettings((current) => ({
                        ...current,
                        notifications: event.target.checked,
                      }))
                    }
                  />
                </label>

                <label>
                  Accessibility
                  <select
                    value={accountSettings.accessibility}
                    onChange={(event) =>
                      setAccountSettings((current) => ({ ...current, accessibility: event.target.value }))
                    }
                  >
                    <option value="High contrast">High contrast</option>
                    <option value="Standard">Standard</option>
                    <option value="Large text">Large text</option>
                  </select>
                </label>
              </div>
            </section>
          </div>

          <div className="panel-grid">
            <section className="card">
              <div className="card-header">
                <h3>Pending orders</h3>
                <span className="pill muted">Execution queue</span>
              </div>

              {pendingOrders.length === 0 ? (
                <div className="empty-state">
                  <p>There are no pending orders.</p>
                </div>
              ) : (
                <ul className="pending-list">
                  {pendingOrders.map((order) => (
                    <li key={order.id} className="pending-item">
                      <div>
                        <strong>{order.symbol}</strong>
                        <span>{order.side} · {order.quantity} shares</span>
                      </div>
                      <button type="button" className="ghost-button small" onClick={() => void handleCancelOrder(order.id)}>
                        Cancel
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section className="card tutorial-card">
              <div className="card-header">
                <h3>Beginner trading tutorial</h3>
                <span className="pill muted">New here?</span>
              </div>

              <ol>
                <li>Review market prices and sector concentration before you act.</li>
                <li>Use cash and buying power to estimate how much you can safely allocate.</li>
                <li>Place a small order first to confirm your strategy and monitor execution.</li>
                <li>Monitor open orders and recent activity as your portfolio evolves.</li>
              </ol>
            </section>
          </div>
        </section>
      </div>
    </main>
  )
}

export default App
