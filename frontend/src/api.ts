/// <reference types="vite/client" />

const API_URL = import.meta.env.VITE_API_URL || '/api'

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message)
    this.name = 'ApiError'
  }
}

export type User = { name: string; username: string; role: 'CUSTOMER' | 'ADMIN' | 'SUPPORT'; accountId?: string }
export type Account = { id: string; name: string; type: string; number: string; balance: number; color: string }
export type Transaction = { id: string; merchant: string; category: string; date: string; amount: number; status: string; initials: string }
export type Loan = { id: string; amount: number; outstanding: number; status: string; createdAt: string }
export type Investment = { id: string; product: string; amount: number; status: string; createdAt: string }
export type Ticket = { id: string; userId: string; subject: string; message: string; status: string; createdAt: string }

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      credentials: 'include',
      headers: { 'Content-Type': 'application/json', ...options?.headers },
    })
  } catch {
    throw new Error('Unable to reach BankEase API. Start the backend on http://localhost:8080 and try again.')
  }
  if (!response.ok) {
    let detail = `Request failed (${response.status})`
    try { const body = await response.json(); detail = body.message || body.error || detail } catch { /* non-json error */ }
    const method = options?.method || 'GET'
    const endpoint = `${method} ${path}`
    const message = response.status >= 500
      ? `BankEase API returned HTTP ${response.status} for ${endpoint}: ${detail}. Check the backend terminal for the exception details.`
      : `${endpoint}: ${detail}`
    throw new ApiError(response.status, message)
  }
  return response.status === 204 ? (undefined as T) : response.json()
}

const toTransaction = (item: any): Transaction => ({
  id: item.id, merchant: item.description || item.merchant || 'Transaction',
  category: item.type || 'Banking', date: item.createdAt ? new Date(item.createdAt).toLocaleDateString() : '',
  amount: Number(item.amount), status: item.status || 'Pending',
  initials: (item.description || 'TX').slice(0, 2).toUpperCase(),
})

export const api = {
  login: (username: string, password: string) => request<{ message: string; user: User }>('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  me: () => request<User>('/me'),
  logout: () => request<{ message: string }>('/auth/logout', { method: 'POST' }),
  dashboard: () => request<any>('/dashboard'),
  accounts: async () => {
    const dashboard = await request<any>('/dashboard')
    const account = dashboard.account
    return [{ id: account.id, name: 'Everyday account', type: 'Checking', number: account.number, balance: Number(account.balance), color: '#3159e8' }] as Account[]
  },
  transactions: async () => (await request<any[]>('/transactions')).map(toTransaction),
  transfer: (data: { destination: string; amount: number; description: string }) => request('/transfers', { method: 'POST', body: JSON.stringify(data) }),
  payBill: (data: { biller: string; reference: string; amount: number }) => request('/bills/pay', { method: 'POST', body: JSON.stringify(data) }),
  loan: (amount: number) => request<Loan>('/loans', { method: 'POST', body: JSON.stringify({ amount }) }),
  investment: (product: string, amount: number) => request<Investment>('/investments', { method: 'POST', body: JSON.stringify({ product, amount }) }),
  createTicket: (subject: string, message: string) => request<Ticket>('/support/tickets', { method: 'POST', body: JSON.stringify({ subject, message }) }),
  tickets: () => request<Ticket[]>('/support/tickets'),
  users: () => request<User[]>('/admin/users'),
  resolveTicket: (id: string) => request<Ticket>(`/support/tickets/${encodeURIComponent(id)}/resolve`, { method: 'PATCH' }),
}
